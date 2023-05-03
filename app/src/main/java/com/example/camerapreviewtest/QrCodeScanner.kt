package com.example.camerapreviewtest

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

@SuppressLint("UnsafeOptInUsageError")
class QrCodeScanner(
    private val surfaceProvider: Preview.SurfaceProvider,
    private val lifecycleOwner: LifecycleOwner,
    private val context: Context
) {

    private val _qrCodeFlow = MutableStateFlow<String?>(null)
    private val _errorFlow = MutableSharedFlow<Throwable>(0, 10, BufferOverflow.SUSPEND)
    private var cameraProvider: ProcessCameraProvider? = null
    private val executor = ContextCompat.getMainExecutor(context)
    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .build()
    private val scanner = try {
        BarcodeScanning.getClient(options)
    } catch (e: Throwable) {
        _errorFlow.tryEmit(e)
        null
    }
    private val analyzer = ImageAnalysis.Analyzer { imageProxy ->
        val rotation = imageProxy.imageInfo.rotationDegrees

        imageProxy.image?.let { image ->
            val inputImage = InputImage.fromMediaImage(image, rotation)

            scanner?.process(inputImage)
                ?.addOnFailureListener {
                    _errorFlow.tryEmit(it)
                    imageProxy.close()
                }
                ?.addOnSuccessListener { results ->
                    results.firstOrNull()?.let { code ->
                        _qrCodeFlow.value = code.rawValue
                        cameraProvider?.unbindAll()
                    }
                    imageProxy.close()
                }
        }
    }
    private val preview = Preview.Builder().build()
        .also { it.setSurfaceProvider(executor, surfaceProvider) }
    private val imageAnalysis = ImageAnalysis.Builder().build()
        .also { it.setAnalyzer(executor, analyzer) }
    val qrCodeFlow = _qrCodeFlow.asStateFlow()
    val errorFlow = _errorFlow.asSharedFlow()

    suspend fun startScanning() {
        _qrCodeFlow.value = null
        try {
            cameraProvider = withContext(Dispatchers.IO) {
                ProcessCameraProvider.getInstance(context).get()
            }

            withContext(Dispatchers.Main) {
                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalysis
                )
            }
        } catch (e: Throwable) {
            _errorFlow.emit(e)
        }
    }

    fun stopScanning() {
        cameraProvider?.unbindAll()
        _qrCodeFlow.value = null
    }

    fun scanImage(bitmap: Bitmap) {
        scanner?.process(bitmap, 0)
            ?.addOnFailureListener {
                _errorFlow.tryEmit(it)
            }
            ?.addOnSuccessListener { results ->
                results.firstOrNull()?.let { code ->
                    _qrCodeFlow.value = code.rawValue
                    cameraProvider?.unbindAll()
                } ?: run {
                    _errorFlow.tryEmit(Exception("No qr code found"))
                }
            }
    }

}
