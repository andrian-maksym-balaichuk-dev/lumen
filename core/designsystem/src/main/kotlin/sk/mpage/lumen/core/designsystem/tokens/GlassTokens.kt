package sk.mpage.lumen.core.designsystem.tokens

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import sk.mpage.lumen.core.designsystem.theme.LumoraTheme

@Immutable
public data class GlassMaterial(
    val tint: Color,
    val blur: Dp,
)

@Immutable
public data class LumoraGlass(
    val isEnabled: Boolean,
    val frost: GlassMaterial,
    val thin: GlassMaterial,
    val regular: GlassMaterial,
    val thick: GlassMaterial,
    val highlight: Brush,
    val shadow: Color,
)

internal object GlassTokens {
    private val NightTint = Color(0xFF15161D)

    fun of(
        darkTheme: Boolean,
        enabled: Boolean,
        surfaceContainerHigh: Color,
    ): LumoraGlass {
        val highlight =
            if (darkTheme) {
                Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.20f), Color.White.copy(alpha = 0.04f)))
            } else {
                Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.70f), Color.White.copy(alpha = 0.15f)))
            }
        val shadow = if (darkTheme) Color.Black.copy(alpha = 0.40f) else Color(0xFF0B0C12).copy(alpha = 0.12f)
        if (!enabled) {
            val fallback = GlassMaterial(surfaceContainerHigh.copy(alpha = 0.96f), 0.dp)
            return LumoraGlass(false, fallback, fallback, fallback, fallback, highlight, shadow)
        }
        return if (darkTheme) {
            LumoraGlass(
                isEnabled = true,
                frost = GlassMaterial(Color(0xFF1A1B23).copy(alpha = 0.70f), 0.dp),
                thin = GlassMaterial(NightTint.copy(alpha = 0.62f), 16.dp),
                regular = GlassMaterial(NightTint.copy(alpha = 0.72f), 24.dp),
                thick = GlassMaterial(NightTint.copy(alpha = 0.94f), 0.dp),
                highlight = highlight,
                shadow = shadow,
            )
        } else {
            LumoraGlass(
                isEnabled = true,
                frost = GlassMaterial(Color.White.copy(alpha = 0.72f), 0.dp),
                thin = GlassMaterial(Color.White.copy(alpha = 0.55f), 16.dp),
                regular = GlassMaterial(Color.White.copy(alpha = 0.68f), 24.dp),
                thick = GlassMaterial(Color.White.copy(alpha = 0.94f), 0.dp),
                highlight = highlight,
                shadow = shadow,
            )
        }
    }
}

internal val LocalGlass =
    staticCompositionLocalOf {
        GlassTokens.of(darkTheme = false, enabled = false, surfaceContainerHigh = Color(0xFFE6E8EF))
    }

@Composable
private fun GlassMaterials(darkTheme: Boolean) {
    val scheme = MaterialTheme.colorScheme
    val glass = GlassTokens.of(darkTheme, enabled = true, surfaceContainerHigh = scheme.surfaceContainerHigh)
    val fallback = GlassTokens.of(darkTheme, enabled = false, surfaceContainerHigh = scheme.surfaceContainerHigh)
    val materials =
        listOf(
            "Frost" to glass.frost,
            "Thin" to glass.thin,
            "Regular" to glass.regular,
            "Thick" to glass.thick,
            "Fallback" to fallback.regular,
        )
    Column(
        modifier =
            Modifier
                .background(Brush.linearGradient(listOf(scheme.primary, scheme.secondary, scheme.tertiary)))
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        materials.forEach { (name, material) ->
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(material.tint, MaterialTheme.shapes.medium)
                        .padding(16.dp),
            ) {
                Text("$name, blur ${material.blur}", color = scheme.onSurface)
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun GlassMaterialsPreview() {
    val darkTheme = isSystemInDarkTheme()
    LumoraTheme(darkTheme = darkTheme) {
        GlassMaterials(darkTheme)
    }
}
