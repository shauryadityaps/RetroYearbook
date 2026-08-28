package com.yearbook.retro.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.yearbook.retro.data.model.PhotoEntry
import com.yearbook.retro.media.DateStampRenderer
import com.yearbook.retro.ui.components.LeatherButton
import com.yearbook.retro.ui.components.ParticipantsSheet
import com.yearbook.retro.ui.components.PolaroidPhotoCard
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
import com.yearbook.retro.ui.theme.WaxSealRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsideYearbookScreen(
    viewModel: InsideYearbookViewModel,
    onBack: () -> Unit,
    onOpenSlideshow: (String) -> Unit,
    onOpenVideoExport: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    var showDropDialog by rememberSaveable { mutableStateOf(false) }
    var codeCopiedToast by remember { mutableStateOf(false) }
    var selectedPhoto by remember { mutableStateOf<PhotoEntry?>(null) }
    var showParticipantsDropdown by remember { mutableStateOf(false) }
    var showParticipantsSheet by remember { mutableStateOf(false) }
    var showSealConfirmDialog by remember { mutableStateOf(false) }

    val yearbook = uiState.yearbook
    val isOwner = yearbook?.ownerId == uiState.currentUser?.uid

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ParchmentBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = DarkSepiaText
                        )
                    }
                    Text(
                        text = yearbook?.title ?: "Yearbook",
                        fontFamily = InternetFriends,
                        fontSize = 22.sp,
                        color = DarkSepiaText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Top Action buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // 1. Single Minimalist Participants Button & Dropdown Menu
                    Box {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(ParchmentCardSurface)
                                .border(1.dp, SaddleLeather.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .clickable { showParticipantsDropdown = true }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Group,
                                    contentDescription = "Participants",
                                    tint = SaddleLeather,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${uiState.members.size}",
                                    fontFamily = ElegantTypewriter,
                                    fontSize = 11.sp,
                                    color = DarkSepiaText
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Dropdown",
                                    tint = DarkSepiaText,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }

                        // Participants Dropdown Menu
                        DropdownMenu(
                            expanded = showParticipantsDropdown,
                            onDismissRequest = { showParticipantsDropdown = false },
                            modifier = Modifier
                                .background(ParchmentCardSurface)
                                .border(1.dp, AntiqueBorder, RoundedCornerShape(8.dp))
                        ) {
                            Text(
                                text = "ALBUM COLLABORATORS",
                                fontFamily = ElegantTypewriter,
                                fontSize = 10.sp,
                                color = SaddleLeather,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                            HorizontalDivider(color = AntiqueBorder.copy(alpha = 0.5f))

                            if (uiState.members.isEmpty()) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "Loading members...",
                                            fontFamily = ElegantTypewriter,
                                            fontSize = 12.sp,
                                            color = MutedSepiaText
                                        )
                                    },
                                    onClick = { }
                                )
                            } else {
                                uiState.members.take(6).forEach { member ->
                                    val memberIsOwner = member.uid == yearbook?.ownerId
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = member.displayName.ifBlank { "Friend" },
                                                        fontFamily = ElegantTypewriter,
                                                        fontSize = 12.sp,
                                                        color = DarkSepiaText,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    if (memberIsOwner) {
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = "(OWNER)",
                                                            fontFamily = ElegantTypewriter,
                                                            fontSize = 9.sp,
                                                            color = GoldFoil
                                                        )
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
                                        },
                                        onClick = {
                                            showParticipantsDropdown = false
                                            showParticipantsSheet = true
                                        }
                                    )
                                }
                            }

                            HorizontalDivider(color = AntiqueBorder.copy(alpha = 0.5f))
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Group,
                                            contentDescription = null,
                                            tint = SaddleLeather,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "View Full Member Details",
                                            fontFamily = ElegantTypewriter,
                                            fontSize = 11.sp,
                                            color = SaddleLeather
                                        )
                                    }
                                },
                                onClick = {
                                    showParticipantsDropdown = false
                                    showParticipantsSheet = true
                                }
                            )
                        }
                    }

                    if (!uiState.isCompletedOrArchived) {
                        // WHILE ONGOING: Exactly 2 buttons (RECAP SLIDESHOW & SEAL ALBUM)
                        // Button 1: See Recap Slideshow
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SaddleLeather)
                                .clickable {
                                    if (yearbook != null) onOpenSlideshow(yearbook.id)
                                }
                                .padding(horizontal = 9.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Slideshow",
                                    tint = GoldFoilLight,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "RECAP",
                                    fontFamily = ElegantTypewriter,
                                    fontSize = 11.sp,
                                    color = GoldFoilLight
                                )
                            }
                        }

                        // Button 2: Seal & Complete the Album (Visible strictly to the album creator)
                        if (isOwner) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF8A1C14))
                                    .clickable {
                                        showSealConfirmDialog = true
                                    }
                                    .padding(horizontal = 9.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Seal Album",
                                        tint = GoldFoilLight,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "SEAL ALBUM",
                                        fontFamily = ElegantTypewriter,
                                        fontSize = 11.sp,
                                        color = GoldFoilLight
                                    )
                                }
                            }
                        }
                    } else {
                        // AFTER SEALED & COMPLETE: Direct Quick Export Options (PDF & REEL)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(ParchmentCardSurface)
                                .border(1.dp, GoldFoil, RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.exportScrapbookPdf(context) { }
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = "PDF Scrapbook",
                                    tint = SaddleLeather,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "PDF",
                                    fontFamily = ElegantTypewriter,
                                    fontSize = 11.sp,
                                    color = SaddleLeather
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(DateStampAmber)
                                .clickable {
                                    if (yearbook != null) onOpenVideoExport(yearbook.id)
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Movie,
                                    contentDescription = "Video Reel",
                                    tint = ParchmentBackground,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "REEL",
                                    fontFamily = ElegantTypewriter,
                                    fontSize = 11.sp,
                                    color = ParchmentBackground
                                )
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(AntiqueBorder.copy(alpha = 0.4f))
            )

            // Scrollable Feed
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Album Banner Metadata & Invite Code Card
                if (yearbook != null) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(6.dp, RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .background(ParchmentCardSurface)
                                .border(1.dp, AntiqueBorder, RoundedCornerShape(12.dp))
                                .padding(16.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
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
                                        fontSize = 12.sp,
                                        color = MutedSepiaText
                                    )

                                    // Show Invite Code only when album is ongoing
                                    if (!uiState.isCompletedOrArchived) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(SaddleLeather)
                                                .clickable {
                                                    clipboardManager.setText(AnnotatedString(yearbook.joinCode))
                                                    codeCopiedToast = true
                                                }
                                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "CODE: ${yearbook.joinCode}",
                                                    fontFamily = ElegantTypewriter,
                                                    fontSize = 11.sp,
                                                    color = GoldFoilLight,
                                                    letterSpacing = 1.sp
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Icon(
                                                    imageVector = Icons.Default.ContentCopy,
                                                    contentDescription = "Copy",
                                                    tint = GoldFoilLight,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                            }
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(GoldFoil.copy(alpha = 0.15f))
                                                .border(1.dp, GoldFoil, RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "★ SEALED ARCHIVE",
                                                fontFamily = ElegantTypewriter,
                                                fontSize = 10.sp,
                                                color = SaddleLeather,
                                                letterSpacing = 1.sp
                                            )
                                        }
                                    }
                                }

                                if (codeCopiedToast) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Invite code copied! Share with your friends.",
                                        fontFamily = ElegantTypewriter,
                                        fontSize = 11.sp,
                                        color = Color(0xFF2E7D32)
                                    )
                                }

                                if (yearbook.description.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = yearbook.description,
                                        fontFamily = ElegantTypewriter,
                                        fontSize = 12.sp,
                                        color = DarkSepiaText
                                    )
                                }
                            }
                        }
                    }
                }

                // COMPLETED & SEALED ALBUM ARCHIVE BANNER & 30-DAY RETENTION REMINDER
                item {
                    if (uiState.isCompletedOrArchived) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(8.dp, RoundedCornerShape(14.dp))
                                .clip(RoundedCornerShape(14.dp))
                                .background(ParchmentCardSurface)
                                .border(2.dp, GoldFoil, RoundedCornerShape(14.dp))
                                .padding(18.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Completed",
                                        tint = WaxSealRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "★ ALBUM SEALED & COMPLETED ★",
                                        fontFamily = ElegantTypewriter,
                                        fontSize = 13.sp,
                                        color = SaddleLeather,
                                        letterSpacing = 1.2.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "This album is completed and sealed! You can now generate your nostalgia video reel and download your PDF scrapbook.",
                                    fontFamily = ElegantTypewriter,
                                    fontSize = 12.sp,
                                    color = DarkSepiaText,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // 30-Day Cloud Safety Countdown Badge
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(WaxSealRed.copy(alpha = 0.10f))
                                        .border(1.dp, WaxSealRed.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Shield,
                                            contentDescription = "Retention Policy",
                                            tint = WaxSealRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Cloud Notice: This album will be deleted from the cloud in a month (${uiState.daysUntilCloudDeletion} days remaining) to preserve storage. Save your Video Reel and PDF Scrapbook to your device to keep forever!",
                                            fontFamily = ElegantTypewriter,
                                            fontSize = 11.sp,
                                            color = WaxSealRed,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Dual Primary Export Options: Video Reel & PDF Scrapbook
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // 1. PDF Scrapbook Button
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(SaddleLeather)
                                            .clickable {
                                                viewModel.exportScrapbookPdf(context) { }
                                            }
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Download,
                                                contentDescription = "PDF",
                                                tint = GoldFoilLight,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "GENERATE PDF",
                                                fontFamily = ElegantTypewriter,
                                                fontSize = 11.sp,
                                                color = GoldFoilLight,
                                                letterSpacing = 0.8.sp
                                            )
                                        }
                                    }

                                    // 2. Nostalgia Video Reel Button
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(DateStampAmber)
                                            .clickable {
                                                if (yearbook != null) onOpenVideoExport(yearbook.id)
                                            }
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Movie,
                                                contentDescription = "Video Reel",
                                                tint = ParchmentBackground,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "MAKE REEL",
                                                fontFamily = ElegantTypewriter,
                                                fontSize = 11.sp,
                                                color = ParchmentBackground,
                                                letterSpacing = 0.8.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // 3. Play Slideshow Option
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable {
                                            if (yearbook != null) onOpenSlideshow(yearbook.id)
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play Slideshow",
                                        tint = SaddleLeather,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Play Fullscreen Recap Slideshow",
                                        fontFamily = ElegantTypewriter,
                                        fontSize = 11.sp,
                                        color = SaddleLeather
                                    )
                                }
                            }
                        }
                    } else if (uiState.hasPostedToday) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(WaxSealGreen.copy(alpha = 0.12f))
                                .border(1.5.dp, WaxSealGreen.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .padding(14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Completed",
                                    tint = WaxSealGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "TODAY'S MEMORY IS SEALED",
                                    fontFamily = ElegantTypewriter,
                                    fontSize = 12.sp,
                                    color = WaxSealGreen,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(4.dp, RoundedCornerShape(10.dp))
                                .clip(RoundedCornerShape(10.dp))
                                .background(SaddleLeather)
                                .clickable { showDropDialog = true }
                                .padding(14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AddAPhoto,
                                    contentDescription = "Drop",
                                    tint = GoldFoilLight,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "DROP TODAY'S MEMORY",
                                    fontFamily = ElegantTypewriter,
                                    fontSize = 13.sp,
                                    color = GoldFoilLight,
                                    letterSpacing = 1.2.sp
                                )
                            }
                        }
                    }
                }

                // Error message
                if (uiState.errorMessage != null) {
                    item {
                        Text(
                            text = uiState.errorMessage ?: "",
                            fontFamily = ElegantTypewriter,
                            fontSize = 12.sp,
                            color = WaxSealRed,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }

                // Photos Feed grouped by date
                if (uiState.photosGroupedByDate.isEmpty() && !uiState.isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "No photos dropped yet.",
                                    fontFamily = InternetFriends,
                                    fontSize = 22.sp,
                                    color = DarkSepiaText
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Be the first to drop today's memory.",
                                    fontFamily = ElegantTypewriter,
                                    fontSize = 12.sp,
                                    color = MutedSepiaText
                                )
                            }
                        }
                    }
                } else {
                    uiState.photosGroupedByDate.forEach { (dateStr, photos) ->
                        // Date Separator Header
                        item(key = "header_$dateStr") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp, bottom = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(1.dp)
                                        .background(AntiqueBorder)
                                )
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 10.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(ParchmentCardSurface)
                                        .border(1.dp, AntiqueBorder, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 10.dp, vertical = 3.dp)
                                ) {
                                    val shortYear = if (dateStr.length >= 4) dateStr.substring(2) else dateStr
                                    Text(
                                        text = "'${shortYear.replace("-", " ")}",
                                        fontFamily = ElegantTypewriter,
                                        fontSize = 11.sp,
                                        color = DateStampAmber,
                                        letterSpacing = 1.sp
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(1.dp)
                                        .background(AntiqueBorder)
                                )
                            }
                        }

                        // Polaroid Cards in 2-Column Scrapbook Gallery Grid
                        photos.chunked(2).forEach { rowPhotos ->
                            item(key = "row_${rowPhotos.first().id}") {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    rowPhotos.forEach { photo ->
                                        Box(modifier = Modifier.weight(1f)) {
                                            PolaroidPhotoCard(
                                                photo = photo,
                                                onClick = { selectedPhoto = photo }
                                            )
                                        }
                                    }
                                    // If odd number of photos, add a spacer to maintain equal column width
                                    if (rowPhotos.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Daily Drop Modal Dialog
        if (showDropDialog) {
            PhotoDropDialog(
                isUploading = uiState.isUploadingDrop,
                onDismiss = { showDropDialog = false },
                onConfirmDrop = { bitmap, caption ->
                    viewModel.dropTodayPhoto(bitmap, caption)
                    showDropDialog = false
                }
            )
        }

        // Participants Full Sheet
        if (showParticipantsSheet && yearbook != null) {
            val allPhotos = uiState.photosGroupedByDate.values.flatten()
            ParticipantsSheet(
                yearbook = yearbook,
                members = uiState.members,
                photos = allPhotos,
                onDismiss = { showParticipantsSheet = false }
            )
        }

        // In-flight PDF Scrapbook Generation Dialog
        if (uiState.isExportingPdf) {
            BasicAlertDialog(onDismissRequest = { }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(16.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(ParchmentCardSurface)
                        .border(2.dp, GoldFoil, RoundedCornerShape(16.dp))
                        .padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularProgressIndicator(
                            color = SaddleLeather,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "GENERATING PDF SCRAPBOOK",
                            fontFamily = ElegantTypewriter,
                            fontSize = 13.sp,
                            color = SaddleLeather,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = uiState.pdfExportProgress ?: "Rendering high-res polaroids...",
                            fontFamily = ElegantTypewriter,
                            fontSize = 11.sp,
                            color = MutedSepiaText,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Confirmation Dialog to Seal Album
        if (showSealConfirmDialog) {
            BasicAlertDialog(onDismissRequest = { showSealConfirmDialog = false }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(16.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(ParchmentCardSurface)
                        .border(2.dp, GoldFoil, RoundedCornerShape(16.dp))
                        .padding(24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(SaddleLeather)
                                .border(1.5.dp, GoldFoil, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Seal Album",
                                tint = GoldFoilLight,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Seal & Complete Album?",
                            fontFamily = InternetFriends,
                            fontSize = 24.sp,
                            color = DarkSepiaText
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Sealing this album will conclude the memory collection phase. You and all members will unlock the options to make the Nostalgia Reel and generate the PDF Scrapbook.\n\nThe 1-month cloud safety timer will start.",
                            fontFamily = ElegantTypewriter,
                            fontSize = 12.sp,
                            color = MutedSepiaText,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        LeatherButton(
                            text = "SEAL ALBUM NOW",
                            onClick = {
                                viewModel.sealYearbook()
                                showSealConfirmDialog = false
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Cancel",
                            fontFamily = ElegantTypewriter,
                            fontSize = 12.sp,
                            color = MutedSepiaText,
                            modifier = Modifier
                                .clickable { showSealConfirmDialog = false }
                                .padding(8.dp)
                        )
                    }
                }
            }
        }

        // Photo Detail Fullscreen Dialog (Uncropped aspect ratio preserved)
        if (selectedPhoto != null) {
            val photo = selectedPhoto!!
            Dialog(onDismissRequest = { selectedPhoto = null }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(16.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(PolaroidPaper)
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Author header & close
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Photo by ${photo.authorName}",
                                fontFamily = ElegantTypewriter,
                                fontSize = 12.sp,
                                color = MutedSepiaText
                            )

                            IconButton(
                                onClick = { selectedPhoto = null },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = DarkSepiaText,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Full Polaroid Photo Frame with Glowing Amber Date Stamp
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF201B17))
                                .border(1.dp, AntiqueBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = photo.photoUrl,
                                contentDescription = photo.caption,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 440.dp)
                            )

                            val rawDate = photo.dateString.replace("-", " ")
                            val shortYear = if (rawDate.length >= 4) rawDate.substring(2) else rawDate
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                                    .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "'$shortYear",
                                    fontFamily = ElegantTypewriter,
                                    fontSize = 13.sp,
                                    color = DateStampAmber,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        if (photo.caption.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = photo.caption,
                                fontFamily = InternetFriends,
                                fontSize = 24.sp,
                                color = DarkSepiaText,
                                lineHeight = 28.sp,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
