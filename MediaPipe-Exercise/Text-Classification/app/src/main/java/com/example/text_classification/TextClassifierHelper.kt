package com.example.text_classification

import android.content.Context
import android.util.Log
import com.google.mediapipe.tasks.components.containers.Classifications
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.text.textclassifier.TextClassifier

class TextClassifierHelper(
    val modelName: String = "bert_classifier.tflite",
    val context: Context,
    val classifierListener: ClassifierListener? = null,
) {
    private val textClassifier: TextClassifier? = null

    init {
        initClassifier()
    }

    private fun initClassifier() {
        try {
            // Konfigurasi TextClassifier MediaPipe
            val optionBuilder = TextClassifier.TextClassifierOptions.builder()

            val baseOptionBuilder = BaseOptions.builder().setModelAssetPath(modelName)
            optionBuilder.setBaseOptions(baseOptionBuilder.build())

        } catch (e: IllegalStateException) {
            classifierListener?.onError(context.getString(R.string.text_classifier_failed))
            Log.e(TAG, e.message.toString())
        }
    }

    interface ClassifierListener {
        fun onError(error: String)
        fun onResult(
            result: List<Classifications>?,
            inferenceTime: Long,
        )
    }

    companion object {
        private const val TAG = "TextClassifierHelper"
    }
}