package com.seyone22.cook

import android.Manifest.permission.CAMERA
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.FirebaseApp
import com.seyone22.cook.data.remote.SyncRepository
import com.seyone22.cook.ui.AppViewModelProvider
import com.seyone22.cook.ui.theme.CookTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

/**
 * MainViewModel handles the "Startup Sync" logic to keep the Activity lean.
 */
class MainViewModel(private val syncRepository: SyncRepository) : ViewModel() {
    fun triggerStartupSync() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("MainViewModel", "Starting background reconciliation...")
                syncRepository.performStartupSync()
            } catch (e: Exception) {
                Log.e("MainViewModel", "Startup Sync failed", e)
            }
        }
    }
}

class MainActivity : ComponentActivity() {

    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>
    private var pendingPhotoUri: Uri? = null

    // Reactive states to pass into CookApp
    private var sharedRecipeId by mutableStateOf<String?>(null)
    private var initialSharedText by mutableStateOf<String?>(null)

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        registerPermissionLauncher()

        // Initial check for Deep Links or Shared Text
        handleIncomingIntent(intent)

        setContent {
            // Obtain the MainViewModel via your Factory
            val mainViewModel: MainViewModel = viewModel(factory = AppViewModelProvider.Factory)

            // Trigger the Startup Sync
            mainViewModel.triggerStartupSync()

            CookTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Pass all external entry points into the main Compose entry point
                    CookApp(
                        onCameraRequested = { requestCameraAccess() },
                        sharedText = initialSharedText,
                        sharedRecipeId = sharedRecipeId,
                        onSharedRecipeHandled = {
                            sharedRecipeId = null
                            initialSharedText = null
                        }
                    )
                }
            }
        }
    }

    /**
     * Handles cases where the app is already in memory and a new intent is received.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    /**
     * Routes incoming data from Deep Links (QR codes) or System Shares.
     */
    private fun handleIncomingIntent(intent: Intent) {
        // 1. Handle Deep Links (cookapp://shared/{recipeId})
        val uri: Uri? = intent.data
        if (uri != null && uri.scheme == "cookapp" && uri.host == "shared") {
            val id = uri.lastPathSegment
            if (!id.isNullOrBlank()) {
                Log.d("MainActivity", "Deep Link detected for Recipe: $id")
                sharedRecipeId = id
                return
            }
        }

        // 2. Handle System Intent Actions
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
            Log.e("MainActivity", "Could not collapse notification shade", e)
        }
    }
}