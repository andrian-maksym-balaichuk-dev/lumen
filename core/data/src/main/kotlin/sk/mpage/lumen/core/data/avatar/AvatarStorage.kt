package sk.mpage.lumen.core.data.avatar

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import sk.mpage.lumen.core.common.dispatcher.Dispatcher
import sk.mpage.lumen.core.common.dispatcher.LumenDispatcher
import sk.mpage.lumen.core.model.Avatar
import java.io.File
import java.io.IOException
import java.time.Clock
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.roundToInt

internal class AvatarStorage
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val clock: Clock,
        @Dispatcher(LumenDispatcher.IO) private val io: CoroutineDispatcher,
    ) {
        private val directory: File get() = File(context.filesDir, Avatar.Custom.DIRECTORY)

        fun file(fileName: String): File = File(directory, fileName)

        suspend fun import(uriString: String): String =
            withContext(io) {
                val bitmap = decode(uriString.toUri())
                val fileName = "avatar_${clock.millis()}.jpg"
                val partial = File(directory, "$fileName.part")
                try {
                    directory.mkdirs()
                    partial.outputStream().use { out ->
                        if (!bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)) {
                            throw IOException("JPEG encoding failed")
                        }
                    }
                    if (!partial.renameTo(file(fileName))) throw IOException("Could not move $partial")
                    fileName
                } finally {
                    partial.delete()
                    bitmap.recycle()
                }
            }

        suspend fun delete(fileName: String) {
            withContext(io) { file(fileName).delete() }
        }

        private fun decode(uri: Uri): Bitmap {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            return ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                val longest = max(info.size.width, info.size.height)
                if (longest > MAX_SIZE_PX) {
                    val scale = MAX_SIZE_PX.toFloat() / longest
                    decoder.setTargetSize(
                        (info.size.width * scale).roundToInt().coerceIn(1, MAX_SIZE_PX),
                        (info.size.height * scale).roundToInt().coerceIn(1, MAX_SIZE_PX),
                    )
                }
            }
        }

        companion object {
            const val MAX_SIZE_PX: Int = 512
            private const val JPEG_QUALITY = 85
        }
    }
