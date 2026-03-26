package com.example.fruitrek.ui.screens

import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.fruitrek.ui.FruiTrekViewModel
import com.example.fruitrek.ui.components.CelebrationOverlay
import com.example.fruitrek.ui.components.DetectionOverlay
import com.example.fruitrek.ui.theme.MangoOrange
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong // <-- CRITICAL NEW IMPORT

@Composable
fun CameraScreen(
    viewModel: FruiTrekViewModel,
    onOpenPassport: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val detections by viewModel.detections.collectAsState()
    val lockedFruit by viewModel.lockedFruit.collectAsState()
    val showCelebration by viewModel.showCelebration.collectAsState()
    val modelReady by viewModel.modelReady.collectAsState()
    val unlockedIds by viewModel.unlockedFruitIds.collectAsState()

    val executor = remember { Executors.newSingleThreadExecutor() }

    // THE VALVE: Thread-safe timestamp to track the last processed frame
    val lastAnalyzedTimestamp = remember { AtomicLong(0L) }

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Camera Preview ────────────────────────────────────────────────────
        AndroidView(
            factory = { ctx ->
                val pv = PreviewView(ctx)
                pv.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                pv.scaleType = PreviewView.ScaleType.FILL_CENTER
                pv
            },
            modifier = Modifier.fillMaxSize(),
            update = { previewView ->
                val future = ProcessCameraProvider.getInstance(context)
                future.addListener({
                    val provider = future.get()

                    val preview = Preview.Builder().build()
                    preview.setSurfaceProvider(previewView.surfaceProvider)

                    val resolutionSelector = ResolutionSelector.Builder()
                        .setResolutionStrategy(ResolutionStrategy.HIGHEST_AVAILABLE_STRATEGY)
                        .build()

                    val analysis = ImageAnalysis.Builder()
                        .setResolutionSelector(resolutionSelector)
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                        .build()

                    analysis.setAnalyzer(executor) { imageProxy ->
                        val currentTime = System.currentTimeMillis()

                        // THE THROTTLE LOGIC:
                        // If less than 300ms has passed, drop the frame instantly to save CPU.
                        if (currentTime - lastAnalyzedTimestamp.get() < 300) {
                            imageProxy.close()
                            return@setAnalyzer
                        }

                        // Update the timestamp lock
                        lastAnalyzedTimestamp.set(currentTime)

                        try {
                            // Only perform the heavy Bitmap conversion 3 times a second
                            val bmp = imageProxy.toBitmap()
                            viewModel.onNewFrame(bmp)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        } finally {
                            // CRITICAL: Always close the proxy to prevent memory leaks
                            imageProxy.close()
                        }
                    }

                    try {
                        provider.unbindAll()
                        provider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            analysis
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(context))
            }
        )

        // ── Detection overlay ─────────────────────────────────────────────────
        DetectionOverlay(detections = detections)

        // ── Top HUD ───────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.65f), Color.Transparent)
                    )
                )
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "FruiTrek",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "${unlockedIds.size} / 7 fruits found",
                        color = Color.White.copy(alpha = 0.80f),
                        fontSize = 12.sp
                    )
                }
                IconButton(
                    onClick = onOpenPassport,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MangoOrange)
                ) {
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = "Passport",
                        tint = Color.White
                    )
                }
            }
        }

        // ── Bottom status ─────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.70f))
                    )
                )
                .navigationBarsPadding()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                !modelReady -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = MangoOrange,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Loading fruit detector...",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
                detections.isEmpty() -> {
                    Text(
                        text = "Point your camera at a fruit!",
                        color = Color.White.copy(alpha = 0.88f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                else -> {
                    val pct = (detections.first().confidence * 100).toInt()
                    Text(
                        text = "Scanning... $pct% confident",
                        color = Color(0xFFA5D6A7),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // ── Celebration ───────────────────────────────────────────────────────
        CelebrationOverlay(
            fruit = lockedFruit,
            visible = showCelebration,
            onDismiss = viewModel::dismissCelebration
        )
    }
}