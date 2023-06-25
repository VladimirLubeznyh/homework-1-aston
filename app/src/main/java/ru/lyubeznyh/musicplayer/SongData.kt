package ru.lyubeznyh.musicplayer

import androidx.annotation.DrawableRes
import androidx.annotation.RawRes

data class SongData(
    val artist:String,
    val songName: String,
    @RawRes
    val song:Int,
    @DrawableRes
    val albumImage:Int
)
