package com.project.zariya.feature.scanner.presentation

import android.Manifest
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.project.zariya.core.ui.components.EmptyState
import com.project.zariya.core.ui.components.ZariyaPrimaryButton
import com.project.zariya.core.ui.theme.ZariyaBackground
import com.project.zariya.core.ui.theme.ZariyaPrimary
import com.project.zariya.core.ui.theme.ZariyaSurface
import com.project.zariya.core.ui.theme.ZariyaSurfaceElevated
import com.project.zariya.core.ui.theme.ZariyaTextPrimary
import com.project.zariya.core.ui.theme.ZariyaTextSecondary
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    onNavigateBack: () -> Unit,
    onNavigateToResults: () -> Unit,
    viewModel: ScannerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var hasCameraPermission by remember { mutableStateOf(false) }
    var isFlashOn by remember { mutableStateOf(false) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Camera permission is required to scan prescriptions", Toast.LENGTH_LONG).show()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onImageCaptured(it) }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    LaunchedEffect(uiState.rawText) {
        if (uiState.rawText.isNotBlank() && !uiState.isProcessing) {
            onNavigateToResults()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Scan Prescription",
                        color = ZariyaTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ZariyaTextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { isFlashOn = !isFlashOn }) {
                        Icon(
                            imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = if (isFlashOn) "Turn off flash" else "Turn on flash",
                            tint = ZariyaTextPrimary
                        )
                    }
                    IconButton(onClick = { galleryLauncher.launch("image/*") }) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "Pick from gallery",
                            tint = ZariyaTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ZariyaSurfaceElevated
                )
            )
        },
        containerColor = ZariyaBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (hasCameraPermission) {
                CameraPreviewContent(
                    isFlashOn = isFlashOn,
                    isProcessing = uiState.isProcessing,
                    error = uiState.error,
                    onImageCaptureReady = { capture -> imageCapture = capture },
                    onCaptureClick = {
                        imageCapture?.let { capture ->
                            captureImage(context, capture) { uri ->
                                viewModel.onImageCaptured(uri)
                            }
                        }
                    },
                    onRetryClick = { viewModel.retryProcessing() }
                )
            } else {
                CameraPermissionDeniedContent(
                    onRequestPermission = {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    onPickFromGallery = { galleryLauncher.launch("image/*") }
                )
            }
        }
    }
}

@Composable
private fun CameraPreviewContent(
    isFlashOn: Boolean,
    isProcessing: Boolean,
    error: String?,
    onImageCaptureReady: (ImageCapture) -> Unit,
    onCaptureClick: () -> Unit,
    onRetryClick: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .build()
    }

    LaunchedEffect(imageCapture) {
        onImageCaptureReady(imageCapture)
    }

    LaunchedEffect(isFlashOn) {
        imageCapture.flashMode = if (isFlashOn) {
            ImageCapture.FLASH_MODE_ON
        } else {
            ImageCapture.FLASH_MODE_OFF
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture
                        )
                    } catch (_: Exception) {
                        // Camera binding failed
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Scanning overlay frame
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(280.dp)
                .align(Alignment.Center)
                .border(
                    width = 2.dp,
                    color = ZariyaPrimary.copy(alpha = 0.7f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                )
        )

        Text(
            text = "Position the prescription within the frame",
            color = ZariyaTextPrimary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 320.dp)
                .background(
                    color = ZariyaSurface.copy(alpha = 0.7f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Bottom capture controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(ZariyaSurfaceElevated.copy(alpha = 0.9f))
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnimatedVisibility(
                visible = error != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                error?.let { errorMessage ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        Text(
                            text = errorMessage,
                            color = com.project.zariya.core.ui.theme.ZariyaError,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ZariyaPrimaryButton(
                            text = "Retry",
                            onClick = onRetryClick,
                            modifier = Modifier.fillMaxWidth(0.5f)
                        )
                    }
                }
            }

            if (isProcessing) {
                CircularProgressIndicator(
                    color = ZariyaPrimary,
                    modifier = Modifier.size(56.dp),
                    strokeWidth = 3.dp
                )
                Text(
                    text = "Processing prescription...",
                    color = ZariyaTextSecondary,
                    fontSize = 13.sp
                )
            } else {
                IconButton(
                    onClick = onCaptureClick,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(ZariyaPrimary)
                        .border(4.dp, ZariyaTextPrimary.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Capture",
                        tint = com.project.zariya.core.ui.theme.ZariyaTextOnPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CameraPermissionDeniedContent(
    onRequestPermission: () -> Unit,
    onPickFromGallery: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        EmptyState(
            icon = Icons.Default.CameraAlt,
            title = "Camera Permission Required",
            subtitle = "Allow camera access to scan prescriptions, or pick an image from your gallery.",
            action = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ZariyaPrimaryButton(
                        text = "Grant Camera Permission",
                        onClick = onRequestPermission,
                        modifier = Modifier.fillMaxWidth()
                    )
                    ZariyaPrimaryButton(
                        text = "Pick from Gallery",
                        onClick = onPickFromGallery,
                        icon = Icons.Default.PhotoLibrary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }
}

private fun captureImage(
    context: Context,
    imageCapture: ImageCapture,
    onImageCaptured: (Uri) -> Unit
) {
    val photoFile = File(
        context.cacheDir,
        "prescription_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.jpg"
    )

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val savedUri = Uri.fromFile(photoFile)
                onImageCaptured(savedUri)
            }

            override fun onError(exception: ImageCaptureException) {
                Toast.makeText(
                    context,
                    "Failed to capture image: ${exception.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    )
}
