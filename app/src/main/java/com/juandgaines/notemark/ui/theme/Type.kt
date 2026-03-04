package com.juandgaines.notemark.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.juandgaines.notemark.R

val SpaceGrotesk = FontFamily(
    Font(R.font.space_grotesk_bold, FontWeight.Bold),
    Font(R.font.space_grotesk_medium, FontWeight.Medium),
)

val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
)

val Typography = Typography(
    // Title/xLarge
    displaySmall = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.36.sp,
    ),
    // Title/Large
    headlineMedium = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.32.sp,
    ),
    // Title/Small
    titleSmall = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.Medium,
        fontSize = 17.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
    ),
    // Body/Large
    bodyLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.17.sp,
    ),
    // Body/Medium
    bodyMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
    ),
    // Body/Small
    bodySmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
    ),
)
