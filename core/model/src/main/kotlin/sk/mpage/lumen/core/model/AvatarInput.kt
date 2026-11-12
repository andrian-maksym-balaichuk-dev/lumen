package sk.mpage.lumen.core.model

public sealed interface AvatarInput {
    public data class Preset(
        val key: String,
    ) : AvatarInput

    public data class Picked(
        val uriString: String,
    ) : AvatarInput
}
