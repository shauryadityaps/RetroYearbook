package com.yearbook.retro.ui.screens.recap

import android.media.MediaPlayer
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.yearbook.retro.R
import com.yearbook.retro.ui.theme.DarkSepiaText
import com.yearbook.retro.ui.theme.DateStampAmber
import com.yearbook.retro.ui.theme.ElegantTypewriter
import com.yearbook.retro.ui.theme.GoldFoilLight
import com.yearbook.retro.ui.theme.InternetFriends
import com.yearbook.retro.ui.theme.MutedSepiaText
import com.yearbook.retro.ui.theme.ParchmentBackground
import com.yearbook.retro.ui.theme.SaddleLeather
import kotlinx.coroutines.delay

@Composable
fun NostalgicSlideshowScreen(
    viewModel: RecapViewModel,
    onClose: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val photos = uiState.photos
    var currentIndex by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(true) }

    // Audio player for nostalgic acoustic loop
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(Unit) {
        try {
            val player = MediaPlayer.create(context, R.raw.nostalgic_acoustic_loop).apply {
                isLooping = true
                start()
            }
            mediaPlayer = player
        } catch (e: Exception) {
            // Audio fallback
        }

        onDispose {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    // Auto-advance slides every 3.5 seconds
    LaunchedEffect(isPlaying, photos.size, currentIndex) {
        if (isPlaying && photos.isNotEmpty()) {
            delay(3500)
            currentIndex = (currentIndex + 1) % photos.size
        }
    }

    // Ken Burns scale animation on ambient background
    val scale by animateFloatAsState(
        targetValue = if (currentIndex % 2 == 0) 1.12f else 1.04f,
        animationSpec = tween(durationMillis = 3500, easing = LinearEasing),
        label = "KenBurnsScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF120E0B))
    ) {
        if (photos.isNotEmpty() && currentIndex < photos.size) {
            val currentPhoto = photos[currentIndex]

            // Fullscreen Transition with Ambient Background and Uncropped Foreground
            AnimatedContent(
                targetState = currentPhoto,
                transitionSpec = {
                    fadeIn(animationSpec = tween(600)) togetherWith fadeOut(animationSpec = tween(600))
                },
                label = "SlideshowCrossfade",
                modifier = Modifier.fillMaxSize()
            ) { photo ->
                Box(modifier = Modifier.fillMaxSize()) {
                    // 1. Ambient Blurred Theater Background (fills space with matching colors)
                    AsyncImage(
                        model = photo.photoUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(scale)
                            .blur(25.dp)
                            .alpha(0.38f)
                    )

                    // Cinematic Vignette Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.80f),
                                        Color(0xFF14100D).copy(alpha = 0.40f),
                                        Color(0xFF14100D).copy(alpha = 0.40f),
                                        Color.Black.copy(alpha = 0.92f)
                                    )
                                )
                            )
                    )

                    // 2. Foreground Center Photo (100% Uncropped, Full Aspect Ratio Preserved)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 16.dp, end = 16.dp, top = 80.dp, bottom = 180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .shadow(20.dp, RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1F1A15))
                                .border(1.dp, GoldFoilLight.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            SubcomposeAsyncImage(
                                model = photo.photoUrl,
                                contentDescription = photo.caption,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.wrapContentSize(),
                                loading = {
                                    Box(
                                        modifier = Modifier.size(120.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            color = DateStampAmber,
                                            strokeWidth = 2.dp,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Top Overlay: Close & Album Name
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = ParchmentBackground
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = uiState.yearbook?.title ?: "Yearbook Recap",
                        fontFamily = InternetFriends,
                        fontSize = 24.sp,
                        color = GoldFoilLight
                    )
                    Text(
                        text = "SLIDE ${currentIndex + 1} OF ${photos.size}",
                        fontFamily = ElegantTypewriter,
                        fontSize = 11.sp,
                        color = ParchmentBackground.copy(alpha = 0.8f),
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.size(48.dp))
            }

            // Bottom Overlay: Amber Date Stamp, Caption, and Player Controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Amber 7-segment Date Stamp
                val rawDate = currentPhoto.dateString.replace("-", " ")
                val shortYear = if (rawDate.length >= 4) rawDate.substring(2) else rawDate
                Box(
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "'$shortYear • ${currentPhoto.authorName.uppercase()}",
                        fontFamily = ElegantTypewriter,
                        fontSize = 12.sp,
                        color = DateStampAmber,
                        letterSpacing = 1.2.sp
                    )
                }

                // Handwritten Caption in Internet Friends
                if (currentPhoto.caption.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentPhoto.caption,
                        fontFamily = InternetFriends,
                        fontSize = 22.sp,
                        color = ParchmentBackground,
                        textAlign = TextAlign.Center,
                        lineHeight = 26.sp,
                        maxLines = 2
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { (currentIndex + 1).toFloat() / photos.size.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = GoldFoilLight,
                    trackColor = Color.White.copy(alpha = 0.25f),
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Player Controls: Prev, Play/Pause, Next
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Previous Slide
                    IconButton(
                        onClick = {
                            if (photos.isNotEmpty()) {
                                currentIndex = if (currentIndex - 1 < 0) photos.size - 1 else currentIndex - 1
                            }
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous Slide",
                            tint = ParchmentBackground,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Play/Pause Toggle
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .background(SaddleLeather)
                            .border(1.5.dp, GoldFoilLight, CircleShape)
                            .clickable { isPlaying = !isPlaying },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = GoldFoilLight,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Next Slide
                    IconButton(
                        onClick = {
                            if (photos.isNotEmpty()) {
                                currentIndex = (currentIndex + 1) % photos.size
                            }
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next Slide",
                            tint = ParchmentBackground,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        } else {
            // Empty State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No Memories to Recap",
                        fontFamily = InternetFriends,
                        fontSize = 28.sp,
                        color = GoldFoilLight
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Drop your first photo in this album to watch the nostalgic musical slideshow.",
                        fontFamily = ElegantTypewriter,
                        fontSize = 12.sp,
                        color = ParchmentBackground.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.background(SaddleLeather, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Back",
                            tint = GoldFoilLight
                        )
                    }
                }
            }
        }
    }
}
