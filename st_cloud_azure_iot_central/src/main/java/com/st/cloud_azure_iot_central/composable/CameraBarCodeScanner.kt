package com.st.cloud_azure_iot_central.composable

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.st.ui.composables.BlueMsButton
import com.st.ui.composables.BlueMsButtonOutlined
import com.st.ui.theme.Shapes
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraBarCodeScanner(onClose: (String?) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    var preview by remember { mutableStateOf<Preview?>(null) }
    val barCodeVal = remember { mutableStateOf("") }
    Surface(
        modifier = Modifier.size(320.dp,400.dp),
        shape = Shapes.medium
    ) {

        val cameraPermissionState = rememberPermissionState(
            permission = Manifest.permission.CAMERA
        )

        if(cameraPermissionState.status.isGranted) {
            AndroidView(
                factory = { androidViewContext ->
                    PreviewView(androidViewContext).apply {
                        this.scaleType = PreviewView.ScaleType.FILL_CENTER
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    }
                },
                update = { previewView ->
                    val cameraSelector: CameraSelector = CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build()
                    val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
                    val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> =
                        ProcessCameraProvider.getInstance(context)

                    cameraProviderFuture.addListener({
                        preview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }
                        val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                        val barcodeAnalyser = BarCodeAnalyser { barcodes ->
                            barcodes.forEach { barcode ->
                                barcode.rawValue?.let { barcodeValue ->
                                    barCodeVal.value = barcodeValue
                                    try {
                                        cameraProvider.unbindAll()
                                    } catch (e: Exception) {
                                        Log.d(
                                            "CameraBarCodeScanner",
                                            "CameraPreview: ${e.localizedMessage}"
                                        )
                                    }
                                    onClose(barcodeValue)
                                }
                            }
                        }
                        val imageAnalysis: ImageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also {
                                it.setAnalyzer(cameraExecutor, barcodeAnalyser)
                            }

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                        } catch (e: Exception) {
                            Log.d("CameraBarCodeScanner", "CameraPreview: ${e.localizedMessage}")
                        }
                    }, ContextCompat.getMainExecutor(context))
                }
            )
        } else {
            MissingPermissionDialog(
                doNotShowRationale = cameraPermissionState.status.shouldShowRationale,
                onPermissionRequest = { cameraPermissionState.launchPermissionRequest() },
                goToSettings = {
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).also {
                        val uri = Uri.fromParts("package", context.packageName, null)
                        it.data = uri
                        context.startActivity(it)
                    }
                }
            )
        }
    }
}

@Composable
fun MissingPermissionDialog(
    doNotShowRationale: Boolean,
    goToSettings: () -> Unit,
    onPermissionRequest: () -> Unit
) {
    // TODO: extract string resource
    AlertDialog(
        onDismissRequest = { /** NOOP **/ },
        title = {
            androidx.compose.material3.Text(text = "Permission required")
        },
        text = {
            if(doNotShowRationale) {
                Text("Camera not available")
            } else {
                Text("The camera is necessary for reading the QR-Code")
            }
        },
        dismissButton = {
            BlueMsButtonOutlined(
                onClick = { /** NOOP **/ },
                text = stringResource(id = android.R.string.cancel)
            )
        },
        confirmButton = {
            if (doNotShowRationale) {
                BlueMsButton(
                    onClick = goToSettings,
                    text = "Open Settings"
                )
            } else {
                BlueMsButton(
                    onClick = onPermissionRequest,
                    text = stringResource(id = android.R.string.ok)
                )
            }
        }
    )
}

@SuppressLint("UnsafeOptInUsageError")
class BarCodeAnalyser(
    private val onBarcodeDetected: (barcodes: List<Barcode>) -> Unit
) : ImageAnalysis.Analyzer {
    private var lastAnalyzedTimeStamp = 0L

    override fun analyze(image: ImageProxy) {
        val currentTimestamp = System.currentTimeMillis()
        if (currentTimestamp - lastAnalyzedTimeStamp >= TimeUnit.SECONDS.toMillis(1)) {
            image.image?.let { imageToAnalyze ->
                val options = BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                    .build()
                val barcodeScanner = BarcodeScanning.getClient(options)
                val imageToProcess =
                    InputImage.fromMediaImage(imageToAnalyze, image.imageInfo.rotationDegrees)

                barcodeScanner.process(imageToProcess)
                    .addOnSuccessListener { barcodes ->
                        if (barcodes.isNotEmpty()) {
                            onBarcodeDetected(barcodes)
                        } else {
                            Log.d("CameraBarCodeScanner", "analyze: No barcode Scanned")
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.d("CameraBarCodeScanner", "BarcodeAnalyser: Something went wrong $exception")
                    }
                    .addOnCompleteListener {
                        image.close()
                    }
            }
            lastAnalyzedTimeStamp = currentTimestamp
        } else {
            image.close()
        }
    }
}