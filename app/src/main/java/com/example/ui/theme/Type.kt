package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Set of Material typography styles to start with
val Typography =
  Typography(
    displayLarge = TextStyle(
      fontWeight = FontWeight.Black,
      fontSize = 28.sp,
      lineHeight = 34.sp,
      letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
      fontWeight = FontWeight.ExtraBold,
      fontSize = 24.sp,
      lineHeight = 30.sp,
      letterSpacing = (-0.25).sp
    ),
    titleLarge = TextStyle(
      fontWeight = FontWeight.Bold,
      fontSize = 18.sp,
      lineHeight = 24.sp,
      letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
      fontWeight = FontWeight.Bold,
      fontSize = 16.sp,
      lineHeight = 22.sp,
      letterSpacing = 0.15.sp
    ),
    bodyLarge = TextStyle(
      fontWeight = FontWeight.Normal,
      fontSize = 15.sp,
      lineHeight = 22.sp,
      letterSpacing = 0.25.sp
    ),
    bodyMedium = TextStyle(
      fontWeight = FontWeight.Normal,
      fontSize = 13.sp,
      lineHeight = 18.sp,
      letterSpacing = 0.25.sp
    ),
    labelLarge = TextStyle(
      fontWeight = FontWeight.Bold,
      fontSize = 12.sp,
      lineHeight = 16.sp,
      letterSpacing = 0.75.sp
    ),
    labelSmall = TextStyle(
      fontWeight = FontWeight.Bold,
      fontSize = 10.sp,
      lineHeight = 14.sp,
      letterSpacing = 1.sp
    )
  )
