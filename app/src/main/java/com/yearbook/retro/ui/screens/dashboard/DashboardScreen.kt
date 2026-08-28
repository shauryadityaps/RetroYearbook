package com.yearbook.retro.ui.screens.dashboard

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.yearbook.retro.ui.components.LeatherBookCover
import com.yearbook.retro.ui.components.ProfileBottomSheet
import com.yearbook.retro.ui.theme.AntiqueBorder
import com.yearbook.retro.ui.theme.DarkSepiaText
import com.yearbook.retro.ui.theme.DateStampAmber
import com.yearbook.retro.ui.theme.ElegantTypewriter
import com.yearbook.retro.ui.theme.InternetFriends
import com.yearbook.retro.ui.theme.MutedSepiaText
import com.yearbook.retro.ui.theme.ParchmentBackground
import com.yearbook.retro.ui.theme.ParchmentCardSurface
import com.yearbook.retro.ui.theme.SaddleLeather
import com.yearbook.retro.ui.theme.WaxSealGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onOpenYearbook: (String) -> Unit,
    onNavigateToAdd: () -> Unit,
    onSignOut: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showProfileSheet by remember { mutableStateOf(false) }

    val todayFormatted = remember {
        SimpleDateFormat("EEEE, MMMM d", Locale.US).format(Date()).uppercase(Locale.US)
    }

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
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Circular User Profile Avatar
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(SaddleLeather)
                        .border(1.5.dp, AntiqueBorder, CircleShape)
                        .clickable { showProfileSheet = true },
                    contentAlignment = Alignment.Center
                ) {
                    val avatarUrl = uiState.currentUser?.photoUrl
                    if (!avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = "Profile",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = (uiState.currentUser?.displayName?.take(1) ?: "A").uppercase(),
                            fontFamily = InternetFriends,
                            fontSize = 20.sp,
                            color = ParchmentBackground
                        )
                    }
                }

                // Center Title
                Text(
                    text = "Retro Yearbook",
                    fontFamily = InternetFriends,
                    fontSize = 30.sp,
                    color = DarkSepiaText
                )

                // Date Stamp Badge in Top-Right
                Box(
                    modifier = Modifier
                        .background(ParchmentCardSurface, RoundedCornerShape(6.dp))
                        .border(1.dp, AntiqueBorder.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    val shortDate = SimpleDateFormat("MM.dd", Locale.US).format(Date())
                    Text(
                        text = "'26 $shortDate",
                        fontFamily = ElegantTypewriter,
                        fontSize = 11.sp,
                        color = DateStampAmber,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Divider stitch line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(AntiqueBorder.copy(alpha = 0.4f))
            )

            // Main Content: 2-Column Grid of Pending Today Albums
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section Header (spans both columns)
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column {
                        Text(
                            text = todayFormatted,
                            fontFamily = ElegantTypewriter,
                            fontSize = 12.sp,
                            color = MutedSepiaText,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Today's Memory Drops",
                            fontFamily = InternetFriends,
                            fontSize = 26.sp,
                            color = DarkSepiaText
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Add one photo per day to your active albums before midnight.",
                            fontFamily = ElegantTypewriter,
                            fontSize = 12.sp,
                            color = MutedSepiaText
                        )
                    }
                }

                // Empty / All-Done State (spans both columns)
                if (uiState.pendingYearbooks.isEmpty() && !uiState.isLoading) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp)
                                .background(ParchmentCardSurface, RoundedCornerShape(14.dp))
                                .border(1.dp, AntiqueBorder, RoundedCornerShape(14.dp))
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "All Caught Up",
                                    tint = WaxSealGreen,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "All Caught Up!",
                                    fontFamily = InternetFriends,
                                    fontSize = 26.sp,
                                    color = DarkSepiaText
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "You have sealed today's memories in all your ongoing open albums. Check back tomorrow or visit your Library to view past memories.",
                                    fontFamily = ElegantTypewriter,
                                    fontSize = 12.sp,
                                    color = MutedSepiaText,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    // Grid of Pending Albums
                    items(uiState.pendingYearbooks, key = { it.first.id }) { pair ->
                        val yearbook = pair.first
                        val status = pair.second

                        LeatherBookCover(
                            yearbook = yearbook,
                            status = status,
                            isGrid = true,
                            onClick = { onOpenYearbook(yearbook.id) }
                        )
                    }
                }
            }
        }

        // Profile Bottom Sheet
        if (showProfileSheet) {
            ProfileBottomSheet(
                user = uiState.currentUser,
                onDismiss = { showProfileSheet = false },
                onUpdateName = { newName ->
                    viewModel.updateProfile(newName)
                },
                onSignOut = {
                    showProfileSheet = false
                    viewModel.signOut()
                    onSignOut()
                }
            )
        }
    }
}
