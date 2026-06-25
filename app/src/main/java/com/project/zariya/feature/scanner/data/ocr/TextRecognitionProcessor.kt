package com.project.zariya.feature.scanner.data.ocr

import android.content.Context
import android.net.Uri
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.project.zariya.core.util.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class TextRecognitionProcessor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val recognizer: TextRecognizer =
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    @OptIn(ExperimentalGetImage::class)
    fun processImage(imageProxy: ImageProxy): Flow<Result<String>> = callbackFlow {
        trySend(Result.Loading)

        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            trySend(Result.Error("Failed to acquire camera image"))
            imageProxy.close()
            close()
            return@callbackFlow
        }

        val inputImage = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        recognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                val extractedText = visionText.text
                if (extractedText.isBlank()) {
                    trySend(Result.Error("No text detected in the image"))
                } else {
                    trySend(Result.Success(extractedText))
                }
                imageProxy.close()
                close()
            }
            .addOnFailureListener { exception ->
                trySend(Result.Error("Text recognition failed: ${exception.localizedMessage}"))
                imageProxy.close()
                close()
            }

        awaitClose { imageProxy.close() }
    }

    fun processUri(uri: Uri): Flow<Result<String>> = flow {
        emit(Result.Loading)

        try {
            val inputImage = InputImage.fromFilePath(context, uri)
            val result = recognizeTextSuspend(inputImage)
            emit(result)
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            emit(Result.Error("Failed to load image: ${e.localizedMessage}"))
        } catch (e: Exception) {
            emit(Result.Error("Unexpected error: ${e.localizedMessage}"))
        }
    }

    private suspend fun recognizeTextSuspend(inputImage: InputImage): Result<String> =
        suspendCancellableCoroutine { continuation ->
            recognizer.process(inputImage)
                .addOnSuccessListener { visionText ->
                    val extractedText = visionText.text
                    if (extractedText.isBlank()) {
                        continuation.resume(Result.Error("No text detected in the image"))
                    } else {
                        continuation.resume(Result.Success(extractedText))
                    }
                }
                .addOnFailureListener { exception ->
                    continuation.resume(
                        Result.Error("Text recognition failed: ${exception.localizedMessage}")
                    )
                }
        }
}
