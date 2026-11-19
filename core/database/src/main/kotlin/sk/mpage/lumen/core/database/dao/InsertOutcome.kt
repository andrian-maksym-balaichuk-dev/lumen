package sk.mpage.lumen.core.database.dao

public data class InsertOutcome(
    val sessionId: Long,
    val previousBest: Int?,
    val alreadySaved: Boolean,
)
