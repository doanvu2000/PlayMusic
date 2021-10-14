package com.example.musicplayer.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.musicplayer.ApplicationClass
import com.example.musicplayer.R
import com.example.musicplayer.activity.MainActivity
import com.example.musicplayer.activity.PlayMusicActivity
import com.example.musicplayer.receiver.NotificationReceiver
import kotlinx.android.synthetic.main.activity_play_music.*
import java.lang.Exception

class MusicService : Service() {
    private var myBinder = MyBinder()
    var mediaPlayer: MediaPlayer? = null
    private lateinit var mediaSession: MediaSessionCompat
    override fun onBind(p0: Intent?): IBinder {
        mediaSession = MediaSessionCompat(baseContext, "My Music")
        return myBinder
    }

    inner class MyBinder : Binder() {
        fun currentService(): MusicService {
            return this@MusicService
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val index = intent?.getIntExtra("indexSong", 0)
        when (intent?.action) {

        }

        return super.onStartCommand(intent, flags, startId)
    }


    fun showNotification(playPauseBtn: Int) {
        val prevIntent = Intent(this, NotificationReceiver::class.java)
            .setAction(ApplicationClass.PREVIOUS)
        val prevPendingIntent = PendingIntent.getBroadcast(
            this, 0, prevIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val playIntent = Intent(this, NotificationReceiver::class.java)
            .setAction(ApplicationClass.PLAY)
        val playPendingIntent = PendingIntent.getBroadcast(
            this, 0, playIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val nextIntent = Intent(this, NotificationReceiver::class.java)
            .setAction(ApplicationClass.NEXT)
        val nextPendingIntent = PendingIntent.getBroadcast(
            this, 0, nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val exitIntent = Intent(this, NotificationReceiver::class.java)
            .setAction(ApplicationClass.EXIT)
        val exitPendingIntent = PendingIntent.getBroadcast(
            this, 0, exitIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("indexSong", PlayMusicActivity.indexSong)
        intent.putExtra("flagMain", "resumePlay")
        val mainIntent = PendingIntent.getActivity(this, 0, intent, 0)
        val notification = NotificationCompat.Builder(this, ApplicationClass.CHANNEL_ID)
            .setContentIntent(mainIntent)
            .setContentTitle(PlayMusicActivity.musicList[PlayMusicActivity.indexSong].name)
            .setContentText(PlayMusicActivity.musicList[PlayMusicActivity.indexSong].author)
            .setSmallIcon(R.drawable.musical_note)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.musical_note))
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .addAction(R.drawable.ic_previous, "Previous", prevPendingIntent)
            .addAction(playPauseBtn, "Play", playPendingIntent)
            .addAction(R.drawable.ic_next, "Next", nextPendingIntent)
            .addAction(R.drawable.ic_baseline_close_24, "Exit", exitPendingIntent)
            .build()
        startForeground(1, notification)
    }

    fun createMedia() {
        try {
            if (PlayMusicActivity.musicService!!.mediaPlayer == null) {
                PlayMusicActivity.musicService!!.mediaPlayer = MediaPlayer()
            }
            PlayMusicActivity.musicService!!.mediaPlayer!!.reset()
            //set path
            PlayMusicActivity.musicService!!.mediaPlayer!!.setDataSource(
                this,
                PlayMusicActivity.musicList[PlayMusicActivity.indexSong]!!.url
            )
            PlayMusicActivity.musicService!!.mediaPlayer!!.prepare()

            PlayMusicActivity.musicService!!.showNotification(R.drawable.ic_pause)
        } catch (ex: Exception) {
            return
        }

    }
}