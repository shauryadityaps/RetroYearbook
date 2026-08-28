package com.yearbook.retro.ui.screens.detail

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.yearbook.retro.media.DateStampRenderer
import com.yearbook.retro.media.ImageCompressor
import com.yearbook.retro.ui.components.LeatherButton
import com.yearbook.retro.ui.theme.AntiqueBorder
import com.yearbook.retro.ui.theme.DarkSepiaText
import com.yearbook.retro.ui.theme.DateStampAmber
import com.yearbook.retro.ui.theme.ElegantTypewriter
import com.yearbook.retro.ui.theme.GoldFoil
import com.yearbook.retro.ui.theme.GoldFoilLight
import com.yearbook.retro.ui.theme.InternetFriends
import com.yearbook.retro.ui.theme.MutedSepiaText
import com.yearbook.retro.ui.theme.ParchmentBackground
import com.yearbook.retro.ui.theme.ParchmentCardSurface
import com.yearbook.retro.ui.theme.PolaroidPaper
import com.yearbook.retro.ui.theme.SaddleLeather
import kotlinx.coroutines.launch
import java.io.File

import androidx.compose.runtime.saveable.rememberSaveable

@Composable
fun PhotoDropDialog(
    isUploading: Boolean,
    onDismiss: () -> Unit,
    onConfirmDrop: (bitmap: Bitmap, caption: String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var caption by rememberSaveable { mutableStateOf("") }
    var isProcessingImage by remember { mutableStateOf(false) }
    var currentCameraUriString by rememberSaveable { mutableStateOf<String?>(null) }

    fun processImageUri(uri: Uri) {
        coroutineScope.launch {
            isProcessingImage = true
            val result = ImageCompressor.compressAndStamp(context, uri)
            if (result.isSuccess) {
                selectedBitmap = result.getOrNull()?.second
            } else {
                // Fallback decoding with memory-safe scaling
                try {
                    val fallback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                    } else {
                        @Suppress("DEPRECATION")
                        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                    }
                    selectedBitmap = DateStampRenderer.applyDateStamp(context, fallback)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            isProcessingImage = false
        }
    }

    // 1. Gallery photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            processImageUri(uri)
        }
    }

    // 2. Camera photo capture launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        val uri = currentCameraUriString?.let { Uri.parse(it) } ?: run {
            val tempFile = File(context.cacheDir, "camera_snap_temp.jpg")
            if (tempFile.exists() && tempFile.length() > 0) {
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tempFile)
            } else null
        }

        if (success && uri != null) {
            processImageUri(uri)
        } else if (uri != null) {
            // Some cameras report false on success, verify file length
            val tempFile = File(context.cacheDir, "camera_snap_temp.jpg")
            if (tempFile.exists() && tempFile.length() > 0) {
                processImageUri(uri)
            }
        }
    }

    // 3. Camera permission launcher
    fun launchCamera() {
        try {
            val tempFile = File(context.cacheDir, "camera_snap_temp.jpg")
            if (tempFile.exists()) tempFile.delete()
            tempFile.createNewFile()
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempFile
            )
            currentCameraUriString = uri.toString()
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            launchCamera()
        } else {
            launchCamera()
        }
    }

    fun openCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                launchCamera()
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        } else {
            launchCamera()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = ParchmentCardSurface,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, AntiqueBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TODAY'S MEMORY",
                            fontFamily = ElegantTypewriter,
                            fontSize = 11.sp,
                            color = MutedSepiaText,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Drop Today's Photo",
                            fontFamily = InternetFriends,
                            fontSize = 26.sp,
                            color = DarkSepiaText
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MutedSepiaText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Image Preview or Dual Choice Selector (Camera vs Gallery)
                if (selectedBitmap != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(PolaroidPaper)
                            .border(1.dp, AntiqueBorder, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Image(
                            bitmap = selectedBitmap!!.asImageBitmap(),
                            contentDescription = "Selected memory",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(4.dp))
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    // Retake / Change options row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(ParchmentBackground)
                                .border(1.dp, AntiqueBorder, RoundedCornerShape(6.dp))
                                .clickable { openCamera() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Camera",
                                tint = SaddleLeather,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Retake",
                                fontFamily = ElegantTypewriter,
                                fontSize = 11.sp,
                                color = DarkSepiaText
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(ParchmentBackground)
                                .border(1.dp, AntiqueBorder, RoundedCornerShape(6.dp))
                                .clickable { photoPickerLauncher.launch("image/*") }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = "Gallery",
                                tint = SaddleLeather,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Gallery",
                                fontFamily = ElegantTypewriter,
                                fontSize = 11.sp,
                                color = DarkSepiaText
                            )
                        }
                    }
                } else if (isProcessingImage) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ParchmentBackground)
                            .border(1.dp, AntiqueBorder, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = SaddleLeather,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Applying amber date stamp...",
                                fontFamily = ElegantTypewriter,
                                fontSize = 12.sp,
                                color = DarkSepiaText
                            )
                        }
                    }
                } else {
                    // DUAL TILES: CAMERA ON SPOT vs GALLERY PICKER
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "CHOOSE PHOTO SOURCE",
                            fontFamily = ElegantTypewriter,
                            fontSize = 10.sp,
                            color = SaddleLeather,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // TILE 1: CAMERA (TAKE PHOTO ON SPOT)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(130.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ParchmentBackground)
                                    .border(1.5.dp, SaddleLeather.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                    .clickable { openCamera() }
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(SaddleLeather)
                                            .border(1.dp, GoldFoil, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = "Camera",
                                            tint = GoldFoilLight,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "TAKE PHOTO",
                                        fontFamily = ElegantTypewriter,
                                        fontSize = 11.sp,
                                        color = DarkSepiaText,
                                        letterSpacing = 0.8.sp
                                    )
                                    Text(
                                        text = "On the spot",
                                        fontFamily = ElegantTypewriter,
                                        fontSize = 9.sp,
                                        color = MutedSepiaText
                                    )
                                }
                            }

                            // TILE 2: GALLERY PICKER
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(130.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ParchmentBackground)
                                    .border(1.5.dp, GoldFoil.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                    .clickable { photoPickerLauncher.launch("image/*") }
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(ParchmentCardSurface)
                                            .border(1.dp, SaddleLeather, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PhotoLibrary,
                                            contentDescription = "Gallery",
                                            tint = SaddleLeather,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "FROM GALLERY",
                                        fontFamily = ElegantTypewriter,
                                        fontSize = 11.sp,
                                        color = DarkSepiaText,
                                        letterSpacing = 0.8.sp
                                    )
                                    Text(
                                        text = "Choose existing",
                                        fontFamily = ElegantTypewriter,
                                        fontSize = 9.sp,
                                        color = MutedSepiaText
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Automatic vintage date stamp is stamped onto both camera and gallery memories.",
                            fontFamily = ElegantTypewriter,
                            fontSize = 10.sp,
                            color = MutedSepiaText,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Caption Input
                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    label = {
                        Text(
                            text = "HANDWRITTEN CAPTION",
                            fontFamily = ElegantTypewriter,
                            fontSize = 11.sp,
                            color = MutedSepiaText
                        )
                    },
                    placeholder = {
                        Text(
                            text = "Write a little note about this moment...",
                            fontFamily = InternetFriends,
                            fontSize = 16.sp,
                            color = MutedSepiaText.copy(alpha = 0.5f)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = InternetFriends,
                        fontSize = 20.sp,
                        color = DarkSepiaText
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SaddleLeather,
                        unfocusedBorderColor = AntiqueBorder,
                        focusedContainerColor = ParchmentBackground,
                        unfocusedContainerColor = ParchmentBackground
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                LeatherButton(
                    text = "SEAL & DROP MEMORY",
                    onClick = {
                        if (selectedBitmap != null) {
                            onConfirmDrop(selectedBitmap!!, caption)
                        }
                    },
                    enabled = selectedBitmap != null && !isProcessingImage,
                    isLoading = isUploading || isProcessingImage
                )
            }
        }
    }
}
