package uk.ac.tees.mad.nutriscan.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import uk.ac.tees.mad.nutriscan.ui.theme.GreenPrimary
import uk.ac.tees.mad.nutriscan.ui.theme.White
import java.util.concurrent.Executors
import uk.ac.tees.mad.nutriscan.R
import uk.ac.tees.mad.nutriscan.ui.viewmodels.MainVM


@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel : MainVM,
    onBarcodeScanned: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var barcodeValue by remember { mutableStateOf<String?>(null) }

    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = {
//                    Text(
//                        "NutriScan",
//                        fontWeight = FontWeight.Bold,
//                        color = White
//                    )
//                },
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = GreenPrimary
//                ),
//                actions = {
//                    IconButton(onClick = onNavigateToHistory) {
//                        Icon(
//                            imageVector = Icons.Default.History,
//                            contentDescription = "History",
//                            tint = White
//                        )
//                    }
//                    IconButton(onClick = onNavigateToProfile) {
//                        Icon(
//                            imageVector = Icons.Default.AccountCircle,
//                            contentDescription = "Profile",
//                            tint = White
//                        )
//                    }
//                }
//            )
//        },
        containerColor = White
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .background(GreenPrimary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (hasCameraPermission) {
                    AndroidView(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp)),
                        factory = { ctx ->
                            val previewView = PreviewView(ctx)

                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()

                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }

                                val options = BarcodeScannerOptions.Builder()
                                    .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                                    .build()
                                val scanner = BarcodeScanning.getClient(options)

                                val analysis = ImageAnalysis.Builder().build().also {
                                    it.setAnalyzer(
                                        Executors.newSingleThreadExecutor()
                                    ) { imageProxy ->
                                        val mediaImage = imageProxy.image
                                        if (mediaImage != null) {
                                            val image = InputImage.fromMediaImage(
                                                mediaImage,
                                                imageProxy.imageInfo.rotationDegrees
                                            )
                                            scanner.process(image)
                                                .addOnSuccessListener { barcodes ->
                                                    for (barcode in barcodes) {
                                                        barcode.rawValue?.let { code ->
                                                            barcodeValue = code
                                                            onBarcodeScanned(code)
                                                        }
                                                    }
                                                }
                                                .addOnFailureListener {
                                                    Log.e("ScanScreen", "Barcode scan failed", it)
                                                }
                                                .addOnCompleteListener {
                                                    imageProxy.close()
                                                }
                                        } else imageProxy.close()
                                    }
                                }

                                try {
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        CameraSelector.DEFAULT_BACK_CAMERA,
                                        preview,
                                        analysis
                                    )
                                } catch (e: Exception) {
                                    Log.e("ScanScreen", "Camera binding failed", e)
                                }
                            }, ContextCompat.getMainExecutor(ctx))

                            previewView
                        }
                    )
                    Image(painterResource(R.drawable.barcode), contentDescription = "",
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(GreenPrimary))
                } else {
                    Text("Camera permission not granted", color = GreenPrimary)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            barcodeValue?.let {
                Text(
                    text = "Scanned Code: $it",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GreenPrimary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ElevatedButton(
                    onClick = onNavigateToProfile,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    Text("Profile", color = White)
                }
                ElevatedButton(
                    onClick = onNavigateToHistory,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    Text("History", color = White)
                }
            }
        }
    }
}
