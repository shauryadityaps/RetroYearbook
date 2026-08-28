package com.yearbook.retro.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yearbook.retro.ui.theme.DarkSepiaText
import com.yearbook.retro.ui.theme.ElegantTypewriter
import com.yearbook.retro.ui.theme.GoldFoil
import com.yearbook.retro.ui.theme.GoldFoilLight
import com.yearbook.retro.ui.theme.LeatherDark
import com.yearbook.retro.ui.theme.LeatherMedium
import com.yearbook.retro.ui.theme.ParchmentBackground
import com.yearbook.retro.ui.theme.SaddleLeather

@Composable
fun LeatherButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }

    val buttonBrush = if (enabled) {
        Brush.horizontalGradient(
            colors = listOf(
                LeatherDark,
                LeatherMedium,
                SaddleLeather
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                Color(0xFFAFA79E),
                Color(0xFFC0B8AE)
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .shadow(if (enabled) 6.dp else 0.dp, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .background(buttonBrush)
            .border(
                width = 1.dp,
                color = if (enabled) GoldFoil.copy(alpha = 0.6f) else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(
                enabled = enabled && !isLoading,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(8.dp),
                color = GoldFoilLight,
                strokeWidth = 2.5.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                if (leadingIcon != null) {
                    leadingIcon()
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                }
                Text(
                    text = text.uppercase(),
                    fontFamily = ElegantTypewriter,
                    fontSize = 14.sp,
                    color = if (enabled) ParchmentBackground else DarkSepiaText.copy(alpha = 0.5f),
                    letterSpacing = 1.2.sp
                )
            }
        }
    }
}
