package sk.mpage.lumen.core.ui

import androidx.annotation.DrawableRes
import sk.mpage.lumen.core.designsystem.icon.LumoraIcons
import sk.mpage.lumen.core.model.Category

@DrawableRes
public fun categoryIcon(categoryId: Int?): Int =
    when (categoryId) {
        null -> LumoraIcons.Shuffle
        9 -> LumoraIcons.Lightbulb
        10 -> LumoraIcons.MenuBook
        11 -> LumoraIcons.Movie
        12 -> LumoraIcons.MusicNote
        13 -> LumoraIcons.TheaterComedy
        14 -> LumoraIcons.Tv
        15 -> LumoraIcons.SportsEsports
        16 -> LumoraIcons.Casino
        17 -> LumoraIcons.Eco
        18 -> LumoraIcons.Computer
        19 -> LumoraIcons.Calculate
        20 -> LumoraIcons.AutoAwesome
        21 -> LumoraIcons.SportsSoccer
        22 -> LumoraIcons.Public
        23 -> LumoraIcons.AccountBalance
        24 -> LumoraIcons.Gavel
        25 -> LumoraIcons.Palette
        26 -> LumoraIcons.Star
        27 -> LumoraIcons.Pets
        28 -> LumoraIcons.DirectionsCar
        29 -> LumoraIcons.AutoStories
        30 -> LumoraIcons.Devices
        31 -> LumoraIcons.Animation
        32 -> LumoraIcons.MovieFilter
        else -> LumoraIcons.Category
    }

public val Category.shortName: String
    get() = name.substringAfter(": ")
