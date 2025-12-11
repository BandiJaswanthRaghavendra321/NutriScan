package uk.ac.tees.mad.nutriscan.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import uk.ac.tees.mad.nutriscan.R
import uk.ac.tees.mad.nutriscan.data.local.BiometricPrefs
import uk.ac.tees.mad.nutriscan.data.remote.model.ApiResponse
import uk.ac.tees.mad.nutriscan.data.remote.model.Product
import uk.ac.tees.mad.nutriscan.ui.theme.GreenDark
import uk.ac.tees.mad.nutriscan.ui.theme.GreenLight
import uk.ac.tees.mad.nutriscan.ui.theme.GreenPrimary
import uk.ac.tees.mad.nutriscan.ui.theme.White
import uk.ac.tees.mad.nutriscan.ui.viewmodels.MainVM
import uk.ac.tees.mad.nutriscan.utils.BiometricHelper
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainVM,
    navController: NavController,
    onBarcodeScanned: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val productDetails by viewModel.product.collectAsState()
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

    LaunchedEffect(barcodeValue) {
        if (barcodeValue != null) {
            viewModel.fetchApiData(barcodeValue!!)
            Log.d("Barcode", barcodeValue!!)
        }
    }
    var biometricEnabled by remember { mutableStateOf(false) }
    var locked by remember { mutableStateOf(true) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }
    LaunchedEffect(Unit) {
        biometricEnabled = BiometricPrefs.isEnabled(context)
        locked = biometricEnabled
        if (!hasCameraPermission){
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    if (locked && biometricEnabled) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(GreenLight),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Fingerprint,
                    contentDescription = null,
                    tint = GreenPrimary,
                    modifier = Modifier.size(80.dp)
                )
                Spacer(Modifier.height(12.dp))
                Text("App Locked", color = GreenDark)
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (BiometricHelper.isBiometricAvailable(context)) {
                            BiometricHelper.showBiometricPrompt(
                                context,
                                onSuccess = { locked = false },
                                onError = { snackbarMessage = it }
                            )
                        } else snackbarMessage = "Biometric not available"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Unlock", color = White)
                }
            }
        }
    } else {

        Scaffold(
            containerColor = GreenLight,
            bottomBar = {
                BottomBar(navController = navController)
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Scan a Product 🍎",
                    style = MaterialTheme.typography.titleMedium,
                    color = GreenPrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(GreenPrimary.copy(alpha = 0.05f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (hasCameraPermission) {
                        AndroidView(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(20.dp)),
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
                                                        Log.e(
                                                            "ScanScreen",
                                                            "Barcode scan failed",
                                                            it
                                                        )
                                                    }
                                                    .addOnCompleteListener { imageProxy.close() }
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

                        Image(
                            painter = painterResource(R.drawable.barcode),
                            contentDescription = "Scan overlay",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(140.dp),
                            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(GreenPrimary)
                        )
                    } else {
                        Text(
                            text = "Camera permission not granted",
                            color = GreenPrimary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when (productDetails) {
                            is ApiResponse.Initial -> {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    barcodeValue?.let {
                                        Text(
                                            text = "Scanned Code: $it",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = GreenPrimary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        "Scan a barcode to view details",
                                        color = Color.Gray
                                    )
                                }
                            }

                            is ApiResponse.Loading -> {
                                CircularProgressIndicator(color = GreenPrimary)
                            }

                            is ApiResponse.Success -> {
                                val product = (productDetails as ApiResponse.Success<Product>).data
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Top,
                                    modifier = Modifier.padding(16.dp).clickable {
                                        navController.navigate(
                                            NutriNavigationComp.Details.withArgs(
                                                barcodeValue!!
                                            )
                                        )
                                    }
                                ) {
                                    product.image_url?.let {
                                        Image(
                                            painter = rememberAsyncImagePainter(it),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(120.dp)
                                                .clip(RoundedCornerShape(16.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                    }

                                    Text(
                                        text = product.product_name ?: "Unknown Product",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = GreenPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Brand: ${product.brands ?: "Not available"}",
                                        color = Color.Gray,
                                        style = MaterialTheme.typography.bodySmall
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    product.nutriments?.let { n ->
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = GreenPrimary.copy(
                                                    alpha = 0.05f
                                                )
                                            ),
                                            shape = RoundedCornerShape(16.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp)) {
                                                Text(
                                                    "Nutrition per 100g:",
                                                    fontWeight = FontWeight.Bold,
                                                    color = GreenPrimary
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text("Energy: ${n.energy_kcal_100g ?: 0} kcal")
                                                Text("Fat: ${n.fat_100g ?: 0} g (Saturated: ${n.saturated_fat_100g ?: 0} g)")
                                                Text("Carbs: ${n.carbohydrates_100g ?: 0} g (Sugars: ${n.sugars_100g ?: 0} g)")
                                                Text("Proteins: ${n.proteins_100g ?: 0} g")
                                                Text("Fiber: ${n.fiber_100g ?: 0} g")
                                                Text("Salt: ${n.salt_100g ?: 0} g")
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    product.nutriscore_grade?.let {
                                        Text(
                                            text = "NutriScore: ${it.uppercase()}",
                                            color = GreenPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            is ApiResponse.Error -> {
                                val message = (productDetails as ApiResponse.Error).message
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Error: $message",
                                        color = Color.Red,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                    Button(
                                        onClick = { barcodeValue?.let { viewModel.fetchApiData(it) } },
                                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Text("Retry", color = White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "NutriScan – Home / Scanner")
@Composable
fun NutriScanHomePreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE8F5E9)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(60.dp))

        Text(
            text = "Scan a Product",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E7D32)
        )

        Spacer(Modifier.height(32.dp))

        // Camera Preview Placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(320.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF4CAF50).copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(80.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Point camera at barcode",
                    color = Color(0xFF2E7D32),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Barcode overlay
            Image(
                painter = painterResource(id = R.drawable.barcode), // if you have it, otherwise remove
                contentDescription = null,
                modifier = Modifier
                    .size(160.dp)
                    .align(Alignment.Center),
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color(0xFF4CAF50))
            )
        }

        Spacer(Modifier.height(40.dp))

        // Scanned Product Card
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Lindt Excellence 70% Cocoa",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B5E20),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFE8F5E9)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("CHOCOLATE", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(16.dp))

                Text("Brand: Lindt", color = Color(0xFF757575))
                Text("Scanned: 17 Sep 2025", color = Color(0xFF757575))

                Spacer(Modifier.height(20.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50).copy(0.1f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Nutrition per 100g:", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                        Spacer(Modifier.height(8.dp))
                        Text("580 kcal • Fat: 42g • Sugars: 28g")
                        Text("Protein: 7g • Salt: 0.1g")
                    }
                }

                Spacer(Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF7DCEA0)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("B", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(100.dp)) // For bottom nav
    }
}