package sk.mpage.lumen.feature.game.result

import sk.mpage.lumen.core.model.ShareSummary

internal sealed interface ResultEffect {
    data class ShareText(
        val summary: ShareSummary,
    ) : ResultEffect
}
