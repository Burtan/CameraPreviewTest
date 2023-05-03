package com.example.camerapreviewtest

import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.launch

@Composable
fun ScannerContent(
    previewView: PreviewView,
    scanner: QrCodeScanner,
    hasCameraPermission: Boolean?,
    requestPermission: () -> Unit,
) {
    LaunchedEffect(scanner) {
        launch {
            scanner.startScanning()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (hasCameraPermission) {
            true -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AndroidView({ previewView }, modifier = Modifier.fillMaxSize())
                }
            }
            false -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "Need camera permission for qr-code scanning",
                        textAlign = TextAlign.Justify
                    )
                    Button(onClick = { requestPermission() }) {
                        Text("Grant camera permission")
                    }
                }
            }
            null -> { // camera request in progress
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "Requesting camera permission",
                        textAlign = TextAlign.Justify
                    )
                }
            }
        }
    }
}

@Composable
@Preview(locale = "de")
fun ScannerContentPreview() {
    Column(modifier = Modifier.fillMaxSize()) {
        var hasCameraPermission by remember { mutableStateOf(true) }
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val previewView = remember { PreviewView(context) }
        ScannerContent(
            previewView = previewView,
            scanner = QrCodeScanner(
                surfaceProvider = previewView.surfaceProvider,
                lifecycleOwner = lifecycleOwner,
                context = context
            ),
            hasCameraPermission = hasCameraPermission,
            requestPermission = { hasCameraPermission = true }
        )
    }
}
