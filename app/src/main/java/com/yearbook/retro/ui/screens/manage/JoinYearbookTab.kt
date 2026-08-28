package com.yearbook.retro.ui.screens.manage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yearbook.retro.data.model.Yearbook
import com.yearbook.retro.ui.components.LeatherButton
import com.yearbook.retro.ui.theme.AntiqueBorder
import com.yearbook.retro.ui.theme.DarkSepiaText
import com.yearbook.retro.ui.theme.ElegantTypewriter
import com.yearbook.retro.ui.theme.GoldFoil
import com.yearbook.retro.ui.theme.GoldFoilLight
import com.yearbook.retro.ui.theme.InternetFriends
import com.yearbook.retro.ui.theme.MutedSepiaText
import com.yearbook.retro.ui.theme.ParchmentCardSurface
import com.yearbook.retro.ui.theme.SaddleLeather
import com.yearbook.retro.ui.theme.WaxSealRed

@Composable
fun JoinYearbookTab(
    isJoining: Boolean,
    previewYearbook: Yearbook?,
    joinSuccessYearbook: Yearbook?,
    errorMessage: String?,
    onCodeChange: (String) -> Unit,
    onJoin: (String) -> Unit,
    onOpenYearbook: (String) -> Unit
) {
    var codeInput by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    LaunchedEffect(joinSuccessYearbook) {
        if (joinSuccessYearbook != null) {
            onOpenYearbook(joinSuccessYearbook.id)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .imePadding()
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Enter Invite Code",
            fontFamily = InternetFriends,
            fontSize = 26.sp,
            color = DarkSepiaText
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Enter the 6-character alphanumeric code provided by your friend.",
            fontFamily = ElegantTypewriter,
            fontSize = 12.sp,
            color = MutedSepiaText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(18.dp))

        // 6-Character Input
        OutlinedTextField(
            value = codeInput,
            onValueChange = {
                val formatted = it.take(6).uppercase()
                codeInput = formatted
                onCodeChange(formatted)
            },
            placeholder = {
                Text(
                    text = "FL26X9",
                    fontFamily = ElegantTypewriter,
                    fontSize = 20.sp,
                    letterSpacing = 4.sp,
                    color = MutedSepiaText.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontFamily = ElegantTypewriter,
                fontSize = 20.sp,
                color = DarkSepiaText,
                letterSpacing = 4.sp,
                textAlign = TextAlign.Center
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SaddleLeather,
                unfocusedBorderColor = AntiqueBorder,
                focusedContainerColor = ParchmentCardSurface,
                unfocusedContainerColor = ParchmentCardSurface
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Live Album Preview if Found
        if (previewYearbook != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(SaddleLeather)
                    .border(1.dp, GoldFoil, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "FOUND YEARBOOK",
                        fontFamily = ElegantTypewriter,
                        fontSize = 10.sp,
                        color = GoldFoil,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = previewYearbook.title,
                        fontFamily = InternetFriends,
                        fontSize = 22.sp,
                        color = GoldFoilLight,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (previewYearbook.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = previewYearbook.description,
                            fontFamily = ElegantTypewriter,
                            fontSize = 11.sp,
                            color = ParchmentCardSurface,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = "Members",
                            tint = GoldFoilLight,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(
                            text = "${previewYearbook.memberIds.size} Collaborators",
                            fontFamily = ElegantTypewriter,
                            fontSize = 11.sp,
                            color = GoldFoilLight
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                fontFamily = ElegantTypewriter,
                fontSize = 12.sp,
                color = WaxSealRed,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Join Action
        LeatherButton(
            text = "JOIN YEARBOOK",
            onClick = {
                onJoin(codeInput)
            },
            enabled = codeInput.length == 6,
            isLoading = isJoining
        )

        Spacer(modifier = Modifier.height(30.dp))
    }
}
