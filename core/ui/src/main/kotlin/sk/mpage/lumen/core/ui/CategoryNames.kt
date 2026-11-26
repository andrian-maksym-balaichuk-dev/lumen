package sk.mpage.lumen.core.ui

import android.content.res.Resources
import androidx.annotation.StringRes
import sk.mpage.lumen.core.model.Category

public fun Category.displayName(resources: Resources): String =
    categoryNameRes(id)?.let(resources::getString) ?: name.substringAfter(": ")

public fun categoryDisplayName(
    apiName: String,
    resources: Resources,
): String = Category(OPEN_TDB_IDS[apiName] ?: -1, apiName).displayName(resources)

private val OPEN_TDB_IDS: Map<String, Int> =
    mapOf(
        "General Knowledge" to 9,
        "Entertainment: Books" to 10,
        "Entertainment: Film" to 11,
        "Entertainment: Music" to 12,
        "Entertainment: Musicals & Theatres" to 13,
        "Entertainment: Television" to 14,
        "Entertainment: Video Games" to 15,
        "Entertainment: Board Games" to 16,
        "Science & Nature" to 17,
        "Science: Computers" to 18,
        "Science: Mathematics" to 19,
        "Mythology" to 20,
        "Sports" to 21,
        "Geography" to 22,
        "History" to 23,
        "Politics" to 24,
        "Art" to 25,
        "Celebrities" to 26,
        "Animals" to 27,
        "Vehicles" to 28,
        "Entertainment: Comics" to 29,
        "Science: Gadgets" to 30,
        "Entertainment: Japanese Anime & Manga" to 31,
        "Entertainment: Cartoon & Animations" to 32,
    )

@StringRes
private fun categoryNameRes(categoryId: Int): Int? =
    when (categoryId) {
        9 -> R.string.category_general
        10 -> R.string.category_books
        11 -> R.string.category_film
        12 -> R.string.category_music
        13 -> R.string.category_musicals
        14 -> R.string.category_television
        15 -> R.string.category_video_games
        16 -> R.string.category_board_games
        17 -> R.string.category_science_nature
        18 -> R.string.category_computers
        19 -> R.string.category_mathematics
        20 -> R.string.category_mythology
        21 -> R.string.category_sports
        22 -> R.string.category_geography
        23 -> R.string.category_history
        24 -> R.string.category_politics
        25 -> R.string.category_art
        26 -> R.string.category_celebrities
        27 -> R.string.category_animals
        28 -> R.string.category_vehicles
        29 -> R.string.category_comics
        30 -> R.string.category_gadgets
        31 -> R.string.category_anime_manga
        32 -> R.string.category_cartoons
        else -> null
    }
