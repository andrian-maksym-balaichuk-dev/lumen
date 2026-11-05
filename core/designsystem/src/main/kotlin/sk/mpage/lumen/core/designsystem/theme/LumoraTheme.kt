package sk.mpage.lumen.core.designsystem.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
public fun LumoraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme =
        when {
            dynamicColor -> if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            darkTheme -> LumoraDarkColors
            else -> LumoraLightColors
        }
    CompositionLocalProvider(
        LocalLumoraColors provides if (darkTheme) DarkSemanticColors else LightSemanticColors,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = LumoraTypography,
            shapes = LumoraShapes,
            content = content,
        )
    }
}

public object Lumora {
    public val colors: LumoraColors
        @Composable
        @ReadOnlyComposable
        get() = LocalLumoraColors.current
}

@Composable
private fun ColorRoles() {
    val scheme = MaterialTheme.colorScheme
    val semantic = Lumora.colors
    val roles =
        listOf(
            Triple("primary", scheme.primary, scheme.onPrimary),
            Triple("primaryContainer", scheme.primaryContainer, scheme.onPrimaryContainer),
            Triple("secondary", scheme.secondary, scheme.onSecondary),
            Triple("secondaryContainer", scheme.secondaryContainer, scheme.onSecondaryContainer),
            Triple("tertiary", scheme.tertiary, scheme.onTertiary),
            Triple("tertiaryContainer", scheme.tertiaryContainer, scheme.onTertiaryContainer),
            Triple("error", scheme.error, scheme.onError),
            Triple("errorContainer", scheme.errorContainer, scheme.onErrorContainer),
            Triple("surface", scheme.surface, scheme.onSurface),
            Triple("surfaceContainerHigh", scheme.surfaceContainerHigh, scheme.onSurfaceVariant),
            Triple("successContainer", semantic.successContainer, semantic.onSuccessContainer),
            Triple("warning", semantic.warning, scheme.surface),
            Triple("streakFlame", semantic.streakFlame, scheme.surface),
        )
    Column(
        modifier = Modifier.background(scheme.background).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text("Lumora", style = MaterialTheme.typography.headlineLarge, color = scheme.onBackground)
        roles.forEach { (name, container, content) -> ColorRole(name, container, content) }
    }
}

@Composable
private fun ColorRole(
    name: String,
    container: Color,
    content: Color,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(container, MaterialTheme.shapes.small)
                .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(name, style = MaterialTheme.typography.labelLarge, color = content)
    }
}

@Preview(name = "Light", widthDp = 320)
@Composable
private fun ColorRolesLightPreview() {
    LumoraTheme(darkTheme = false) { ColorRoles() }
}

@Preview(name = "Dark", widthDp = 320)
@Composable
private fun ColorRolesDarkPreview() {
    LumoraTheme(darkTheme = true) { ColorRoles() }
}
