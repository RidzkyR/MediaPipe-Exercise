package com.example.audio_classifier

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.util.Log
import com.google.mediapipe.formats.proto.ClassificationProto.Classification
import com.google.mediapipe.tasks.audio.audioclassifier.AudioClassifier
import com.google.mediapipe.tasks.audio.audioclassifier.AudioClassifierResult
import com.google.mediapipe.tasks.audio.core.RunningMode
import com.google.mediapipe.tasks.components.containers.Classifications
import com.google.mediapipe.tasks.core.BaseOptions
import java.lang.IllegalStateException
import java.lang.RuntimeException

class AudioClassifierHelper(
    val threshold: Float = 0.1f,
    val maxResult: Int = 3,
    val modelName: String = "yamnet.tflite",
    val runningMode: RunningMode = RunningMode.AUDIO_STREAM,
    val overlap: Float = 0.5f,
    val context: Context,
    val classifierListener: ClassifierListener? = null,
) {
    private var audioClassifier: AudioClassifier? = null
    private var recorder: AudioRecord? = null
    init {
        initClassifier()
    }

    private fun initClassifier() {
        try{
            val optionBuilder = AudioClassifier.AudioClassifierOptions.builder()
                .setScoreThreshold(threshold)
                .setMaxResults(maxResult)
                .setRunningMode(runningMode)

            if (runningMode == RunningMode.AUDIO_STREAM){
                optionBuilder
                    .setResultListener(this::streamAudioResultListener)
                    .setErrorListener(this::streamAudioResultListener)
            }

            val baseOptionBuilder = BaseOptions.builder()
                .setModelAssetPath(modelName)
            optionBuilder.setBaseOptions(baseOptionBuilder.build())

            //inisialisasi audio classifier
            audioClassifier = AudioClassifier.createFromOptions(context,optionBuilder.build())

            if (runningMode == RunningMode.AUDIO_STREAM){
                recorder = audioClassifier?.createAudioRecord(
                    AudioFormat.CHANNEL_IN_DEFAULT,
                    SAMPLING_RATE_IN_HZ,
                    BUFFER_SIZE_IN_BYTES.toInt()
                )
            }

        }catch (e: IllegalStateException){
            classifierListener?.onError(context.getString(R.string.audio_classifier_failed))
            Log.e(TAG, "MP task failed to load with error: " + e.message)
        }catch (e: RuntimeException){
            classifierListener?.onError(context.getString(R.string.audio_classifier_failed))
            Log.e(TAG, "MP task failed to load with error: " + e.message)
        }
    }

    private fun streamAudioResultListener(resultListener: AudioClassifierResult) {
        classifierListener?.onResults(
            resultListener.classificationResults().first().classifications(),
            resultListener.timestampMs()
        )
    }
    private fun streamAudioResultListener(e: RuntimeException){
        classifierListener?.onError(e.message.toString())
    }

    interface ClassifierListener {
        fun onResults(resul: List<Classifications>, inferenceTime: Long)
        fun onError(error: String)
    }

    companion object {
        private const val TAG = "AudioClassifierHelper"

        private const val SAMPLING_RATE_IN_HZ = 16000
        private const val EXPECTED_INPUT_LENGTH = 0.975F
        private const val REQUIRE_INPUT_BUFFER_SIZE = SAMPLING_RATE_IN_HZ * EXPECTED_INPUT_LENGTH
        private const val BUFFER_SIZE_FACTOR: Int = 2
        private const val BUFFER_SIZE_IN_BYTES =
            REQUIRE_INPUT_BUFFER_SIZE * Float.SIZE_BYTES * BUFFER_SIZE_FACTOR
    }
}
