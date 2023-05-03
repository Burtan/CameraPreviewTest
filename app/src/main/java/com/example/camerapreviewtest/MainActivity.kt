package com.example.camerapreviewtest

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.app.ActivityCompat
import com.example.camerapreviewtest.ui.theme.CameraPreviewTestTheme

class MainActivity : ComponentActivity() {

    private val _permissionState = mutableStateOf<Map<String, Boolean>>(emptyMap())
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        _permissionState.value = _permissionState.value.plus(it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissions()

        setContent {
            val permissions by remember { _permissionState }
            val context = LocalContext.current
            val lifecycleOwner = LocalLifecycleOwner.current
            val previewView = remember { PreviewView(context) }
            val scanner = remember {
                QrCodeScanner(
                    surfaceProvider = previewView.surfaceProvider,
                    lifecycleOwner = lifecycleOwner,
                    context = context
                )
            }

            CameraPreviewTestTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ScannerContent(
                        previewView = previewView,
                        scanner = scanner,
                        hasCameraPermission = permissions[Manifest.permission.CAMERA],
                        requestPermission = ::requestPermissions,
                    )
                }
            }
        }
    }

    private fun requestPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        when {
            checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                _permissionState.value = _permissionState.value.plus(Manifest.permission.CAMERA to true)
            }
            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) -> {
                // the user declined the request and needs more information on why to
                // grant it
                _permissionState.value = _permissionState.value.plus(Manifest.permission.CAMERA to false)
            }
            else -> permissionsToRequest.add(Manifest.permission.CAMERA)
        }
        requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
    }

}