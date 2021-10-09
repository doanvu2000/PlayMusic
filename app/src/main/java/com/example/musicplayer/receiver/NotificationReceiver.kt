package com.example.musicplayer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.musicplayer.ApplicationClass
import com.example.musicplayer.R
import com.example.musicplayer.activity.PlayMusicActivity
import com.example.musicplayer.service.MusicService
import kotlin.system.exitProcess

class NotificationReceiver : BroadcastReceiver() {
    private val TAG = "Activity"
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            ApplicationClass.PREVIOUS -> {
                Log.d(TAG, "previous ")
                previousSong(context!!)
            }
            ApplicationClass.PLAY -> {
                if (PlayMusicActivity.isPlaying) pauseMusic(context!!) else playMusic(context!!)
                Log.d(TAG, "play ")
            }
            ApplicationClass.NEXT -> {
                Log.d(TAG, "next ")
                nextSong(context!!)
            }
            ApplicationClass.EXIT -> {
                Log.d(TAG, "exit ")
                PlayMusicActivity.musicService!!.stopForeground(true)
                PlayMusicActivity.musicService = null
                exitProcess(1)
            }
            else -> Toast.makeText(context, "receiver", Toast.LENGTH_SHORT).show()
        }
    }

    private fun playMusic(context: Context) {
        PlayMusicActivity.isPlaying = true
        PlayMusicActivity.musicService!!.mediaPlayer!!.start()
        PlayMusicActivity.musicService!!.showNotification(R.drawable.ic_pause)

        val intent = Intent("play_pause")
        intent.putExtra("flag", "play")
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    private fun pauseMusic(context: Context) {
        PlayMusicActivity.isPlaying = false
        PlayMusicActivity.musicService!!.mediaPlayer!!.pause()
        PlayMusicActivity.musicService!!.showNotification(R.drawable.ic_play)

        val intent = Intent("play_pause")
        intent.putExtra("flag", "pause")
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)

    }

    private fun previousSong(context: Context) {
        val intentChanged = Intent("ChangedSong")
        var index = PlayMusicActivity.indexSong
        if (index > 0) index-- else index = PlayMusicActivity.musicList.size - 1
        intentChanged.putExtra("indexSong", index)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intentChanged)

    }

    private fun nextSong(context: Context) {
        val intentChanged = Intent("ChangedSong")
        var index = PlayMusicActivity.indexSong
        if (index < PlayMusicActivity.musicList.size - 1) index++ else index = 0
        intentChanged.putExtra("indexSong", index)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intentChanged)
    }
}