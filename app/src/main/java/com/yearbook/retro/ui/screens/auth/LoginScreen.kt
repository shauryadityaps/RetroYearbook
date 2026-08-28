package com.yearbook.retro.ui.screens.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.yearbook.retro.util.AuthRateLimiter
import com.yearbook.retro.util.EmailValidator

enum class AuthMode {
    SIGN_IN,       // Returning user: Email + Password
    CREATE_ACCOUNT // New user: Email + Username + Password
}

data class AccountExistsDialogState(
    val isOpen: Boolean = false,
    val email: String = "",
    val existingName: String = ""
)

data class UserNotFoundDialogState(
    val isOpen: Boolean = false,
    val email: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val scrollState = rememberScrollState()

    var authMode by remember { mutableStateOf(AuthMode.SIGN_IN) }

    var emailInput by remember { mutableStateOf("") }
    var usernameInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var localError by remember { mutableStateOf<String?>(null) }

    var accountExistsDialog by remember { mutableStateOf(AccountExistsDialogState()) }
    var userNotFoundDialog by remember { mutableStateOf(UserNotFoundDialogState()) }

    LaunchedEffect(currentUser, uiState.isSuccess) {
        if (currentUser != null || uiState.isSuccess) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ParchmentBackground)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(10.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(ParchmentCardSurface)
                .border(1.5.dp, AntiqueBorder, RoundedCornerShape(16.dp))
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Vintage Emblem
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(SaddleLeather)
                    .border(2.dp, GoldFoil, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoStories,
                    contentDescription = "Retro Yearbook",
                    tint = GoldFoilLight,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // App Title
            Text(
                text = "Retro Yearbook",
                fontFamily = InternetFriends,
                fontSize = 34.sp,
                color = DarkSepiaText,
                lineHeight = 38.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Subtitle
            Text(
                text = "One memory per day with your closest friends.",
                fontFamily = ElegantTypewriter,
                fontSize = 12.sp,
                color = MutedSepiaText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Mode Selector Tabs (SIGN IN vs CREATE ACCOUNT)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(ParchmentBackground)
                    .border(1.dp, AntiqueBorder, RoundedCornerShape(10.dp))
                    .padding(3.dp)
            ) {
                // SIGN IN TAB
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (authMode == AuthMode.SIGN_IN) SaddleLeather else ParchmentBackground)
                        .clickable {
                            authMode = AuthMode.SIGN_IN
                            localError = null
                            viewModel.clearError()
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SIGN IN",
                        fontFamily = ElegantTypewriter,
                        fontSize = 12.sp,
                        color = if (authMode == AuthMode.SIGN_IN) GoldFoilLight else DarkSepiaText,
                        letterSpacing = 0.8.sp
                    )
                }

                // CREATE ACCOUNT TAB
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (authMode == AuthMode.CREATE_ACCOUNT) SaddleLeather else ParchmentBackground)
                        .clickable {
                            authMode = AuthMode.CREATE_ACCOUNT
                            localError = null
                            viewModel.clearError()
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CREATE ACCOUNT",
                        fontFamily = ElegantTypewriter,
                        fontSize = 12.sp,
                        color = if (authMode == AuthMode.CREATE_ACCOUNT) GoldFoilLight else DarkSepiaText,
                        letterSpacing = 0.8.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            AnimatedContent(
                targetState = authMode,
                transitionSpec = {
                    if (targetState == AuthMode.CREATE_ACCOUNT) {
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut()
                        )
                    } else {
                        (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> width } + fadeOut()
                        )
                    }
                },
                label = "AuthModeAnimation"
            ) { mode ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (mode == AuthMode.SIGN_IN) {
                        // TAB 1: SIGN IN (RETURNING USER)
                        Text(
                            text = "WELCOME BACK",
                            fontFamily = ElegantTypewriter,
                            fontSize = 11.sp,
                            color = SaddleLeather,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Enter your registered email and password to restore all your albums and memories:",
                            fontFamily = ElegantTypewriter,
                            fontSize = 12.sp,
                            color = MutedSepiaText
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Email Input
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = {
                                emailInput = it
                                localError = null
                                viewModel.clearError()
                            },
                            label = {
                                Text(
                                    text = "YOUR REGISTERED EMAIL",
                                    fontFamily = ElegantTypewriter,
                                    fontSize = 11.sp,
                                    color = MutedSepiaText
                                )
                            },
                            placeholder = {
                                Text(
                                    text = "e.g. yourname@gmail.com",
                                    fontFamily = ElegantTypewriter,
                                    fontSize = 13.sp,
                                    color = MutedSepiaText.copy(alpha = 0.45f)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Email",
                                    tint = SaddleLeather,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = ElegantTypewriter,
                                fontSize = 14.sp,
                                color = DarkSepiaText
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SaddleLeather,
                                unfocusedBorderColor = AntiqueBorder,
                                focusedContainerColor = ParchmentBackground,
                                unfocusedContainerColor = ParchmentBackground
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Password Input
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = {
                                passwordInput = it
                                localError = null
                                viewModel.clearError()
                            },
                            label = {
                                Text(
                                    text = "YOUR PASSWORD",
                                    fontFamily = ElegantTypewriter,
                                    fontSize = 11.sp,
                                    color = MutedSepiaText
                                )
                            },
                            placeholder = {
                                Text(
                                    text = "Enter your password",
                                    fontFamily = ElegantTypewriter,
                                    fontSize = 13.sp,
                                    color = MutedSepiaText.copy(alpha = 0.45f)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Password",
                                    tint = SaddleLeather,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                        tint = SaddleLeather,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = ElegantTypewriter,
                                fontSize = 14.sp,
                                color = DarkSepiaText
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SaddleLeather,
                                unfocusedBorderColor = AntiqueBorder,
                                focusedContainerColor = ParchmentBackground,
                                unfocusedContainerColor = ParchmentBackground
                            )
                        )

                        if (localError != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = localError!!,
                                fontFamily = ElegantTypewriter,
                                fontSize = 11.sp,
                                color = WaxSealRed,
                                textAlign = TextAlign.Center
                            )
                        } else if (uiState.errorMessage != null && !userNotFoundDialog.isOpen) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = uiState.errorMessage!!,
                                fontFamily = ElegantTypewriter,
                                fontSize = 11.sp,
                                color = WaxSealRed,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        LeatherButton(
                            text = "SIGN IN TO MY YEARBOOK",
                            onClick = {
                                // 1. Rate Limiting Check
                                val rateLimit = AuthRateLimiter.checkRateLimit()
                                if (rateLimit is AuthRateLimiter.RateLimitResult.RateLimited) {
                                    localError = "Too many attempts. Please wait ${rateLimit.remainingSeconds}s before trying again."
                                    return@LeatherButton
                                }

                                // 2. Strict RFC 5322 Email Validation
                                val emailValidation = EmailValidator.validate(emailInput)
                                if (emailValidation is EmailValidator.ValidationResult.Invalid) {
                                    localError = emailValidation.reason
                                    return@LeatherButton
                                }

                                if (passwordInput.isBlank()) {
                                    localError = "Please enter your password"
                                    return@LeatherButton
                                }

                                AuthRateLimiter.recordAttempt()
                                localError = null
                                val clean = emailInput.trim().lowercase()

                                viewModel.signInWithEmail(
                                    email = clean,
                                    password = passwordInput,
                                    onUserNotFound = { notFoundEmail ->
                                        userNotFoundDialog = UserNotFoundDialogState(
                                            isOpen = true,
                                            email = notFoundEmail
                                        )
                                    }
                                )
                            },
                            isLoading = uiState.isLoading
                        )
                    } else {
                        // TAB 2: CREATE ACCOUNT (NEW USER)
                        Text(
                            text = "START NEW YEARBOOK JOURNEY",
                            fontFamily = ElegantTypewriter,
                            fontSize = 11.sp,
                            color = SaddleLeather,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Enter your email, choose your username, and set your password:",
                            fontFamily = ElegantTypewriter,
                            fontSize = 12.sp,
                            color = MutedSepiaText
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Email Input
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = {
                                emailInput = it
                                localError = null
                                viewModel.clearError()
                            },
                            label = {
                                Text(
                                    text = "YOUR EMAIL ADDRESS",
                                    fontFamily = ElegantTypewriter,
                                    fontSize = 11.sp,
                                    color = MutedSepiaText
                                )
                            },
                            placeholder = {
                                Text(
                                    text = "e.g. yourname@gmail.com",
                                    fontFamily = ElegantTypewriter,
                                    fontSize = 13.sp,
                                    color = MutedSepiaText.copy(alpha = 0.45f)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Email",
                                    tint = SaddleLeather,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = ElegantTypewriter,
                                fontSize = 14.sp,
                                color = DarkSepiaText
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SaddleLeather,
                                unfocusedBorderColor = AntiqueBorder,
                                focusedContainerColor = ParchmentBackground,
                                unfocusedContainerColor = ParchmentBackground
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Username / Display Name Input
                        OutlinedTextField(
                            value = usernameInput,
                            onValueChange = {
                                usernameInput = it
                                localError = null
                                viewModel.clearError()
                            },
                            label = {
                                Text(
                                    text = "YOUR YEARBOOK USERNAME",
                                    fontFamily = ElegantTypewriter,
                                    fontSize = 11.sp,
                                    color = MutedSepiaText
                                )
                            },
                            placeholder = {
                                Text(
                                    text = "e.g. Alex Rivers",
                                    fontFamily = ElegantTypewriter,
                                    fontSize = 13.sp,
                                    color = MutedSepiaText.copy(alpha = 0.45f)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Username",
                                    tint = SaddleLeather,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = ElegantTypewriter,
                                fontSize = 14.sp,
                                color = DarkSepiaText
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SaddleLeather,
                                unfocusedBorderColor = AntiqueBorder,
                                focusedContainerColor = ParchmentBackground,
                                unfocusedContainerColor = ParchmentBackground
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Create Password Input
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = {
                                passwordInput = it
                                localError = null
                                viewModel.clearError()
                            },
                            label = {
                                Text(
                                    text = "CREATE PASSWORD",
                                    fontFamily = ElegantTypewriter,
                                    fontSize = 11.sp,
                                    color = MutedSepiaText
                                )
                            },
                            placeholder = {
                                Text(
                                    text = "Minimum 4 characters",
                                    fontFamily = ElegantTypewriter,
                                    fontSize = 13.sp,
                                    color = MutedSepiaText.copy(alpha = 0.45f)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Password",
                                    tint = SaddleLeather,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                        tint = SaddleLeather,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = ElegantTypewriter,
                                fontSize = 14.sp,
                                color = DarkSepiaText
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SaddleLeather,
                                unfocusedBorderColor = AntiqueBorder,
                                focusedContainerColor = ParchmentBackground,
                                unfocusedContainerColor = ParchmentBackground
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Confirm Password Input
                        OutlinedTextField(
                            value = confirmPasswordInput,
                            onValueChange = {
                                confirmPasswordInput = it
                                localError = null
                                viewModel.clearError()
                            },
                            label = {
                                Text(
                                    text = "CONFIRM PASSWORD",
                                    fontFamily = ElegantTypewriter,
                                    fontSize = 11.sp,
                                    color = MutedSepiaText
                                )
                            },
                            placeholder = {
                                Text(
                                    text = "Re-enter your password",
                                    fontFamily = ElegantTypewriter,
                                    fontSize = 13.sp,
                                    color = MutedSepiaText.copy(alpha = 0.45f)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Confirm Password",
                                    tint = SaddleLeather,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(
                                        imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                                        tint = SaddleLeather,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = ElegantTypewriter,
                                fontSize = 14.sp,
                                color = DarkSepiaText
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SaddleLeather,
                                unfocusedBorderColor = AntiqueBorder,
                                focusedContainerColor = ParchmentBackground,
                                unfocusedContainerColor = ParchmentBackground
                            )
                        )

                        if (localError != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = localError!!,
                                fontFamily = ElegantTypewriter,
                                fontSize = 11.sp,
                                color = WaxSealRed,
                                textAlign = TextAlign.Center
                            )
                        } else if (uiState.errorMessage != null && !accountExistsDialog.isOpen) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = uiState.errorMessage!!,
                                fontFamily = ElegantTypewriter,
                                fontSize = 11.sp,
                                color = WaxSealRed,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        LeatherButton(
                            text = "CREATE ACCOUNT & ENTER",
                            onClick = {
                                // 1. Rate Limiting Check
                                val rateLimit = AuthRateLimiter.checkRateLimit()
                                if (rateLimit is AuthRateLimiter.RateLimitResult.RateLimited) {
                                    localError = "Too many attempts. Please wait ${rateLimit.remainingSeconds}s before trying again."
                                    return@LeatherButton
                                }

                                // 2. Strict RFC 5322 Email Validation
                                val emailValidation = EmailValidator.validate(emailInput)
                                if (emailValidation is EmailValidator.ValidationResult.Invalid) {
                                    localError = emailValidation.reason
                                    return@LeatherButton
                                }

                                val cleanName = usernameInput.trim()
                                if (cleanName.length < 2) {
                                    localError = "Username must be at least 2 characters"
                                    return@LeatherButton
                                }

                                if (passwordInput.length < 4) {
                                    localError = "Password must be at least 4 characters"
                                    return@LeatherButton
                                }

                                if (passwordInput != confirmPasswordInput) {
                                    localError = "Passwords do not match. Please re-enter."
                                    return@LeatherButton
                                }

                                AuthRateLimiter.recordAttempt()
                                localError = null
                                val cleanEmail = emailInput.trim().lowercase()

                                viewModel.createAccountWithEmail(
                                    email = cleanEmail,
                                    displayName = cleanName,
                                    password = passwordInput,
                                    onAccountExists = { existEmail, existName ->
                                        accountExistsDialog = AccountExistsDialogState(
                                            isOpen = true,
                                            email = existEmail,
                                            existingName = existName
                                        )
                                    }
                                )
                            },
                            isLoading = uiState.isLoading
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Note about permanent email linking
            Text(
                text = "Your account and albums are securely protected with your password and synced to Supabase.",
                fontFamily = ElegantTypewriter,
                fontSize = 10.sp,
                color = MutedSepiaText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Amber vintage engine badge
            Text(
                text = "'26 RETRO SUPABASE ENGINE",
                fontFamily = ElegantTypewriter,
                fontSize = 10.sp,
                color = DateStampAmber,
                letterSpacing = 1.sp
            )
        }
    }

    // POPUP DIALOG 1: EMAIL ALREADY HAS AN ACCOUNT
    if (accountExistsDialog.isOpen) {
        BasicAlertDialog(
            onDismissRequest = {
                accountExistsDialog = AccountExistsDialogState()
            }
        ) {
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
                            imageVector = Icons.Default.Info,
                            contentDescription = "Account Exists",
                            tint = GoldFoilLight,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Account Already Exists",
                        fontFamily = InternetFriends,
                        fontSize = 24.sp,
                        color = DarkSepiaText
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "An account for '${accountExistsDialog.email}' is already registered under '${accountExistsDialog.existingName}'.\n\nPlease switch to Sign In and enter your password.",
                        fontFamily = ElegantTypewriter,
                        fontSize = 12.sp,
                        color = MutedSepiaText,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    LeatherButton(
                        text = "SWITCH TO SIGN IN",
                        onClick = {
                            authMode = AuthMode.SIGN_IN
                            accountExistsDialog = AccountExistsDialogState()
                            localError = null
                            viewModel.clearError()
                        }
                    )
                }
            }
        }
    }

    // POPUP DIALOG 2: NO ACCOUNT FOUND FOR THIS EMAIL
    if (userNotFoundDialog.isOpen) {
        BasicAlertDialog(
            onDismissRequest = {
                userNotFoundDialog = UserNotFoundDialogState()
            }
        ) {
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
                            imageVector = Icons.Default.Warning,
                            contentDescription = "User Not Found",
                            tint = GoldFoilLight,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "No Account Found",
                        fontFamily = InternetFriends,
                        fontSize = 24.sp,
                        color = DarkSepiaText
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "We could not find any registered profile with '${userNotFoundDialog.email}'.\n\nWould you like to create your new yearbook account now?",
                        fontFamily = ElegantTypewriter,
                        fontSize = 12.sp,
                        color = MutedSepiaText,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    LeatherButton(
                        text = "CREATE NEW ACCOUNT",
                        onClick = {
                            authMode = AuthMode.CREATE_ACCOUNT
                            userNotFoundDialog = UserNotFoundDialogState()
                            localError = null
                            viewModel.clearError()
                        }
                    )
                }
            }
        }
    }
}
