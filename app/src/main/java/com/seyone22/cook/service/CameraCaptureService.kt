// File: DocumentScannerService.kt
package com.seyone22.cook.service

import android.app.Activity
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

/**
 * Compose helper that creates and returns a function which, when invoked,
 * launches the ML Kit Document Scanner UI and returns scanned file URIs via callback.
 *
 * Usage:
 *   val launchScanner = rememberDocumentScannerLauncher(
 *       pageLimit = 1,
 *       allowGalleryImport = true,
 *       onSuccess = { imageUris, pdfUri -> /* handle */ },
 *       onError = { throwable -> /* handle */ }
 *   )
 *
 *   // call launchScanner() on FAB click
 *
 * Notes:
 * - The Document Scanner flow is provided by Google Play services and returns an IntentSender.
 * - You must run this from a composable context where LocalContext is an Activity.
 *
 * See: ML Kit Document Scanner docs (getStartScanIntent + Activity Result API). :contentReference[oaicite:5]{index=5}
 */
@Composable
fun rememberDocumentScannerLauncher(
    pageLimit: Int = 1,
    allowGalleryImport: Boolean = true,
    onSuccess: (imageUris: List<Uri>, pdfUri: Uri?) -> Unit,
    onError: (Throwable) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()

    // Build options (use constants per docs)
    val options = remember {
        GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(allowGalleryImport)
            .setPageLimit(pageLimit)
            .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG) // JPEG only; add PDF if you want both
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
            .build()
    }

    // Get client for scanner
    val scannerClient = remember { GmsDocumentScanning.getClient(options) }

    // ActivityResult launcher for the IntentSender returned by getStartScanIntent(...)
    val launcher = rememberLauncherForActivityResult(StartIntentSenderForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scanResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
            if (scanResult != null) {
                val pages = scanResult.getPages() ?: emptyList()
                val imageUris = pages.mapNotNull { page -> page.getImageUri() } // Java-style getters
                val pdfUri = scanResult.getPdf()?.getUri()
                onSuccess(imageUris, pdfUri)
            } else {
                onError(IOException("Failed to parse scanning result"))
            }
        } else {
            onError(IOException("Document scanner cancelled or returned code ${result.resultCode}"))
        }
    }

    // Return a function that starts the scanner through the ActivityResult flow.
    return remember {
        {
            if (activity == null) {
                onError(IllegalStateException("Document scanner requires an Activity context"))
                return@remember
            }

            scope.launch {
                try {
                    // getStartScanIntent(activity) returns a Task<IntentSender> — await() via kotlinx-coroutines-play-services
                    val intentSender = scannerClient.getStartScanIntent(activity).await()
                    launcher.launch(IntentSenderRequest.Builder(intentSender).build())
                } catch (e: Exception) {
                    onError(e)
                }
            }
        }
    }
}

/**
 * Recognize text from a Uri (file on device) using ML Kit Text Recognition.
 * This is a suspending helper that returns the concatenated recognized text.
 *
 * Uses: InputImage.fromFilePath(...) and TextRecognition.getClient(...).process(image).await()
 * See Text Recognition v2 docs. :contentReference[oaicite:6]{index=6}
 */
suspend fun recognizeTextFromUri(context: Context, fileUri: Uri): String = withContext(Dispatchers.IO) {
    val image = try {
        InputImage.fromFilePath(context, fileUri)
    } catch (e: Exception) {
        throw IOException("Unable to create InputImage from fileUri", e)
    }

    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    val visionText = try {
        recognizer.process(image).await() // await() from kotlinx-coroutines-play-services
    } catch (e: Exception) {
        throw IOException("Text recognition failed", e)
    }

    visionText.text // plain text; you can also use blocks/lines if you want structure
}

/**
 * Example: send `rawText` to your backend which will call Gemini / LLM and return JSON-LD.
 * Strong recommendation: do LLM calls on the server (protect API keys, add validation).
 *
 * This example uses OkHttp synchronously inside a `withContext(Dispatchers.IO)` block
 * to keep the example short and dependency-light. Replace with Ktor/Retrofit if you prefer.
 */
suspend fun parseRecipeViaBackend(
    rawText: String,
    backendUrl: String,
    bearerApiKey: String? = null // optional if your backend requires a front-end token
): String = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val json = JSONObject().put("text", rawText).toString()
    val mediaType = "application/json; charset=utf-8".toMediaType()
    val body = json.toRequestBody(mediaType)

    val requestBuilder = Request.Builder()
        .url(backendUrl)
        .post(body)

    bearerApiKey?.let { requestBuilder.header("Authorization", "Bearer $it") }

    val response = client.newCall(requestBuilder.build()).execute()
    if (!response.isSuccessful) {
        throw IOException("Backend returned HTTP ${response.code}: ${response.message}")
    }
    response.body?.string() ?: throw IOException("Backend returned empty body")
}
