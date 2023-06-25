package ru.lyubeznyh.musicplayer

import android.app.PendingIntent
import android.content.Context
import android.content.Intent

enum class MediaPlayerActions(private val id: Int, val command:String ) {
    PLAY(1,"ru.lyubeznyh.musicplayer.ACTION_PLAY"),
    PAUSE(2,"ru.lyubeznyh.musicplayer.ACTION_PAUSE"),
    PREVIOUS(3,"ru.lyubeznyh.musicplayer.ACTION_PREVIOUS"),
    NEXT(4,"ru.lyubeznyh.musicplayer.ACTION_NEXT");

    fun getActionPendingIntent(context: Context): PendingIntent? =
        Intent(context, MusicPlayerService::class.java).let { playbackAction ->
            when (id) {
                PREVIOUS.id -> {
                    playbackAction.action = PREVIOUS.command
                    return PendingIntent.getService(
                        context,
                        PREVIOUS.id,
                        playbackAction,
                        PendingIntent.FLAG_IMMUTABLE
                    )
                }
                PLAY.id -> {
                    playbackAction.action = PLAY.command
                    return PendingIntent.getService(
                        context,
                        PLAY.id,
                        playbackAction,
                        PendingIntent.FLAG_IMMUTABLE
                    )
                }
                PAUSE.id -> {
                    playbackAction.action = PAUSE.command
                    return PendingIntent.getService(
                        context,
                        PAUSE.id,
                        playbackAction,
                        PendingIntent.FLAG_IMMUTABLE
                    )
                }
                NEXT.id -> {
                    playbackAction.action = NEXT.command
                    return PendingIntent.getService(
                        context,
                        NEXT.id,
                        playbackAction,
                        PendingIntent.FLAG_IMMUTABLE
                    )
                }
                else -> null
            }
        }
}
