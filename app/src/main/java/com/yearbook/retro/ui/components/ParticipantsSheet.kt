package com.yearbook.retro.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.yearbook.retro.data.model.PhotoEntry
import com.yearbook.retro.data.model.User
import com.yearbook.retro.data.model.Yearbook
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
import com.yearbook.retro.ui.theme.WaxSealGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParticipantsSheet(
    yearbook: Yearbook,
    members: List<User>,
    photos: List<PhotoEntry>,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val clipboardManager = LocalClipboardManager.current
    var copiedCode by remember { mutableStateOf(false) }

    // Calculate memory counts per user
    val photoCountByMember = remember(photos) {
        photos.groupingBy { it.authorId }.eachCount()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ParchmentBackground,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SaddleLeather),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = "Participants",
                            tint = GoldFoilLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "ALBUM PARTICIPANTS",
                            fontFamily = ElegantTypewriter,
                            fontSize = 10.sp,
                            color = MutedSepiaText,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = "${members.size} Collaborator${if (members.size != 1) "s" else ""}",
                            fontFamily = InternetFriends,
                            fontSize = 24.sp,
                            color = DarkSepiaText
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = DarkSepiaText
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Invite Code Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(ParchmentCardSurface)
                    .border(1.dp, GoldFoil.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                    .clickable {
                        clipboardManager.setText(AnnotatedString(yearbook.joinCode))
                        copiedCode = true
                    }
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "INVITE MORE FRIENDS",
                            fontFamily = ElegantTypewriter,
                            fontSize = 9.sp,
                            color = MutedSepiaText,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (copiedCode) "Code copied to clipboard!" else "Tap to copy code: ${yearbook.joinCode}",
                            fontFamily = ElegantTypewriter,
                            fontSize = 11.sp,
                            color = if (copiedCode) WaxSealGreen else DarkSepiaText
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SaddleLeather)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = yearbook.joinCode,
                                fontFamily = ElegantTypewriter,
                                fontSize = 12.sp,
                                color = GoldFoilLight,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = GoldFoilLight,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(
                color = AntiqueBorder.copy(alpha = 0.5f),
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Member List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 340.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(members, key = { it.uid }) { member ->
                    val isOwner = member.uid == yearbook.ownerId
                    val memoryCount = photoCountByMember[member.uid] ?: 0

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(ParchmentCardSurface)
                            .border(1.dp, AntiqueBorder.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Avatar & Name Info
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (member.photoUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = member.photoUrl,
                                        contentDescription = member.displayName,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .border(1.dp, if (isOwner) GoldFoil else SaddleLeather, CircleShape)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(if (isOwner) SaddleLeather else SaddleLeather.copy(alpha = 0.8f))
                                            .border(1.dp, if (isOwner) GoldFoil else AntiqueBorder, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = member.displayName.take(1).uppercase(),
                                            fontFamily = ElegantTypewriter,
                                            fontSize = 14.sp,
                                            color = if (isOwner) GoldFoilLight else PolaroidPaper
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = member.displayName,
                                            fontFamily = ElegantTypewriter,
                                            fontSize = 13.sp,
                                            color = DarkSepiaText,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (isOwner) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(GoldFoil.copy(alpha = 0.2f))
                                                    .border(0.5.dp, GoldFoil, RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Star,
                                                        contentDescription = "Owner",
                                                        tint = GoldFoil,
                                                        modifier = Modifier.size(9.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(2.dp))
                                                    Text(
                                                        text = "CREATOR",
                                                        fontFamily = ElegantTypewriter,
                                                        fontSize = 8.sp,
                                                        color = DarkSepiaText,
                                                        letterSpacing = 0.5.sp
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    if (member.email.isNotBlank()) {
                                        Text(
                                            text = member.email,
                                            fontFamily = ElegantTypewriter,
                                            fontSize = 10.sp,
                                            color = MutedSepiaText,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            // Memories Contribution Count
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "$memoryCount",
                                    fontFamily = InternetFriends,
                                    fontSize = 18.sp,
                                    color = DarkSepiaText
                                )
                                Text(
                                    text = "memories",
                                    fontFamily = ElegantTypewriter,
                                    fontSize = 9.sp,
                                    color = MutedSepiaText
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
