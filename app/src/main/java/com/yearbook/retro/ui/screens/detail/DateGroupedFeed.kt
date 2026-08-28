package com.yearbook.retro.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yearbook.retro.data.model.PhotoEntry
import com.yearbook.retro.media.DateStampRenderer
import com.yearbook.retro.ui.components.PolaroidPhotoCard
import com.yearbook.retro.ui.theme.AntiqueBorder
import com.yearbook.retro.ui.theme.DarkSepiaText
import com.yearbook.retro.ui.theme.DateStampAmber
import com.yearbook.retro.ui.theme.ElegantTypewriter
import com.yearbook.retro.ui.theme.MutedSepiaText
import com.yearbook.retro.ui.theme.ParchmentCardSurface
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun DateGroupedFeed(
    dateIndex: Int,
    dateString: String,
    photos: List<PhotoEntry>,
    onPhotoClick: (PhotoEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val formattedDateHeader = try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val parsed = parser.parse(dateString)
        if (parsed != null) DateStampRenderer.formatDateForHeader(parsed.time) else dateString.uppercase()
    } catch (e: Exception) {
        dateString.uppercase()
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Date Section Header Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(ParchmentCardSurface)
                .border(1.dp, AntiqueBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Date",
                        tint = DateStampAmber,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = "DAY $dateIndex: $formattedDateHeader",
                        fontFamily = ElegantTypewriter,
                        fontSize = 12.sp,
                        color = DarkSepiaText,
                        letterSpacing = 0.6.sp
                    )
                }

                Text(
                    text = "${photos.size} ${if (photos.size == 1) "MEMORY" else "MEMORIES"}",
                    fontFamily = ElegantTypewriter,
                    fontSize = 11.sp,
                    color = MutedSepiaText
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2-Column Grid Layout for Multiple Polaroid Photos
        val photoRows = photos.chunked(2)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            photoRows.forEach { rowPhotos ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowPhotos.forEach { photo ->
                        Box(modifier = Modifier.weight(1f)) {
                            PolaroidPhotoCard(
                                photo = photo,
                                onClick = { onPhotoClick(photo) }
                            )
                        }
                    }
                    // Fill remaining column if odd count to keep clean grid alignment
                    if (rowPhotos.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
