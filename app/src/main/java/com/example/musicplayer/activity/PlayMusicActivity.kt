package com.example.musicplayer.activity

import android.content.*
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.musicplayer.R
import com.example.musicplayer.`object`.MusicAudio
import com.example.musicplayer.receiver.NotificationReceiver
import com.example.musicplayer.service.MusicService
import kotlinx.android.synthetic.main.activity_play_music.*
import java.lang.Exception
import kotlin.random.Random


class PlayMusicActivity : AppCompatActivity(), ServiceConnection, MediaPlayer.OnCompletionListener {
    override fun onCompletion(p0: MediaPlayer?) {
        if (!repeatOne) {
            when {
                isShuffle -> {
                    indexSong = Random.nextInt(0, musicList.size)
                }
                indexSong < musicList.size - 1 -> {
                    indexSong++
                }
                else -> {
                    indexSong = 0
                }
            }
        }
        song = musicList[indexSong]
        createMedia()
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as MusicService.MyBinder
        musicService = binder.currentService()
        createMedia()
//        musicService!!.showNotification(R.drawable.ic_pause)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
    }

    lateinit var notificationReceiver: NotificationReceiver


    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastPlayPause)
    }

    private val TAG = "PlayMusicActivity"

    var broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            indexSong = intent!!.getIntExtra("indexSong", 0)
            song = musicList[indexSong]
            Log.d(TAG, "onReceive: ${song?.name}")
            createMedia()
        }
    }
    var broadcastPlayPause = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            val flag = p1?.getStringExtra("flag")
            if (flag == "play") playAudio() else pauseAudio()
        }
    }
    var song: MusicAudio? = null

    companion object {
        lateinit var musicList: MutableList<MusicAudio>
        var indexSong = 0
        var isPlaying: Boolean = false
        var musicService: MusicService? = null
        var repeatOne = false
        var isShuffle = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_music)
        supportActionBar?.hide()
        notificationReceiver = NotificationReceiver()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastReceiver, IntentFilter("ChangedSong"))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastPlayPause, IntentFilter("play_pause"))
        val flag = intent.getStringExtra("flagMain")
        if (flag != "resumePlay") { //start service
            val intentService = Intent(this, MusicService::class.java)
            bindService(intentService, this, BIND_AUTO_CREATE)
            startService(intentService)
        } else {
            currentTime.text = timerConversion(musicService!!.mediaPlayer!!.currentPosition)
            totalTime.text = timerConversion(musicService!!.mediaPlayer!!.duration)
            seekBar.max = musicService!!.mediaPlayer!!.duration
            seekBar.progress = musicService!!.mediaPlayer!!.currentPosition
            var currentPos = 0
            val handler = Handler(Looper.getMainLooper())
            val runnable = object : Runnable {
                override fun run() {
                    currentPos = musicService!!.mediaPlayer!!.currentPosition
                    currentTime.text = timerConversion(currentPos)
                    seekBar.progress = currentPos
                    handler.postDelayed(this, 1000)
                }
            }
            handler.postDelayed(runnable, 1000)
        }

        indexSong = intent.getIntExtra("indexSong", 0)
        musicList = ArrayList()
        musicList.addAll(MainActivity.musicList)
        song = musicList[indexSong]
        if (song != null) {
            tvNameSong.text = song!!.name
        }

        btnBackToHome.setOnClickListener {
            finish()
        }

        btnPlay.setOnClickListener {
            if (isPlaying) pauseAudio()
            else playAudio()
        }
        btnPrevious.setOnClickListener {
            if (!isShuffle) {
                if (indexSong > 0) {
                    indexSong--
                } else {
                    indexSong = musicList.size - 1
                }
            }else{
                indexSong = Random.nextInt(0, musicList.size)
            }
            song = musicList[indexSong]
            createMedia()
        }
        btnNext.setOnClickListener {
            if (!isShuffle) {
                if (indexSong < musicList.size - 1) {
                    indexSong++
                } else {
                    indexSong = 0
                }
            }else{
                indexSong = Random.nextInt(0, musicList.size)
            }
            song = musicList[indexSong]
            createMedia()
        }

        btnRepeatOne.setOnClickListener {
            if (!repeatOne) {
                repeatOne = true
                btnRepeatOne.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
            } else {
                repeatOne = false
                btnRepeatOne.setColorFilter(ContextCompat.getColor(this, R.color.white))
            }
        }

        btnShuffleSong.setOnClickListener {
            isShuffle = !isShuffle
            if(isShuffle){
                btnShuffleSong.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
            }else{
                btnShuffleSong.setColorFilter(ContextCompat.getColor(this, R.color.white))
            }
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if (p2) musicService!!.mediaPlayer?.seekTo(p1)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })
    }

    fun createMedia() {
        try {
            if (musicService!!.mediaPlayer == null) {
                musicService!!.mediaPlayer = MediaPlayer()
            }
            musicService!!.mediaPlayer!!.reset()
            tvNameSong.text = song!!.name
            //set path
            musicService!!.mediaPlayer!!.setDataSource(this, song!!.url)
            musicService!!.mediaPlayer!!.prepare()
            musicService!!.mediaPlayer!!.start()
            isPlaying = true
            btnPlay.setImageResource(R.drawable.ic_pause)
            musicService!!.showNotification(R.drawable.ic_pause)
            setAudioProgress()
            musicService!!.mediaPlayer!!.setOnCompletionListener(this)
        } catch (ex: Exception) {
            return
        }

    }

    private fun playAudio() {
        btnPlay.setImageResource(R.drawable.ic_pause)
        musicService!!.showNotification(R.drawable.ic_pause)
        isPlaying = true
        musicService!!.mediaPlayer!!.start()
    }

    private fun pauseAudio() {
        btnPlay.setImageResource(R.drawable.ic_play)
        musicService!!.showNotification(R.drawable.ic_play)
        isPlaying = false
        musicService!!.mediaPlayer!!.pause()
    }

    private fun setAudioProgress() {
        val totalDuration = song!!.duration
        var currentPos = musicService!!.mediaPlayer!!.currentPosition
        totalTime.text = timerConversion(totalDuration)
        currentTime.text = timerConversion(currentPos)
        seekBar.max = totalDuration
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                currentPos = musicService!!.mediaPlayer!!.currentPosition
                currentTime.text = timerConversion(currentPos)
                seekBar.progress = currentPos
                handler.postDelayed(this, 1000)
            }
        }
        handler.postDelayed(runnable, 1000)
    }

    fun timerConversion(value: Int): String {
        val audioTime: String
        val hrs = value / 3600000
        val mns = value / 60000 % 60000
        val scs = value % 60000 / 1000

        audioTime = if (hrs > 0) {
            String.format("%02d:%02d:%02d", hrs, mns, scs)
        } else {
            String.format("%02d:%02d", mns, scs)
        }
        return audioTime
    }

    private fun sendToService() {

    }

}