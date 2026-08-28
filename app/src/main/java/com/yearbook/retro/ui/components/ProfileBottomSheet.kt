package com.yearbook.retro.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.yearbook.retro.data.model.User
import com.yearbook.retro.ui.theme.AntiqueBorder
import com.yearbook.retro.ui.theme.DarkSepiaText
import com.yearbook.retro.ui.theme.ElegantTypewriter
import com.yearbook.retro.ui.theme.InternetFriends
import com.yearbook.retro.ui.theme.MutedSepiaText
import com.yearbook.retro.ui.theme.ParchmentBackground
import com.yearbook.retro.ui.theme.ParchmentCardSurface
import com.yearbook.retro.ui.theme.SaddleLeather
import com.yearbook.retro.ui.theme.WaxSealRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileBottomSheet(
    user: User?,
    onDismiss: () -> Unit,
    onUpdateName: (String) -> Unit,
    onSignOut: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var isEditingName by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(user?.displayName ?: "") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ParchmentCardSurface,
        scrimColor = Color.Black.copy(alpha = 0.45f),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // User Avatar
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(SaddleLeather)
                    .border(2.dp, AntiqueBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (!user?.photoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = user?.photoUrl,
                        contentDescription = user?.displayName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = (user?.displayName?.take(1) ?: "U").uppercase(),
                        fontFamily = InternetFriends,
                        fontSize = 38.sp,
                        color = ParchmentBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Display Name & Edit Mode
            if (isEditingName) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        modifier = Modifier.weight(1f),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = ElegantTypewriter,
                            fontSize = 16.sp,
                            color = DarkSepiaText
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SaddleLeather,
                            unfocusedBorderColor = AntiqueBorder,
                            focusedContainerColor = ParchmentBackground,
                            unfocusedContainerColor = ParchmentBackground
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SaddleLeather)
                            .clickable {
                                onUpdateName(editedName)
                                isEditingName = false
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "SAVE",
                            fontFamily = ElegantTypewriter,
                            fontSize = 12.sp,
                            color = ParchmentBackground
                        )
                    }
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { isEditingName = true }
                ) {
                    Text(
                        text = user?.displayName ?: "Friend",
                        fontFamily = InternetFriends,
                        fontSize = 26.sp,
                        color = DarkSepiaText
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Name",
                        tint = MutedSepiaText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (!user?.email.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = user!!.email,
                    fontFamily = ElegantTypewriter,
                    fontSize = 13.sp,
                    color = MutedSepiaText
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sign Out Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(WaxSealRed.copy(alpha = 0.12f))
                    .border(1.dp, WaxSealRed.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .clickable {
                        onSignOut()
                        onDismiss()
                    }
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Sign Out",
                        tint = WaxSealRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SIGN OUT",
                        fontFamily = ElegantTypewriter,
                        fontSize = 13.sp,
                        color = WaxSealRed,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
