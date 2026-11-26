package sk.mpage.lumen.core.ui

import java.util.Locale

public fun formatDuration(seconds: Long): String {
    val hours = seconds / 3_600
    val minutes = seconds % 3_600 / 60
    val rest = seconds % 60
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, rest) else "%d:%02d".format(minutes, rest)
}

public fun formatSeconds(
    milliseconds: Long,
    locale: Locale,
): String = String.format(locale, "%.1f", milliseconds / 1_000.0)
