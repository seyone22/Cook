package com.seyone22.cook.helper

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.seyone22.cook.ui.screen.meals.launchCamera
import android.Manifest

class CameraCaptureManager(
    private val context: Context,
    private val activity: Activity,
    private val onPhotoCaptured: (Uri) -> Unit
) {
    var photoUri: Uri? = null
        private set

    private lateinit var cameraLauncher: ManagedActivityResultLauncher<Uri, Boolean>
    private lateinit var permissionLauncher: ManagedActivityResultLauncher<String, Boolean>

    fun registerLaunchers(
        cameraLauncher: ManagedActivityResultLauncher<Uri, Boolean>,
        permissionLauncher: ManagedActivityResultLauncher<String, Boolean>
    ) {
        this.cameraLauncher = cameraLauncher
        this.permissionLauncher = permissionLauncher
    }

    fun startCameraCapture() {
        when {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                launchCamera()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                activity, Manifest.permission.CAMERA
            ) -> {
                Toast.makeText(context, "Camera permission is needed to take photos.", Toast.LENGTH_LONG).show()
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }

            else -> {
                Toast.makeText(
                    context,
                    "Camera permission is permanently denied. Please enable it in app settings.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun onPermissionResult(granted: Boolean) {
        if (granted) launchCamera()
    }

    private fun launchCamera() {
        launchCamera(context) { uri ->
            photoUri = uri
            cameraLauncher.launch(uri)
        }
    }

    fun onCameraResult(success: Boolean) {
        if (success && photoUri != null) {
            onPhotoCaptured(photoUri!!)
        }
    }
}

@Composable
fun rememberCameraCaptureManager(
    onPhotoCaptured: (Uri) -> Unit
): CameraCaptureManager {
    val context = LocalContext.current
    val activity = context as Activity

    val cameraCaptureManager = remember {
        CameraCaptureManager(context, activity, onPhotoCaptured)
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        cameraCaptureManager.onCameraResult(success)
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        cameraCaptureManager.onPermissionResult(granted)
    }

    cameraCaptureManager.registerLaunchers(cameraLauncher, permissionLauncher)

    return cameraCaptureManager
}
