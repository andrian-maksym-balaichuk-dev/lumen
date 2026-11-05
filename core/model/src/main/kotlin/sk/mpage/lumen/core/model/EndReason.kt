package sk.mpage.lumen.core.model

public enum class EndReason(
    public val code: String,
) {
    COMPLETED("completed"),
    WRONG_ANSWER("wrong_answer"),
    TIMEOUT("timeout"),
    ;

    public companion object {
        public fun fromCode(code: String): EndReason =
            requireNotNull(entries.firstOrNull { it.code == code }) { "Unknown end reason code: $code" }
    }
}
