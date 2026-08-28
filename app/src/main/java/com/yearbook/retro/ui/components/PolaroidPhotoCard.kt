package com.yearbook.retro.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.yearbook.retro.data.model.PhotoEntry
import com.yearbook.retro.ui.theme.AntiqueBorder
import com.yearbook.retro.ui.theme.DarkSepiaText
import com.yearbook.retro.ui.theme.DateStampAmber
import com.yearbook.retro.ui.theme.ElegantTypewriter
import com.yearbook.retro.ui.theme.InternetFriends
import com.yearbook.retro.ui.theme.MutedSepiaText
import com.yearbook.retro.ui.theme.PolaroidPaper
import com.yearbook.retro.ui.theme.SaddleLeather

/**
 * Compact Vintage Polaroid Card designed for 2-column grid layouts.
 * Features realistic photo borders, amber LED date stamps, author tags, and handwritten notes.
 */
@Composable
fun PolaroidPhotoCard(
    photo: PhotoEntry,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 5.dp, shape = RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(PolaroidPaper)
            .border(1.dp, AntiqueBorder.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(6.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Photo Frame with Amber Timestamp
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color(0xFF28231E))
            ) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(photo.photoUrl)
                        .crossfade(300)
                        .build(),
                    contentDescription = photo.caption,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = DateStampAmber,
                                strokeWidth = 2.dp
                            )
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Photo",
                                tint = MutedSepiaText.copy(alpha = 0.5f),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                )

                // Vintage Amber Date Stamp on bottom-right corner
                val rawDate = photo.dateString.replace("-", " ")
                val shortYear = if (rawDate.length >= 4) rawDate.substring(2) else rawDate
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(3.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "'$shortYear",
                        fontFamily = ElegantTypewriter,
                        fontSize = 9.sp,
                        color = DateStampAmber,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Author Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(SaddleLeather),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = photo.authorName.take(1).uppercase(),
                        fontFamily = ElegantTypewriter,
                        fontSize = 9.sp,
                        color = PolaroidPaper
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = photo.authorName,
                    fontFamily = ElegantTypewriter,
                    fontSize = 11.sp,
                    color = MutedSepiaText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Handwritten Caption in Internet Friends font
            if (photo.caption.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = photo.caption,
                    fontFamily = InternetFriends,
                    fontSize = 15.sp,
                    color = DarkSepiaText,
                    lineHeight = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 1.dp)
                )
            }
        }
    }
}
