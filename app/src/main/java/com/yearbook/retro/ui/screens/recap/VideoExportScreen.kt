package com.yearbook.retro.ui.screens.recap

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.yearbook.retro.ui.theme.SaddleLeather
import com.yearbook.retro.ui.theme.WaxSealGreen
import com.yearbook.retro.ui.theme.WaxSealRed

@Composable
fun VideoExportScreen(
    viewModel: RecapViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ParchmentBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = DarkSepiaText
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "ON-DEVICE REEL EXPORTER",
                        fontFamily = ElegantTypewriter,
                        fontSize = 11.sp,
                        color = MutedSepiaText,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "Export Video Reel",
                        fontFamily = InternetFriends,
                        fontSize = 26.sp,
                        color = DarkSepiaText
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main Action Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(ParchmentCardSurface)
                    .border(1.5.dp, AntiqueBorder, RoundedCornerShape(16.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.exportedVideoUri != null) {
                        // Success State
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(WaxSealGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Ready",
                                tint = ParchmentBackground,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "1080p MP4 Ready!",
                            fontFamily = InternetFriends,
                            fontSize = 28.sp,
                            color = DarkSepiaText
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Your reel with nostalgic sepia LUT filter and acoustic soundtrack has been saved to your device Movies gallery.",
                            fontFamily = ElegantTypewriter,
                            fontSize = 12.sp,
                            color = MutedSepiaText,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        LeatherButton(
                            text = "SHARE TO STORIES / WHATSAPP",
                            onClick = {
                                viewModel.shareExportedVideo(context)
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = GoldFoilLight,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )

                    } else if (uiState.isExporting) {
                        // In Progress State
                        CircularProgressIndicator(
                            color = DateStampAmber,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(48.dp)
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = "Developing Video Reel...",
                            fontFamily = InternetFriends,
                            fontSize = 26.sp,
                            color = DarkSepiaText
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = uiState.exportStatusMessage,
                            fontFamily = ElegantTypewriter,
                            fontSize = 12.sp,
                            color = MutedSepiaText,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        LinearProgressIndicator(
                            progress = { uiState.exportProgress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = DateStampAmber,
                            trackColor = AntiqueBorder.copy(alpha = 0.4f)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${uiState.exportProgress}%",
                            fontFamily = ElegantTypewriter,
                            fontSize = 12.sp,
                            color = DateStampAmber
                        )

                    } else {
                        // Initial State
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(SaddleLeather)
                                .border(2.dp, GoldFoil, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Movie,
                                contentDescription = "Video Reel",
                                tint = GoldFoilLight,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = uiState.yearbook?.title ?: "Yearbook Reel",
                            fontFamily = InternetFriends,
                            fontSize = 28.sp,
                            color = DarkSepiaText,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Render a 1080p MP4 recap video combining all ${uiState.photos.size} collaborator memories with smooth 2.5s slide intervals, warm sepia film LUT, and bundled nostalgic acoustic soundtrack.",
                            fontFamily = ElegantTypewriter,
                            fontSize = 12.sp,
                            color = MutedSepiaText,
                            textAlign = TextAlign.Center
                        )

                        if (uiState.exportError != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = uiState.exportError!!,
                                fontFamily = ElegantTypewriter,
                                fontSize = 12.sp,
                                color = WaxSealRed,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        LeatherButton(
                            text = "RENDER 1080P REEL",
                            onClick = {
                                viewModel.startVideoExport(context)
                            },
                            enabled = uiState.photos.isNotEmpty()
                        )
                    }
                }
            }
        }
    }
}
