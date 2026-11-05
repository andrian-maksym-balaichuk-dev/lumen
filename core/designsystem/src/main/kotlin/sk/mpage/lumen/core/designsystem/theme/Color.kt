package sk.mpage.lumen.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

internal val LumoraLightColors =
    lightColorScheme(
        primary = Color(0xFF4F46E5),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFE1E0FF),
        onPrimaryContainer = Color(0xFF14105E),
        secondary = Color(0xFF0F8B8D),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFC8F2F0),
        onSecondaryContainer = Color(0xFF00302F),
        tertiary = Color(0xFFC2410C),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFFFE1CC),
        onTertiaryContainer = Color(0xFF3A1400),
        error = Color(0xFFBA1A1A),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002),
        background = Color(0xFFF4F5FA),
        onBackground = Color(0xFF12131A),
        surface = Color(0xFFF8F9FD),
        onSurface = Color(0xFF12131A),
        onSurfaceVariant = Color(0xFF464A57),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFF2F3F8),
        surfaceContainer = Color(0xFFECEEF4),
        surfaceContainerHigh = Color(0xFFE6E8EF),
        surfaceContainerHighest = Color(0xFFE0E2EA),
        outline = Color(0xFF767A88),
        outlineVariant = Color(0xFFC6C9D4),
        inverseSurface = Color(0xFF2E3038),
    )

internal val LumoraDarkColors =
    darkColorScheme(
        primary = Color(0xFFBDBDFF),
        onPrimary = Color(0xFF20197A),
        primaryContainer = Color(0xFF3730A3),
        onPrimaryContainer = Color(0xFFE1E0FF),
        secondary = Color(0xFF7FD8D5),
        onSecondary = Color(0xFF003736),
        secondaryContainer = Color(0xFF00504F),
        onSecondaryContainer = Color(0xFFC8F2F0),
        tertiary = Color(0xFFFFB68A),
        onTertiary = Color(0xFF532200),
        tertiaryContainer = Color(0xFF7A3300),
        onTertiaryContainer = Color(0xFFFFE1CC),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        background = Color(0xFF0B0C12),
        onBackground = Color(0xFFE4E5EE),
        surface = Color(0xFF0F1017),
        onSurface = Color(0xFFE4E5EE),
        onSurfaceVariant = Color(0xFFC6C9D4),
        surfaceContainerLowest = Color(0xFF0A0B10),
        surfaceContainerLow = Color(0xFF15161D),
        surfaceContainer = Color(0xFF1A1B23),
        surfaceContainerHigh = Color(0xFF24252E),
        surfaceContainerHighest = Color(0xFF2F303A),
        outline = Color(0xFF90939F),
        outlineVariant = Color(0xFF464A57),
        inverseSurface = Color(0xFFE4E5EE),
    )

@Immutable
public data class LumoraColors(
    val success: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val streakFlame: Color,
)

internal val LightSemanticColors =
    LumoraColors(
        success = Color(0xFF1E8E3E),
        successContainer = Color(0xFFD3F5DC),
        onSuccessContainer = Color(0xFF062E14),
        warning = Color(0xFFB26A00),
        streakFlame = Color(0xFFF2610D),
    )

internal val DarkSemanticColors =
    LumoraColors(
        success = Color(0xFF6DD58C),
        successContainer = Color(0xFF0F3D1E),
        onSuccessContainer = Color(0xFFC8F5D2),
        warning = Color(0xFFFFB95C),
        streakFlame = Color(0xFFFF8A3D),
    )

internal val LocalLumoraColors = staticCompositionLocalOf { LightSemanticColors }
