package com.yearbook.retro.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.yearbook.retro.R

// Font Families
val ElegantTypewriter = FontFamily(
    Font(R.font.elegant_typewriter_regular, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.elegant_typewriter_bold, FontWeight.Bold, FontStyle.Normal),
    Font(R.font.elegant_typewriter_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.elegant_typewriter_light, FontWeight.Light, FontStyle.Normal)
)

val InternetFriends = FontFamily(
    Font(R.font.internet_friends, FontWeight.Normal)
)

// Retro Typography System
val RetroTypography = Typography(
    // Album Cover Titles & Big Display Headings
    displayLarge = TextStyle(
        fontFamily = InternetFriends,
        fontWeight = FontWeight.Normal,
        fontSize = 34.sp,
        lineHeight = 40.sp,
        color = DarkSepiaText
    ),
    displayMedium = TextStyle(
        fontFamily = InternetFriends,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        color = DarkSepiaText
    ),
    displaySmall = TextStyle(
        fontFamily = InternetFriends,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        color = DarkSepiaText
    ),

    // Screen Headers
    headlineLarge = TextStyle(
        fontFamily = ElegantTypewriter,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        color = DarkSepiaText
    ),
    headlineMedium = TextStyle(
        fontFamily = ElegantTypewriter,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        color = DarkSepiaText
    ),
    headlineSmall = TextStyle(
        fontFamily = ElegantTypewriter,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        color = DarkSepiaText
    ),

    // Photo Captions & Handwritten Notes
    titleLarge = TextStyle(
        fontFamily = InternetFriends,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 26.sp,
        color = DarkSepiaText
    ),
    titleMedium = TextStyle(
        fontFamily = InternetFriends,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 22.sp,
        color = DarkSepiaText
    ),
    titleSmall = TextStyle(
        fontFamily = InternetFriends,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        color = DarkSepiaText
    ),

    // Typewriter Body Text & Metadata
    bodyLarge = TextStyle(
        fontFamily = ElegantTypewriter,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        color = DarkSepiaText
    ),
    bodyMedium = TextStyle(
        fontFamily = ElegantTypewriter,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = DarkSepiaText
    ),
    bodySmall = TextStyle(
        fontFamily = ElegantTypewriter,
        fontWeight = FontWeight.Light,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = MutedSepiaText
    ),

    // Buttons, Badges & Interactive Labels
    labelLarge = TextStyle(
        fontFamily = ElegantTypewriter,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 1.sp,
        color = DarkSepiaText
    ),
    labelMedium = TextStyle(
        fontFamily = ElegantTypewriter,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        color = DarkSepiaText
    ),
    labelSmall = TextStyle(
        fontFamily = ElegantTypewriter,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        color = MutedSepiaText
    )
)
