package com.yearbook.retro.ui.screens.manage

import android.app.DatePickerDialog
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yearbook.retro.data.model.Yearbook
import com.yearbook.retro.media.DateStampRenderer
import com.yearbook.retro.ui.components.LeatherButton
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
import com.yearbook.retro.ui.theme.SaddleLeather
import com.yearbook.retro.ui.theme.WaxSealRed
import java.util.Calendar

@Composable
fun CreateYearbookTab(
    isCreating: Boolean,
    createdYearbook: Yearbook?,
    errorMessage: String?,
    onCreate: (title: String, description: String, startDate: Long, endDate: Long) -> Unit,
    onReset: () -> Unit,
    onOpenYearbook: (String) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var startDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var endDateMillis by remember { mutableStateOf(System.currentTimeMillis() + (90L * 24 * 60 * 60 * 1000)) }

    var copiedToast by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .imePadding()
            .padding(vertical = 12.dp, horizontal = 4.dp)
    ) {
        if (createdYearbook != null) {
            // Success Card: Invite Code Display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(14.dp))
                    .clip(RoundedCornerShape(14.dp))
                    .background(ParchmentCardSurface)
                    .border(1.5.dp, GoldFoil, RoundedCornerShape(14.dp))
                    .padding(18.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Yearbook Created!",
                        fontFamily = InternetFriends,
                        fontSize = 28.sp,
                        color = DarkSepiaText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Share this 6-character code with your friends:",
                        fontFamily = ElegantTypewriter,
                        fontSize = 12.sp,
                        color = MutedSepiaText,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 6-Character Code Box
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(SaddleLeather)
                            .border(1.dp, GoldFoil, RoundedCornerShape(10.dp))
                            .clickable {
                                clipboardManager.setText(AnnotatedString(createdYearbook.joinCode))
                                copiedToast = true
                            }
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = createdYearbook.joinCode,
                                fontFamily = ElegantTypewriter,
                                fontSize = 24.sp,
                                color = GoldFoilLight,
                                letterSpacing = 4.sp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Code",
                                tint = GoldFoilLight,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (copiedToast) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Code copied to clipboard!",
                            fontFamily = ElegantTypewriter,
                            fontSize = 11.sp,
                            color = Color(0xFF2E7D32)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    LeatherButton(
                        text = "OPEN ALBUM FEED",
                        onClick = { onOpenYearbook(createdYearbook.id) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Create Another Album Action
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(ParchmentBackground)
                            .border(1.dp, SaddleLeather, RoundedCornerShape(10.dp))
                            .clickable {
                                title = ""
                                description = ""
                                onReset()
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Create Another",
                                tint = DarkSepiaText,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CREATE ANOTHER ALBUM",
                                fontFamily = ElegantTypewriter,
                                fontSize = 11.sp,
                                color = DarkSepiaText,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        } else {
            // Create Form
            Column(modifier = Modifier.fillMaxWidth()) {
                // Live Handwritten Title Preview Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(SaddleLeather)
                        .border(1.dp, GoldFoil.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "COVER TITLE PREVIEW",
                            fontFamily = ElegantTypewriter,
                            fontSize = 9.sp,
                            color = GoldFoil,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (title.isNotBlank()) title else "Your Album Title",
                            fontFamily = InternetFriends,
                            fontSize = 24.sp,
                            color = GoldFoilLight,
                            textAlign = TextAlign.Center,
                            lineHeight = 28.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = {
                        Text(
                            text = "ALBUM NAME",
                            fontFamily = ElegantTypewriter,
                            fontSize = 11.sp,
                            color = MutedSepiaText
                        )
                    },
                    placeholder = {
                        Text(
                            text = "e.g. Summer Road Trip '26",
                            fontFamily = ElegantTypewriter,
                            fontSize = 13.sp,
                            color = MutedSepiaText.copy(alpha = 0.45f)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = ElegantTypewriter,
                        fontSize = 14.sp,
                        color = DarkSepiaText
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SaddleLeather,
                        unfocusedBorderColor = AntiqueBorder,
                        focusedContainerColor = ParchmentCardSurface,
                        unfocusedContainerColor = ParchmentCardSurface
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Description Input
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = {
                        Text(
                            text = "DESCRIPTION / THEME",
                            fontFamily = ElegantTypewriter,
                            fontSize = 11.sp,
                            color = MutedSepiaText
                        )
                    },
                    placeholder = {
                        Text(
                            text = "e.g. Our coastal memories and late night drives.",
                            fontFamily = ElegantTypewriter,
                            fontSize = 12.sp,
                            color = MutedSepiaText.copy(alpha = 0.45f)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = ElegantTypewriter,
                        fontSize = 13.sp,
                        color = DarkSepiaText
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SaddleLeather,
                        unfocusedBorderColor = AntiqueBorder,
                        focusedContainerColor = ParchmentCardSurface,
                        unfocusedContainerColor = ParchmentCardSurface
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Date Range Pickers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Start Date
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ParchmentCardSurface)
                            .border(1.dp, AntiqueBorder, RoundedCornerShape(8.dp))
                            .clickable {
                                val cal = Calendar.getInstance().apply { timeInMillis = startDateMillis }
                                DatePickerDialog(
                                    context,
                                    { _, y, m, d ->
                                        val selected = Calendar.getInstance().apply { set(y, m, d) }
                                        startDateMillis = selected.timeInMillis
                                    },
                                    cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH),
                                    cal.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "START DATE",
                                fontFamily = ElegantTypewriter,
                                fontSize = 9.sp,
                                color = MutedSepiaText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Date",
                                    tint = DateStampAmber,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = DateStampRenderer.formatDateForBadge(startDateMillis),
                                    fontFamily = ElegantTypewriter,
                                    fontSize = 11.sp,
                                    color = DarkSepiaText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // End Date
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ParchmentCardSurface)
                            .border(1.dp, AntiqueBorder, RoundedCornerShape(8.dp))
                            .clickable {
                                val cal = Calendar.getInstance().apply { timeInMillis = endDateMillis }
                                DatePickerDialog(
                                    context,
                                    { _, y, m, d ->
                                        val selected = Calendar.getInstance().apply { set(y, m, d) }
                                        endDateMillis = selected.timeInMillis
                                    },
                                    cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH),
                                    cal.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "END DATE",
                                fontFamily = ElegantTypewriter,
                                fontSize = 9.sp,
                                color = MutedSepiaText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Date",
                                    tint = DateStampAmber,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = DateStampRenderer.formatDateForBadge(endDateMillis),
                                    fontFamily = ElegantTypewriter,
                                    fontSize = 11.sp,
                                    color = DarkSepiaText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage,
                        fontFamily = ElegantTypewriter,
                        fontSize = 11.sp,
                        color = WaxSealRed
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                LeatherButton(
                    text = "CREATE & GET INVITE CODE",
                    onClick = {
                        val submitTitle = title
                        val submitDesc = description
                        title = ""
                        description = ""
                        onCreate(submitTitle, submitDesc, startDateMillis, endDateMillis)
                    },
                    isLoading = isCreating
                )

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
