package com.st.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.st.ui.R

val st_main_font = FontFamily(
    Font(resId = R.font.maven_pro_regular),
    Font(resId = R.font.maven_pro_bold, weight = FontWeight.Bold),
    Font(resId = R.font.maven_pro_black, weight = FontWeight.Black),
    Font(resId = R.font.maven_pro_extra_bold, weight = FontWeight.ExtraBold),
    Font(resId = R.font.maven_pro_medium, weight = FontWeight.Medium),
    Font(resId = R.font.maven_pro_semi_bold, weight = FontWeight.SemiBold)
)
