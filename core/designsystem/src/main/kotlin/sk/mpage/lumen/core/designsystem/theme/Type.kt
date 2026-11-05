package sk.mpage.lumen.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import sk.mpage.lumen.core.designsystem.R

private fun interFont(weight: Int) =
    Font(
        resId = R.font.inter_variable,
        weight = FontWeight(weight),
        variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
    )

internal val InterFamily =
    FontFamily(
        interFont(400),
        interFont(600),
        interFont(700),
        interFont(800),
    )

private fun inter(
    size: TextUnit,
    lineHeight: TextUnit,
    weight: Int,
) = TextStyle(
    fontFamily = InterFamily,
    fontWeight = FontWeight(weight),
    fontSize = size,
    lineHeight = lineHeight,
)

internal val LumoraTypography =
    Typography(
        displayLarge = inter(64.sp, 68.sp, 800),
        displayMedium = inter(48.sp, 52.sp, 700),
        displaySmall = inter(36.sp, 44.sp, 700),
        headlineLarge = inter(34.sp, 41.sp, 700),
        headlineMedium = inter(28.sp, 34.sp, 700),
        headlineSmall = inter(24.sp, 30.sp, 600),
        titleLarge = inter(20.sp, 26.sp, 600),
        titleMedium = inter(17.sp, 22.sp, 600),
        titleSmall = inter(15.sp, 20.sp, 600),
        bodyLarge = inter(17.sp, 24.sp, 400),
        bodyMedium = inter(15.sp, 20.sp, 400),
        bodySmall = inter(13.sp, 18.sp, 400),
        labelLarge = inter(15.sp, 20.sp, 600),
        labelMedium = inter(13.sp, 16.sp, 600),
        labelSmall = inter(11.sp, 14.sp, 600),
    )
