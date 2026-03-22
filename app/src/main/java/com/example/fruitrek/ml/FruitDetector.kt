package com.example.fruitrek.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log
import com.example.fruitrek.data.model.DetectionResult
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/**
 * Wraps TFLite Interpreter using only java.nio — zero dependency on
 * tensorflow-lite-support. The Interpreter class is loaded reflectively
 * so if the AAR is present it works; if not, isInitialized stays false
 * and detect() returns an empty list (app runs without crashing).
 */
class FruitDetector(private val context: Context) {

    companion object {
        private const val TAG = "FruitDetector"
        private const val MODEL_FILE = "fruitrek_model.tflite"
        private const val LABELS_FILE = "labels.txt"
        const val INPUT_SIZE = 320
        private const val CONFIDENCE_THRESHOLD = 0.45f
        private const val IOU_THRESHOLD = 0.45f
        private const val MAX_DETECTIONS = 10
    }

    // Hold interpreter as Any to avoid compile-time resolution of Interpreter class
    private var interpreter: Any? = null
    private var runMethod: java.lang.reflect.Method? = null
    private var closeMethod: java.lang.reflect.Method? = null
    private var labels: List<String> = emptyList()

    var isInitialized: Boolean = false
        private set

    fun initialize(): Boolean {
        return try {
            val model = loadModelFile()

            // Load via reflection so compiler never needs to resolve the class
            val interpreterClass = Class.forName("org.tensorflow.lite.Interpreter")
            val optionsClass = Class.forName("org.tensorflow.lite.Interpreter\$Options")
            val opts = optionsClass.getDeclaredConstructor().newInstance()
            optionsClass.getMethod("setNumThreads", Int::class.java).invoke(opts, 4)

            interpreter = interpreterClass
                .getConstructor(MappedByteBuffer::class.java, optionsClass)
                .newInstance(model, opts)

            runMethod = interpreterClass.getMethod("run", Any::class.java, Any::class.java)
            closeMethod = interpreterClass.getMethod("close")

            labels = readLabels()
            isInitialized = true
            Log.d(TAG, "TFLite loaded via reflection. Labels: $labels")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Init failed: ${e.message}", e)
            false
        }
    }

    private fun loadModelFile(): MappedByteBuffer {
        val afd = context.assets.openFd(MODEL_FILE)
        val fis = FileInputStream(afd.fileDescriptor)
        val channel: FileChannel = fis.channel
        return channel.map(FileChannel.MapMode.READ_ONLY, afd.startOffset, afd.declaredLength)
    }

    private fun readLabels(): List<String> =
        context.assets.open(LABELS_FILE).use { stream ->
            BufferedReader(InputStreamReader(stream)).readLines().filter { it.isNotBlank() }
        }

    fun detect(bitmap: Bitmap): List<DetectionResult> {
        val interp = interpreter ?: return emptyList()
        val run = runMethod ?: return emptyList()
        if (!isInitialized) return emptyList()

        val resized = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)
        val inputBuf = toByteBuffer(resized)

        val anchors = 2100 // INPUT_SIZE=320 → 2100 anchors
        val out = Array(1) { Array(4 + labels.size) { FloatArray(anchors) } }

        try {
            run.invoke(interp, inputBuf, out)
        } catch (e: Exception) {
            Log.e(TAG, "Inference error: ${e.message}")
            return emptyList()
        }

        return nms(parse(out[0], anchors).sortedByDescending { it.confidence })
    }

    private fun toByteBuffer(bmp: Bitmap): ByteBuffer {
        val buf = ByteBuffer.allocateDirect(INPUT_SIZE * INPUT_SIZE * 3 * 4)
        buf.order(ByteOrder.nativeOrder())
        val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
        bmp.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)
        for (px in pixels) {
            buf.putFloat(((px shr 16) and 0xFF) / 255f)
            buf.putFloat(((px shr 8) and 0xFF) / 255f)
            buf.putFloat((px and 0xFF) / 255f)
        }
        buf.rewind()
        return buf
    }

    private fun parse(out: Array<FloatArray>, anchors: Int): List<DetectionResult> {
        val list = mutableListOf<DetectionResult>()
        for (i in 0 until anchors) {
            val cx = out[0][i]; val cy = out[1][i]
            val w = out[2][i]; val h = out[3][i]
            var best = 0f; var cls = -1
            for (c in labels.indices) {
                if (out[4 + c][i] > best) { best = out[4 + c][i]; cls = c }
            }
            if (best >= CONFIDENCE_THRESHOLD && cls >= 0) {
                list.add(DetectionResult(
                    label = labels[cls],
                    confidence = best,
                    boundingBox = RectF(
                        (cx - w / 2f).coerceIn(0f, 1f),
                        (cy - h / 2f).coerceIn(0f, 1f),
                        (cx + w / 2f).coerceIn(0f, 1f),
                        (cy + h / 2f).coerceIn(0f, 1f)
                    )
                ))
            }
        }
        return list
    }

    private fun nms(sorted: List<DetectionResult>): List<DetectionResult> {
        val kept = mutableListOf<DetectionResult>()
        val dead = BooleanArray(sorted.size)
        for (i in sorted.indices) {
            if (dead[i]) continue
            kept.add(sorted[i])
            if (kept.size >= MAX_DETECTIONS) break
            for (j in i + 1 until sorted.size) {
                if (!dead[j] && iou(sorted[i].boundingBox, sorted[j].boundingBox) > IOU_THRESHOLD)
                    dead[j] = true
            }
        }
        return kept
    }

    private fun iou(a: RectF, b: RectF): Float {
        val il = maxOf(a.left, b.left); val it2 = maxOf(a.top, b.top)
        val ir = minOf(a.right, b.right); val ib = minOf(a.bottom, b.bottom)
        if (ir <= il || ib <= it2) return 0f
        val inter = (ir - il) * (ib - it2)
        val union = a.width() * a.height() + b.width() * b.height() - inter
        return if (union <= 0f) 0f else inter / union
    }

    fun close() {
        try { closeMethod?.invoke(interpreter) } catch (_: Exception) {}
        interpreter = null
        isInitialized = false
    }
}