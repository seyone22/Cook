package com.seyone22.cook

import android.Manifest.permission.CAMERA
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.seyone22.cook.ui.theme.CookTheme
import java.io.File

class MainActivity : ComponentActivity() {

    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>
    private var pendingPhotoUri: Uri? = null

    // This holds a URL or text shared into the app
    private var initialSharedText: String? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        registerPermissionLauncher()
        handleIncomingIntent(intent)

        setContent {
            CookTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Pass shared text (if any) and camera request callback into the Compose app
                    CookApp(
                        onCameraRequested = { requestCameraAccess() },
                        sharedText = initialSharedText
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent) {
        when (intent.action) {
            "OPEN_CAMERA_FROM_TILE" -> requestCameraAccess()

            Intent.ACTION_SEND -> {
                if (intent.type == "text/plain") {
                    val shared = intent.getStringExtra(Intent.EXTRA_TEXT)
                    if (!shared.isNullOrBlank()) {
                        initialSharedText = shared.trim()
                    }
                }
            }
        }
    }

    private fun registerPermissionLauncher() {
        cameraPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                collapseNotificationShade()
                openCamera()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun requestCameraAccess() {
        if (ContextCompat.checkSelfPermission(this, CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            cameraPermissionLauncher.launch(CAMERA)
        }
    }

    private fun openCamera() {
        val photoFile = File(cacheDir, "quick_tile_image_${System.currentTimeMillis()}.jpg")
        val photoUri = FileProvider.getUriForFile(
            this,
            "$packageName.provider",
            photoFile
        )
        pendingPhotoUri = photoUri

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        if (cameraIntent.resolveActivity(packageManager) != null) {
            startActivity(cameraIntent)
        } else {
            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun collapseNotificationShade() {
        try {
            val statusBarService = getSystemService("statusbar")
            val statusBarManager = Class.forName("android.app.StatusBarManager")
            val collapse = statusBarManager.getMethod("collapsePanels")
            collapse.isAccessible = true
            collapse.invoke(statusBarService)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
