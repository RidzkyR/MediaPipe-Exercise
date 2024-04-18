package com.example.text_classification

import android.content.Context
import com.google.mediapipe.tasks.components.containers.Classifications

class TextClassifierHelper(
    val modelName: String = "bert_classifier.tflite",
    val context: Context,
    val classifierListener: ClassifierListener? = null,
) {
    interface ClassifierListener {
        fun onError(error: String)
        fun onResult(
            result: List<Classifications>?,
            inferenceTime: Long
        )
    }
}