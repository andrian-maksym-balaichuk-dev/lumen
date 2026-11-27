package sk.mpage.lumen.core.ui

import android.content.res.Resources
import sk.mpage.lumen.core.model.Difficulty
import sk.mpage.lumen.core.model.GameMode
import sk.mpage.lumen.core.model.ShareSummary

public object ShareTextFormatter {
    public fun format(
        summary: ShareSummary,
        resources: Resources,
    ): String {
        val category = summary.categoryName.ifEmpty { resources.getString(R.string.share_any_category) }
        val difficulty =
            resources.getString(
                when (summary.difficulty) {
                    Difficulty.ANY -> R.string.share_difficulty_any
                    Difficulty.EASY -> R.string.share_difficulty_easy
                    Difficulty.MEDIUM -> R.string.share_difficulty_medium
                    Difficulty.HARD -> R.string.share_difficulty_hard
                },
            )
        val duration = formatDuration(summary.durationSeconds)
        val result =
            when (summary.mode) {
                GameMode.CLASSIC -> {
                    resources.getString(
                        R.string.share_classic,
                        summary.correct,
                        summary.total,
                        summary.percent,
                        category,
                        difficulty,
                        duration,
                    )
                }

                GameMode.MARATHON -> {
                    resources.getQuantityString(
                        R.plurals.share_marathon,
                        summary.correct,
                        summary.correct,
                        category,
                        difficulty,
                        duration,
                    )
                }
            }
        val best = if (summary.isNewBest) " " + resources.getString(R.string.share_new_best) else ""
        return result + best + "\n" + resources.getString(R.string.share_closing)
    }
}
