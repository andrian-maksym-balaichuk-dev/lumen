package sk.mpage.lumen.core.model

public enum class Difficulty(
    public val code: String,
) {
    ANY("any"),
    EASY("easy"),
    MEDIUM("medium"),
    HARD("hard"),
    ;

    public companion object {
        public fun fromCode(code: String): Difficulty =
            requireNotNull(entries.firstOrNull { it.code == code }) { "Unknown difficulty code: $code" }
    }
}
