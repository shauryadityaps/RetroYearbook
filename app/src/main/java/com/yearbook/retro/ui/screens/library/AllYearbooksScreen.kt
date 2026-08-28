package com.yearbook.retro.ui.screens.library

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yearbook.retro.ui.components.LeatherBookCover
import com.yearbook.retro.ui.theme.AntiqueBorder
import com.yearbook.retro.ui.theme.DarkSepiaText
import com.yearbook.retro.ui.theme.ElegantTypewriter
import com.yearbook.retro.ui.theme.InternetFriends
import com.yearbook.retro.ui.theme.MutedSepiaText
import com.yearbook.retro.ui.theme.ParchmentBackground

@Composable
fun AllYearbooksScreen(
    viewModel: LibraryViewModel,
    onOpenYearbook: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

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
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "THE LIBRARY",
                        fontFamily = ElegantTypewriter,
                        fontSize = 12.sp,
                        color = MutedSepiaText,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "All Yearbooks",
                        fontFamily = InternetFriends,
                        fontSize = 32.sp,
                        color = DarkSepiaText
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(AntiqueBorder.copy(alpha = 0.4f))
            )

            // Bookshelf 2-Column Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Active Section Header
                if (uiState.activeYearbooks.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            text = "ACTIVE MEMORY ALBUMS",
                            fontFamily = ElegantTypewriter,
                            fontSize = 12.sp,
                            color = MutedSepiaText,
                            letterSpacing = 1.sp
                        )
                    }

                    items(uiState.activeYearbooks, key = { it.id }) { yearbook ->
                        LeatherBookCover(
                            yearbook = yearbook,
                            status = null,
                            onClick = { onOpenYearbook(yearbook.id) }
                        )
                    }
                }

                // Completed / Archived Section Header
                if (uiState.archivedYearbooks.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "COMPLETED & ARCHIVED",
                            fontFamily = ElegantTypewriter,
                            fontSize = 12.sp,
                            color = MutedSepiaText,
                            letterSpacing = 1.sp
                        )
                    }

                    items(uiState.archivedYearbooks, key = { it.id }) { yearbook ->
                        LeatherBookCover(
                            yearbook = yearbook,
                            status = com.yearbook.retro.data.model.DailyDropStatus.ENDED,
                            onClick = { onOpenYearbook(yearbook.id) }
                        )
                    }
                }
            }
        }
    }
}
