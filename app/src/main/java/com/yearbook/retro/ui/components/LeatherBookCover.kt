package com.yearbook.retro.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yearbook.retro.data.model.DailyDropStatus
import com.yearbook.retro.data.model.Yearbook
import com.yearbook.retro.media.DateStampRenderer
import com.yearbook.retro.ui.theme.ElegantTypewriter
import com.yearbook.retro.ui.theme.GoldFoil
import com.yearbook.retro.ui.theme.GoldFoilLight
import com.yearbook.retro.ui.theme.InternetFriends
import com.yearbook.retro.ui.theme.LeatherDark
import com.yearbook.retro.ui.theme.LeatherMedium
import com.yearbook.retro.ui.theme.ParchmentBackground
import com.yearbook.retro.ui.theme.SaddleLeather

@Composable
fun LeatherBookCover(
    yearbook: Yearbook,
    status: DailyDropStatus? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isGrid: Boolean = false
) {
    val coverBrush = Brush.horizontalGradient(
        colors = listOf(
            LeatherDark,
            LeatherMedium,
            SaddleLeather,
            LeatherMedium
        )
    )

    val heightDp = if (isGrid) 200.dp else 190.dp
    val spineWidth = if (isGrid) 10.dp else 18.dp
    val startInnerPad = if (isGrid) 14.dp else 26.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(topEnd = 14.dp, bottomEnd = 14.dp, topStart = 4.dp, bottomStart = 4.dp)
            )
            .clip(RoundedCornerShape(topEnd = 14.dp, bottomEnd = 14.dp, topStart = 4.dp, bottomStart = 4.dp))
            .background(coverBrush)
            .clickable(onClick = onClick)
    ) {
        // Spine accent shadow on the left
        Box(
            modifier = Modifier
                .width(spineWidth)
                .fillMaxHeight()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.45f),
                            Color.Black.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Spine stitched ridge
        Box(
            modifier = Modifier
                .padding(start = spineWidth)
                .width(2.dp)
                .fillMaxHeight()
                .background(Color.Black.copy(alpha = 0.3f))
        )

        // Inner Embossed Gold Stitching Box
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = startInnerPad, end = if (isGrid) 8.dp else 12.dp, top = 8.dp, bottom = 8.dp)
                .border(
                    width = 1.dp,
                    color = GoldFoil.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = if (isGrid) 8.dp else 16.dp, vertical = if (isGrid) 8.dp else 12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top decorative accent spacer or Sealed Badge
                if (yearbook.isAlbumSealed) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF8A1C14))
                            .border(1.dp, GoldFoil, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "★ SEALED • EXPORT READY ★",
                            fontFamily = ElegantTypewriter,
                            fontSize = 8.sp,
                            color = GoldFoilLight,
                            letterSpacing = 0.5.sp
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(2.dp))
                }

                // Center Title: Handwritten / Embossed in Gold Foil
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = yearbook.title,
                        fontFamily = InternetFriends,
                        fontSize = if (isGrid) 18.sp else 24.sp,
                        color = GoldFoilLight,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = if (isGrid) 22.sp else 28.sp
                    )
                    if (!isGrid && yearbook.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = yearbook.description,
                            fontFamily = ElegantTypewriter,
                            fontSize = 11.sp,
                            color = ParchmentBackground.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Bottom Metadata: Date & Friend Count
                if (isGrid) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${yearbook.memberIds.size} COLLABORATORS",
                            fontFamily = ElegantTypewriter,
                            fontSize = 9.sp,
                            color = GoldFoil.copy(alpha = 0.9f),
                            letterSpacing = 0.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val startStr = DateStampRenderer.formatDateForBadge(yearbook.startDate)
                        val endStr = DateStampRenderer.formatDateForBadge(yearbook.endDate)
                        Text(
                            text = "$startStr – $endStr",
                            fontFamily = ElegantTypewriter,
                            fontSize = 11.sp,
                            color = GoldFoil.copy(alpha = 0.9f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = "${yearbook.memberIds.size} FRIENDS",
                            fontFamily = ElegantTypewriter,
                            fontSize = 11.sp,
                            color = GoldFoil.copy(alpha = 0.9f),
                            letterSpacing = 0.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
