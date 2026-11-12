package sk.mpage.lumen.core.model

public sealed interface Avatar {
    public data class Preset(
        val key: String,
    ) : Avatar

    public data class Custom(
        val fileName: String,
    ) : Avatar

    public data object None : Avatar
}
