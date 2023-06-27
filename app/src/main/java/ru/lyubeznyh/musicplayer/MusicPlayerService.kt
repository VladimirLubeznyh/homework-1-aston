package ru.lyubeznyh.musicplayer

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat

class MusicPlayerService : Service(), MediaPlayer.OnCompletionListener {

    //List of songs with metadata
    private val musics = listOf(
        SongData(
            "Nickelback",
            "How You Remind Me",
            R.raw.song_one,
            R.drawable.album_image_one
        ),
        SongData(
            "Nickelback",
            "When We Stand Together",
            R.raw.song_tow,
            R.drawable.albume_image_tow
        ),
        SongData(
            "Nickelback",
            "Someday",
            R.raw.song_three,
            R.drawable.album_image_one
        )
    )

    private var currentMusicIndex = FIRST_TRACK_INDEX

    private var isForeground = false
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate() {
        super.onCreate()
        if (mediaPlayer == null) {
            createPlayer()
        }
    }

    //When the service bind to the client, it removes it from the foreground
    override fun onBind(p0: Intent?): IBinder {
        stopForeground(STOP_FOREGROUND_REMOVE)
        return MusicBinder()
    }

    private fun showNotificationPlayer() {
        startForeground(NOTIFICATION_PLAYER_ID, createNotification())
        isForeground = true
    }

    private fun removeNotificationPlayer() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        isForeground = false
    }

    //When the service is unbind from the client, it brings it to the foreground
    override fun onUnbind(intent: Intent?): Boolean {
        showNotificationPlayer()
        return true
    }

    //When the service rebind to the client, it removes it from the foreground
    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
        removeNotificationPlayer()
    }

    fun playTrack() {
        mediaPlayer?.start()
    }

    fun stopTrack() {
        mediaPlayer?.pause()
        if(isForeground) stopSelf()
    }

    fun nextTrack() {
        if (currentMusicIndex + INDEX_MOVE_STEP > musics.lastIndex) currentMusicIndex =
            FIRST_TRACK_INDEX
        else currentMusicIndex++
        recreatePlayer()
        playTrack()
    }

    fun previousTrack() {
        if (currentMusicIndex - INDEX_MOVE_STEP < FIRST_TRACK_INDEX) currentMusicIndex =
            musics.lastIndex
        else currentMusicIndex--
        recreatePlayer()
        playTrack()
    }

    private fun createPlayer() {
        mediaPlayer = MediaPlayer.create(this, musics[currentMusicIndex].song).apply {
            setOnCompletionListener(this@MusicPlayerService)
        }
    }

    fun isPlaying(): Boolean = mediaPlayer?.isPlaying ?: false

    //Recreates the player with a new track
    private fun recreatePlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
        createPlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        removeNotification()
    }

    override fun onCompletion(p0: MediaPlayer?) {
        nextTrack()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        handleIncomingActions(intent)
        if (isForeground) {
            showNotificationPlayer()
        }
        return START_NOT_STICKY
    }

    //Creates a new PendingIntent based on the passed action ID from the ActionId class
    private fun playbackAction(actionId: MediaPlayerActions): PendingIntent? =
        actionId.getActionPendingIntent(this)

    //Depending on the action that came, it controls the playback of the track
    private fun handleIncomingActions(playbackAction: Intent?) {
        if (playbackAction == null || playbackAction.action == null) return
        when (playbackAction.action) {
            MediaPlayerActions.PLAY.command -> playTrack()
            MediaPlayerActions.PAUSE.command -> stopTrack()
            MediaPlayerActions.PREVIOUS.command -> previousTrack()
            MediaPlayerActions.NEXT.command -> nextTrack()
        }
    }

    //Creates a new player notification with new track metadata
    private fun createNotification(): Notification {
        val playPauseAction = playbackAction(if (isPlaying()) MediaPlayerActions.PAUSE else MediaPlayerActions.PLAY)
        val resIconAction = if (isPlaying()) R.drawable.ic_pause_40 else R.drawable.ic_play_arrow_40
        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(
                    this, 0, notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            }

        val largeImage: Bitmap? = try {
            BitmapFactory.decodeResource(resources, musics[currentMusicIndex].albumImage)
        } catch (e: Throwable) {
            null
        }

        return NotificationCompat.Builder(this, App.PLAYER_CHANEL).apply {
            setContentTitle(musics[currentMusicIndex].songName)
            setContentText(musics[currentMusicIndex].artist)
            setContentIntent(pendingIntent)
            setOnlyAlertOnce(true)
            largeImage?.let(::setLargeIcon)
            setSmallIcon(R.drawable.ic_music_note)
            addAction(
                R.drawable.id_skip_previous_40,
                "Previous",
                playbackAction(MediaPlayerActions.PREVIOUS)
            )
            addAction(resIconAction, "Play/Pause", playPauseAction)
            addAction(R.drawable.ic_skip_next_40, "Next", playbackAction(MediaPlayerActions.NEXT))
            setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2)
            )
        }.build()
    }

    private fun removeNotification() {
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).cancel(
            NOTIFICATION_PLAYER_ID
        )
    }

    inner class MusicBinder : Binder() {
        fun getService(): MusicPlayerService = this@MusicPlayerService
    }

    companion object {
        private const val NOTIFICATION_PLAYER_ID = 101
        private const val INDEX_MOVE_STEP = 1
        private const val FIRST_TRACK_INDEX = 0
    }
}
