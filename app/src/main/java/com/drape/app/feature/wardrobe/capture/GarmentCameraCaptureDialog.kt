package com.drape.app.feature.wardrobe.capture

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Cameraswitch
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FlashAuto
import androidx.compose.material.icons.rounded.FlashOff
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.drape.app.core.data.storage.GarmentImageStorageManager
import com.drape.app.core.designsystem.component.GlassElevation
import com.drape.app.core.designsystem.component.LiquidGlassEmptyStateCard
import com.drape.app.core.designsystem.component.LiquidGlassIconButton
import com.drape.app.core.designsystem.component.LiquidGlassPrimaryButton
import com.drape.app.core.designsystem.component.LiquidGlassSecondaryButton
import com.drape.app.core.designsystem.component.LiquidGlassSurface
import com.drape.app.core.designsystem.theme.DrapeThemeTokens
import kotlinx.coroutines.launch

/**
 * Full-screen native CameraX viewfinder & garment capture dialog.
 * Handles runtime CAMERA permission, lens switching, flash modes, garment framing guide,
 * app-private JPEG persistence, and direct Photo Picker fallback.
 */
@Composable
fun GarmentCameraCaptureDialog(
    onPhotoCaptured: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val glassColors = DrapeThemeTokens.glassColors
    val scope = rememberCoroutineScope()

    val storageManager = remember(context) {
        GarmentImageStorageManager(context.applicationContext)
    }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    var flashMode by remember { mutableIntStateOf(ImageCapture.FLASH_MODE_OFF) }
    var isCapturing by remember { mutableStateOf(false) }
    var cameraErrorText by remember { mutableStateOf<String?>(null) }

    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { pickedUri: Uri? ->
        if (pickedUri != null) {
            isCapturing = true
            scope.launch {
                val savedUri = storageManager.importAndOptimizeImage(pickedUri) ?: pickedUri.toString()
                isCapturing = false
                onPhotoCaptured(savedUri)
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(flashMode) {
        imageCapture.flashMode = flashMode
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(glassColors.canvasBackground)
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Camera Controls Bar
                LiquidGlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    elevation = GlassElevation.Floating
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "GARMENT STUDIO CAMERA",
                                style = DrapeThemeTokens.editorialTypography.eyebrowUpper,
                                color = glassColors.accentPrimary
                            )
                            Text(
                                text = "Center piece inside the frame",
                                style = MaterialTheme.typography.bodySmall,
                                color = glassColors.textSecondary
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            LiquidGlassIconButton(
                                icon = when (flashMode) {
                                    ImageCapture.FLASH_MODE_ON -> Icons.Rounded.FlashOn
                                    ImageCapture.FLASH_MODE_AUTO -> Icons.Rounded.FlashAuto
                                    else -> Icons.Rounded.FlashOff
                                },
                                contentDescription = "Toggle Flash",
                                isActive = flashMode != ImageCapture.FLASH_MODE_OFF,
                                onClick = {
                                    flashMode = when (flashMode) {
                                        ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_AUTO
                                        ImageCapture.FLASH_MODE_AUTO -> ImageCapture.FLASH_MODE_ON
                                        else -> ImageCapture.FLASH_MODE_OFF
                                    }
                                }
                            )
                            LiquidGlassIconButton(
                                icon = Icons.Rounded.Cameraswitch,
                                contentDescription = "Switch Camera Lens",
                                onClick = {
                                    lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                                        CameraSelector.LENS_FACING_FRONT
                                    } else {
                                        CameraSelector.LENS_FACING_BACK
                                    }
                                }
                            )
                            LiquidGlassIconButton(
                                icon = Icons.Rounded.Close,
                                contentDescription = "Close Camera",
                                onClick = onDismiss
                            )
                        }
                    }
                }

                // Center Viewfinder or Permission/Hardware Fallback
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 14.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .border(1.dp, glassColors.specularRimTop, RoundedCornerShape(28.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (hasCameraPermission && cameraErrorText == null) {
                        AndroidView(
                            modifier = Modifier.fillMaxSize(),
                            factory = { ctx ->
                                PreviewView(ctx).apply {
                                    layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                    scaleType = PreviewView.ScaleType.FILL_CENTER
                                }
                            },
                            update = { previewView ->
                                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                                cameraProviderFuture.addListener({
                                    runCatching {
                                        val cameraProvider = cameraProviderFuture.get()
                                        val preview = Preview.Builder().build().also {
                                            it.setSurfaceProvider(previewView.surfaceProvider)
                                        }
                                        val selector = CameraSelector.Builder()
                                            .requireLensFacing(lensFacing)
                                            .build()
                                        cameraProvider.unbindAll()
                                        cameraProvider.bindToLifecycle(
                                            lifecycleOwner,
                                            selector,
                                            preview,
                                            imageCapture
                                        )
                                    }.onFailure { err ->
                                        cameraErrorText = err.localizedMessage ?: "Camera hardware unavailable"
                                    }
                                }, ContextCompat.getMainExecutor(context))
                            }
                        )

                        // Subtle Garment Alignment Frame Overlay
                        val guideColor = glassColors.accentPrimary.copy(alpha = 0.65f)
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val padX = size.width * 0.12f
                            val padY = size.height * 0.10f
                            drawRoundRect(
                                color = guideColor,
                                topLeft = Offset(padX, padY),
                                size = Size(size.width - padX * 2, size.height - padY * 2),
                                cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx()),
                                style = Stroke(
                                    width = 1.5.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(24f, 16f), 0f)
                                )
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            LiquidGlassEmptyStateCard(
                                icon = Icons.Rounded.CameraAlt,
                                eyebrowBadge = if (cameraErrorText != null) "Camera Unavailable" else "Camera Permission",
                                title = if (cameraErrorText != null) {
                                    "Camera Hardware Unavailable"
                                } else {
                                    "Allow Camera Access to Photograph Clothes"
                                },
                                description = cameraErrorText
                                    ?: "Grant camera permission to photograph garments directly, or import existing photos from your gallery using Android Photo Picker.",
                                primaryActionText = "Choose from Gallery",
                                primaryActionIcon = Icons.Rounded.PhotoLibrary,
                                onPrimaryAction = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                secondaryActionText = if (!hasCameraPermission) "Grant Camera Access" else null,
                                secondaryActionIcon = Icons.Rounded.CameraAlt,
                                onSecondaryAction = if (!hasCameraPermission) {
                                    { permissionLauncher.launch(Manifest.permission.CAMERA) }
                                } else null
                            )
                        }
                    }

                    if (isCapturing) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.45f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = glassColors.accentPrimary)
                        }
                    }
                }

                // Bottom Capture & Gallery Action Bar
                LiquidGlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    elevation = GlassElevation.Floating
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LiquidGlassSecondaryButton(
                            text = "Photo Library",
                            leadingIcon = Icons.Rounded.PhotoLibrary,
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        )

                        LiquidGlassPrimaryButton(
                            text = if (isCapturing) "Saving…" else "Capture Piece",
                            leadingIcon = Icons.Rounded.CameraAlt,
                            enabled = hasCameraPermission && cameraErrorText == null && !isCapturing,
                            isLoading = isCapturing,
                            onClick = {
                                isCapturing = true
                                val tempFile = storageManager.createTemporaryCameraCaptureFile()
                                val outputOptions = ImageCapture.OutputFileOptions.Builder(tempFile).build()
                                imageCapture.takePicture(
                                    outputOptions,
                                    ContextCompat.getMainExecutor(context),
                                    object : ImageCapture.OnImageSavedCallback {
                                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                            scope.launch {
                                                val savedUri = storageManager.persistCameraCaptureFile(tempFile)
                                                    ?: Uri.fromFile(tempFile).toString()
                                                isCapturing = false
                                                onPhotoCaptured(savedUri)
                                            }
                                        }

                                        override fun onError(exception: ImageCaptureException) {
                                            isCapturing = false
                                            cameraErrorText = exception.localizedMessage ?: "Capture failed"
                                        }
                                    }
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
