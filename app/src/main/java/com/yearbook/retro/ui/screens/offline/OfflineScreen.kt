package com.yearbook.retro.ui.screens.offline

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SignalCellularConnectedNoInternet0Bar
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yearbook.retro.ui.components.LeatherButton
import com.yearbook.retro.ui.theme.AntiqueBorder
import com.yearbook.retro.ui.theme.DarkSepiaText
import com.yearbook.retro.ui.theme.DateStampAmber
import com.yearbook.retro.ui.theme.ElegantTypewriter
import com.yearbook.retro.ui.theme.GoldFoil
import com.yearbook.retro.ui.theme.InternetFriends
import com.yearbook.retro.ui.theme.MutedSepiaText
import com.yearbook.retro.ui.theme.ParchmentBackground
import com.yearbook.retro.ui.theme.ParchmentCardSurface
import com.yearbook.retro.ui.theme.SaddleLeather
import com.yearbook.retro.ui.theme.WaxSealRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OfflineScreen(
    onRetry: suspend () -> Boolean,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var isChecking by remember { mutableStateOf(false) }
    var showFailMessage by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ParchmentBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Retro Signal Antenna / Wifi Icon with amber glow
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .shadow(8.dp, CircleShape)
                    .clip(CircleShape)
                    .background(ParchmentCardSurface)
                    .border(2.dp, GoldFoil, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.WifiOff,
                    contentDescription = "No Network",
                    tint = WaxSealRed,
                    modifier = Modifier
                        .size(44.dp)
                        .alpha(alphaAnim)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main Title
            Text(
                text = "You are currently offline",
                fontFamily = InternetFriends,
                fontSize = 32.sp,
                color = DarkSepiaText,
                textAlign = TextAlign.Center,
                lineHeight = 36.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Decorative date stamp badge
            Box(
                modifier = Modifier
                    .background(SaddleLeather, RoundedCornerShape(6.dp))
                    .border(1.dp, GoldFoil.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "NO CONNECTION DETECTED",
                    fontFamily = ElegantTypewriter,
                    fontSize = 10.sp,
                    color = DateStampAmber,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Informational Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(ParchmentCardSurface)
                    .border(1.dp, AntiqueBorder, RoundedCornerShape(12.dp))
                    .padding(18.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Retro Yearbook requires an active internet connection to securely sync your albums and daily polaroids with friends.",
                        fontFamily = ElegantTypewriter,
                        fontSize = 12.sp,
                        color = DarkSepiaText,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Please turn on Mobile Data or connect to Wi-Fi in your device settings, then tap reload below.",
                        fontFamily = ElegantTypewriter,
                        fontSize = 11.sp,
                        color = MutedSepiaText,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (showFailMessage) {
                Text(
                    text = "Still unable to connect. Please check your data settings and try again.",
                    fontFamily = ElegantTypewriter,
                    fontSize = 11.sp,
                    color = WaxSealRed,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            // Retry Button
            LeatherButton(
                text = "RETRY CONNECTION",
                onClick = {
                    if (!isChecking) {
                        isChecking = true
                        showFailMessage = false
                        scope.launch {
                            delay(500) // gentle feedback delay
                            val success = onRetry()
                            if (!success) {
                                showFailMessage = true
                            }
                            isChecking = false
                        }
                    }
                },
                isLoading = isChecking,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
