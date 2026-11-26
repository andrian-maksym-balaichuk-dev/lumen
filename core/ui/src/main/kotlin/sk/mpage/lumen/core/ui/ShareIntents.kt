package sk.mpage.lumen.core.ui

import android.content.Context
import android.content.Intent

public fun Context.shareText(
    text: String,
    chooserTitle: String,
) {
    val send =
        Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
    startActivity(Intent.createChooser(send, chooserTitle))
}
