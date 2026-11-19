package sk.mpage.lumen.core.common.text

import javax.inject.Inject

public class HtmlEntityDecoder internal constructor(
    private val namedEntities: () -> Map<String, String>,
) {
    @Inject
    public constructor() : this({ HtmlNamedEntities.table })

    public fun decode(text: String): String {
        if ('&' !in text) return text
        return ENTITY.replace(text) { match -> resolve(match.groupValues[1]) ?: match.value }
    }

    private fun resolve(body: String): String? =
        when {
            body.startsWith("#x") || body.startsWith("#X") -> codePoint(body.substring(2), radix = 16)
            body.startsWith("#") -> codePoint(body.substring(1), radix = 10)
            else -> namedEntities()[body]
        }

    private fun codePoint(
        digits: String,
        radix: Int,
    ): String? {
        val value = digits.toIntOrNull(radix) ?: return null
        val valid =
            value in 1..Character.MAX_CODE_POINT && value !in Character.MIN_SURROGATE.code..Character.MAX_SURROGATE.code
        return if (valid) String(Character.toChars(value)) else null
    }

    private companion object {
        val ENTITY = Regex("&(#\\d+|#[xX][0-9a-fA-F]+|[a-zA-Z][a-zA-Z0-9]*);")
    }
}

internal object HtmlNamedEntities {
    val table: Map<String, String> by lazy { load() }

    private fun load(): Map<String, String> {
        val stream =
            checkNotNull(HtmlNamedEntities::class.java.getResourceAsStream("html-entities.tsv")) {
                "html-entities.tsv is missing from the classpath"
            }
        return stream.bufferedReader().useLines { lines ->
            lines
                .filter { it.isNotEmpty() && !it.startsWith("#") }
                .associate { line ->
                    val (name, codePoints) = line.split('\t')
                    name to
                        buildString {
                            codePoints.split(' ').forEach { appendCodePoint(it.toInt(16)) }
                        }
                }
        }
    }
}
