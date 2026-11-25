package sk.mpage.lumen.core.data.avatar

import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import java.io.File
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

internal fun pngFile(
    directory: File,
    width: Int,
    height: Int,
    name: String = "photo_${width}x$height.png",
): Uri {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.MAGENTA) }
    val file = File(directory, name)
    file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    return Uri.fromFile(file)
}

internal fun textFile(directory: File): Uri =
    Uri.fromFile(
        File(directory, "notes.txt").apply {
            writeText("not an image")
        },
    )

internal class TickingClock(
    private var nextMillis: Long = 1_762_900_000_000,
) : Clock() {
    override fun getZone(): ZoneId = ZoneOffset.UTC

    override fun withZone(zone: ZoneId): Clock = this

    override fun instant(): Instant = Instant.ofEpochMilli(nextMillis++)
}
