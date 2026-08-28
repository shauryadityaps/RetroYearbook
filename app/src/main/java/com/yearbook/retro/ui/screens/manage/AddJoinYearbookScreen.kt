package com.yearbook.retro.ui.screens.manage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yearbook.retro.ui.theme.AntiqueBorder
import com.yearbook.retro.ui.theme.DarkSepiaText
import com.yearbook.retro.ui.theme.ElegantTypewriter
import com.yearbook.retro.ui.theme.InternetFriends
import com.yearbook.retro.ui.theme.MutedSepiaText
import com.yearbook.retro.ui.theme.ParchmentBackground
import com.yearbook.retro.ui.theme.ParchmentCardSurface
import com.yearbook.retro.ui.theme.SaddleLeather

enum class ManageTab {
    CREATE, JOIN
}

@Composable
fun AddJoinYearbookScreen(
    viewModel: ManageViewModel,
    onOpenYearbook: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(ManageTab.CREATE) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ParchmentBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "MEMORY HUB",
                fontFamily = ElegantTypewriter,
                fontSize = 11.sp,
                color = MutedSepiaText,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Add / Join Yearbook",
                fontFamily = InternetFriends,
                fontSize = 28.sp,
                color = DarkSepiaText
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Segmented Switcher Tab
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(ParchmentCardSurface)
                    .border(1.dp, AntiqueBorder, RoundedCornerShape(10.dp))
                    .padding(3.dp)
            ) {
                // Create Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedTab == ManageTab.CREATE) SaddleLeather else androidx.compose.ui.graphics.Color.Transparent)
                        .clickable { selectedTab = ManageTab.CREATE }
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CREATE ALBUM",
                        fontFamily = ElegantTypewriter,
                        fontSize = 11.sp,
                        color = if (selectedTab == ManageTab.CREATE) ParchmentBackground else DarkSepiaText,
                        letterSpacing = 0.5.sp
                    )
                }

                // Join Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedTab == ManageTab.JOIN) SaddleLeather else androidx.compose.ui.graphics.Color.Transparent)
                        .clickable { selectedTab = ManageTab.JOIN }
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "JOIN BY CODE",
                        fontFamily = ElegantTypewriter,
                        fontSize = 11.sp,
                        color = if (selectedTab == ManageTab.JOIN) ParchmentBackground else DarkSepiaText,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tab Content
            when (selectedTab) {
                ManageTab.CREATE -> {
                    CreateYearbookTab(
                        isCreating = uiState.isCreating,
                        createdYearbook = uiState.createSuccessYearbook,
                        errorMessage = uiState.errorMessage,
                        onCreate = { title, desc, start, end ->
                            viewModel.createYearbook(title, desc, start, end)
                        },
                        onReset = { viewModel.resetCreateState() },
                        onOpenYearbook = onOpenYearbook
                    )
                }
                ManageTab.JOIN -> {
                    JoinYearbookTab(
                        isJoining = uiState.isJoining,
                        previewYearbook = uiState.previewYearbook,
                        joinSuccessYearbook = uiState.joinSuccessYearbook,
                        errorMessage = uiState.errorMessage,
                        onCodeChange = { code ->
                            viewModel.lookupCodePreview(code)
                        },
                        onJoin = { code ->
                            viewModel.joinYearbook(code)
                        },
                        onOpenYearbook = onOpenYearbook
                    )
                }
            }
        }
    }
}
