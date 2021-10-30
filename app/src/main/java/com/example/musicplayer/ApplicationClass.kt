package com.example.musicplayer

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import com.example.musicplayer.`object`.MusicAudioLocal
import com.example.musicplayer.api.ApiMusic
import com.example.musicplayer.database.SongFavourite
import com.example.musicplayer.model.Music
import com.example.musicplayer.model.Song
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApplicationClass : Application() {
    private val TAG = "Activity-ApplicationClass"

    companion object {
        const val CHANNEL_ID = "channel1"
        const val PLAY = "play"
        const val NEXT = "next"
        const val PREVIOUS = "previous"
        const val EXIT = "exit"
        var listChartRealtime: MutableList<Song> = ArrayList()
        var listSongSearch: MutableList<com.example.musicplayer.model.apisearch.Song> = ArrayList()
        var listSongLocal : MutableList<MusicAudioLocal> = ArrayList()
        var listSongFavourite : MutableList<SongFavourite> = ArrayList()
        const val BASE_API = "https://mp3.zing.vn/"
        var type = "chart-realtime"
        var currentSongName = ""
    }

    override fun onCreate() {
        super.onCreate()


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(CHANNEL_ID, "Now Playing", NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.setSound(null, null)
            notificationChannel.description = "Channel for showing song!!"
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
        getFromAPI()
    }

    private fun getFromAPI() {
        val builder = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_API)
            .build().create(ApiMusic::class.java)
        val get = builder.getSong()
        get.enqueue(object : Callback<Music> {
            override fun onResponse(call: Call<Music>, response: Response<Music>) {
                listChartRealtime.addAll(response.body()!!.data.song)
            }

            override fun onFailure(call: Call<Music>, t: Throwable) {
                Log.d(TAG, "onFailure: ${t.message}")
            }
        })
    }

}