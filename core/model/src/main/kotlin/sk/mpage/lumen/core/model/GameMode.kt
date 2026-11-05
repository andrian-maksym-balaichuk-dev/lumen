package sk.mpage.lumen.core.model

public enum class GameMode(
    public val code: String,
) {
    CLASSIC("classic"),
    MARATHON("marathon"),
    ;

    public companion object {
        public fun fromCode(code: String): GameMode =
            requireNotNull(entries.firstOrNull { it.code == code }) { "Unknown game mode code: $code" }
    }
}
