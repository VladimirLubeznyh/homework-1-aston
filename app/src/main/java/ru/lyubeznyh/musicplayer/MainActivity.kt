package ru.lyubeznyh.musicplayer

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.ImageButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var startBt: ImageButton
    private lateinit var stopBt: ImageButton
    private lateinit var nextBt: ImageButton
    private lateinit var prevBt: ImageButton
    private var musicService: MusicPlayerService? = null
    private val serviceConnection: ServiceConnection by lazy {
        object : ServiceConnection {
            override fun onServiceConnected(p0: ComponentName?, binder: IBinder?) {
                if (binder is MusicPlayerService.MusicBinder) {
                    musicService = binder.getService()
                    initUI()
                }
            }

            override fun onServiceDisconnected(p0: ComponentName?) {
                //nothing happens
            }
        }
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            startService(intent)
        }

    private var intent: Intent? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkPermission(action: () -> Unit) {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            action.invoke()
        } else {
            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startBt = findViewById(R.id.ibPlay)
        stopBt = findViewById(R.id.ibPause)
        nextBt = findViewById(R.id.ibSkipNext)
        prevBt = findViewById(R.id.ibSkipPrevious)
        intent = Intent(this, MusicPlayerService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkPermission { startService(intent) }
        } else {
            startService(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        intent?.let {
            bindService(it, serviceConnection, BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        unbindService(serviceConnection)
    }

    private fun initUI() {
        musicService?.let { service ->
            onStopMusic(service)
            onStartMusic(service)
            onNextMusic(service)
            onPreviousMusic(service)
            correctRenderButton(service)
        }
    }

    private fun onStartMusic(service: MusicPlayerService) {
        startBt.setOnClickListener {
            service.playTrack()
            correctRenderButton(service)
        }
    }

    private fun onStopMusic(service: MusicPlayerService) {
        stopBt.setOnClickListener {
            service.stopTrack()
            correctRenderButton(service)
        }
    }

    private fun onNextMusic(service: MusicPlayerService) {
        nextBt.setOnClickListener {
            service.nextTrack()
            correctRenderButton(service)
        }
    }

    private fun onPreviousMusic(service: MusicPlayerService) {
        prevBt.setOnClickListener {
            service.previousTrack()
            correctRenderButton(service)
        }
    }

   //Displays the correct button depending on whether the player is playing or not
    private fun correctRenderButton(service: MusicPlayerService) {
        if (service.isPlaying()) {
            startBt.visibility = View.INVISIBLE
            stopBt.visibility = View.VISIBLE
        } else {
            startBt.visibility = View.VISIBLE
            stopBt.visibility = View.INVISIBLE
        }
    }
}
