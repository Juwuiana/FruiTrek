package com.example.fruitrek.ui

import android.app.Application
import android.graphics.Bitmap
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fruitrek.data.model.DetectionResult
import com.example.fruitrek.data.model.Fruit
import com.example.fruitrek.data.model.FruitCatalog
import com.example.fruitrek.data.repository.PassportRepository
import com.example.fruitrek.ml.FruitDetector
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

class FruiTrekViewModel(application: Application) : AndroidViewModel(application) {

    private val detector   = FruitDetector(application)
    private val repository = PassportRepository(application)
    private var tts        : TextToSpeech? = null
    private var ttsReady   = false

    // ── UI State ──────────────────────────────────────────────────────────────

    private val _detections     = MutableStateFlow<List<DetectionResult>>(emptyList())
    val detections: StateFlow<List<DetectionResult>> = _detections.asStateFlow()

    private val _lockedFruit    = MutableStateFlow<Fruit?>(null)
    val lockedFruit: StateFlow<Fruit?> = _lockedFruit.asStateFlow()

    private val _showCelebration = MutableStateFlow(false)
    val showCelebration: StateFlow<Boolean> = _showCelebration.asStateFlow()

    private val _modelReady     = MutableStateFlow(false)
    val modelReady: StateFlow<Boolean> = _modelReady.asStateFlow()

    // ── Passport ──────────────────────────────────────────────────────────────

    val unlockedFruitIds: StateFlow<Set<String>> = repository.unlockedFruits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    // ── Sustained detection tracking ──────────────────────────────────────────
    // A fruit must appear in N consecutive frames before being "confirmed".
    // This prevents false positives from brief camera movements.

    private var sustainedLabel = ""
    private var sustainedCount = 0
    private val FRAMES_NEEDED  = 8        // ~0.8 s at ~10 fps inference rate
    private var celebrationJob : Job? = null

    // ── Init ──────────────────────────────────────────────────────────────────

    init {
        loadModel()
        initTts()
    }

    private fun loadModel() {
        viewModelScope.launch {
            _modelReady.value = detector.initialize()
            if (!_modelReady.value) {
                Log.w("ViewModel",
                    "Model not loaded. Add fruitrek_model.tflite to app/src/main/assets/")
            }
        }
    }

    private fun initTts() {
        tts = TextToSpeech(getApplication()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                ttsReady = true
            }
        }
    }

    // ── Frame processing ──────────────────────────────────────────────────────

    /**
     * Called by the CameraX ImageAnalysis analyzer for every video frame.
     * Runs on a background executor thread.
     */
    fun onNewFrame(bitmap: Bitmap) {
        if (!_modelReady.value) return
        viewModelScope.launch {
            val results = detector.detect(bitmap)
            _detections.value = results

            val top = results.firstOrNull()
            if (top != null && top.confidence > 0.50f) {
                if (top.label == sustainedLabel) {
                    sustainedCount++
                    if (sustainedCount == FRAMES_NEEDED) {
                        onFruitConfirmed(top.label)
                    }
                } else {
                    sustainedLabel = top.label
                    sustainedCount = 1
                }
            } else {
                sustainedLabel = ""
                sustainedCount = 0
            }
        }
    }

    // ── Fruit confirmed ───────────────────────────────────────────────────────

    private fun onFruitConfirmed(fruitId: String) {
        val fruit = FruitCatalog.findById(fruitId) ?: return
        _lockedFruit.value = fruit

        viewModelScope.launch {
            val isNew = fruitId !in unlockedFruitIds.value
            repository.unlockFruit(fruitId)
            speak(fruit, isNew)
            if (isNew) triggerCelebration()
        }
    }

    private fun speak(fruit: Fruit, isNew: Boolean) {
        if (!ttsReady) return
        val text = if (isNew)
            "You found a ${fruit.englishName}! " +
            "In Filipino, we call it ${fruit.filipinoName}! " +
            "It has been added to your Fruit Passport!"
        else
            "That is a ${fruit.englishName}! ${fruit.filipinoName} in Filipino!"

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "fruit_${fruit.id}")
    }

    private fun triggerCelebration() {
        celebrationJob?.cancel()
        celebrationJob = viewModelScope.launch {
            _showCelebration.value = true
            delay(4_000)
            _showCelebration.value = false
            _lockedFruit.value     = null
            sustainedCount = 0
        }
    }

    fun dismissCelebration() {
        celebrationJob?.cancel()
        _showCelebration.value = false
        _lockedFruit.value     = null
        sustainedCount = 0
    }

    // ── Passport helpers ──────────────────────────────────────────────────────

    /** Returns all fruits paired with their unlock status */
    fun getPassportFruits(unlockedIds: Set<String>): List<Pair<Fruit, Boolean>> =
        FruitCatalog.all.map { it to (it.id in unlockedIds) }

    // ── Cleanup ───────────────────────────────────────────────────────────────

    override fun onCleared() {
        super.onCleared()
        detector.close()
        tts?.shutdown()
    }
}
