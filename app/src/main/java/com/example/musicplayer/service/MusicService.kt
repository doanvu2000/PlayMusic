package com.example.musicplayer.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.widget.ImageViewCompat
import com.example.musicplayer.ApplicationClass
import com.example.musicplayer.R
import com.example.musicplayer.`object`.MusicAudioLocal
import com.example.musicplayer.activity.MainActivity
import com.example.musicplayer.activity.PlayMusicActivity
import com.example.musicplayer.database.SongFavourite
import com.example.musicplayer.model.Song
import com.example.musicplayer.receiver.NotificationReceiver
import kotlinx.android.synthetic.main.activity_play_music.*
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

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
//
//        val url   = URL(PlayMusicActivity.musicList[PlayMusicActivity.indexSong].thumbnail)
//        val connection :HttpURLConnection = url.openConnection() as HttpURLConnection
//        connection.doInput = true
//        connection.connect()
//        var bitmap :Bitmap = BitmapFactory.decodeStream(connection.inputStream)
//        if (bitmap == null){
//            bitmap = BitmapFactory.decodeResource(resources, R.drawable.musical_note)
//        }

        val notification = NotificationCompat.Builder(this, ApplicationClass.CHANNEL_ID)
            .setContentIntent(mainIntent)
            .setContentTitle(PlayMusicActivity.currentSongName)
            .setContentText(PlayMusicActivity.currentSongArtist)
            .setSmallIcon(R.drawable.musical_note)
            //get image Song?
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
            //checkType
            //check Type
            val songIndex = when (ApplicationClass.type) {
                "chart-realtime" -> PlayMusicActivity.musicList[PlayMusicActivity.indexSong]
                "search" -> PlayMusicActivity.songSearchList[PlayMusicActivity.indexSong]
                "offline" -> PlayMusicActivity.songLocalList[PlayMusicActivity.indexSong]
                else -> PlayMusicActivity.songFavouriteList[PlayMusicActivity.indexSong]
            }
            var link = ""//url play song
            if (songIndex is Song) {//chart-realtime
                link = "http://api.mp3.zing.vn/api/streaming/audio/${songIndex!!.id}/320"
            } else if (songIndex is com.example.musicplayer.model.apisearch.Song) {//search
                link = "http://api.mp3.zing.vn/api/streaming/audio/${songIndex!!.id}/320"
            } else if (songIndex is MusicAudioLocal) {//offline
                link = songIndex.url
            } else if (songIndex is SongFavourite) {//favourite
                if (songIndex.isOnline){
                    link = "http://api.mp3.zing.vn/api/streaming/audio/${songIndex!!.id}/320"
                }else link = songIndex.url
            }
            PlayMusicActivity.musicService!!.mediaPlayer!!.setDataSource(link)
            PlayMusicActivity.musicService!!.mediaPlayer!!.prepare()
            PlayMusicActivity.musicService!!.showNotification(R.drawable.ic_pause)
        } catch (ex: Exception) {
            Log.e("Activity", "error: ${ex.message}")
            return
        }

    }
}