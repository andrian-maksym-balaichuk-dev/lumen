package sk.mpage.lumen.core.model

public enum class QuestionType(
    public val code: String,
) {
    MULTIPLE_CHOICE("multiple"),
    TRUE_FALSE("boolean"),
    ;

    public companion object {
        public fun fromCode(code: String): QuestionType =
            requireNotNull(entries.firstOrNull { it.code == code }) { "Unknown question type code: $code" }
    }
}
