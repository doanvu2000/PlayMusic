package com.example.musicplayer.repository

import android.annotation.SuppressLint
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import com.example.musicplayer.ApplicationClass
import com.example.musicplayer.`object`.MusicAudioLocal
import com.example.musicplayer.activity.MainActivity
import com.example.musicplayer.api.ApiMusic
import com.example.musicplayer.database.SongDatabase
import com.example.musicplayer.database.SongFavourite
import com.example.musicplayer.model.Music
import com.example.musicplayer.model.apirecommend.MusicRecommend
import com.example.musicplayer.model.apisearch.MusicSearch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SongRepository {
    companion object {
        private var INSTANCE: SongRepository? = null
        fun getInstance() = INSTANCE
            ?: SongRepository().also {
                INSTANCE = it
            }
    }

    //chart-realtime
    fun getTopMusic(result: (isSuccess: Boolean, respone: Music?) -> Unit) {
        ApiMusic.api.getSong().enqueue(object : Callback<Music> {
            override fun onResponse(call: Call<Music>, response: Response<Music>) {
                if (response.isSuccessful && response != null) {//call API Chart Realtime
                    result(true, response.body())

                } else {
                    result(false, null)
                }
            }

            override fun onFailure(call: Call<Music>, t: Throwable) {

            }
        })
    }

    fun getSongSearch(
        query: String,
        result: (isSuccess: Boolean, respone: MusicSearch?) -> Unit
    ) {
        ApiMusic.search.getSongSearch("artist,song,key,code", 500, query).enqueue(
            object : Callback<MusicSearch> {
                override fun onResponse(call: Call<MusicSearch>, response: Response<MusicSearch>) {
                    if (response.isSuccessful && response != null) {
                        result(true, response.body())
                    } else {
                        result(false, null)
                    }
                }

                override fun onFailure(call: Call<MusicSearch>, t: Throwable) {
                }
            }
        )
    }

    fun getSongRecommend(
        id: String,
        result: (isSuccess: Boolean, respone: MusicRecommend?) -> Unit
    ) {
        ApiMusic.api.getSongRecommend("audio", id).enqueue(object : Callback<MusicRecommend> {
            override fun onResponse(
                call: Call<MusicRecommend>,
                response: Response<MusicRecommend>
            ) {
                if (response.isSuccessful && response != null) {
                    result(true, response.body())
                } else {
                    result(false, null)
                }
            }

            override fun onFailure(call: Call<MusicRecommend>, t: Throwable) {
            }
        })
    }

    @SuppressLint("Range")
    fun getSongLocal(context: Context): MutableList<MusicAudioLocal> {
        var listSongLocal: MutableList<MusicAudioLocal> = ArrayList()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val cursor = context.contentResolver.query(
            uri,
            arrayOf(
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA
            ),
            MediaStore.Audio.Media.IS_MUSIC + " != 0",
            null,
            null
        )
        if (cursor != null && cursor.moveToFirst()) {
            try {
                do {
                    val title =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                    val duration =
                        cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
                    val author =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                    val url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                    listSongLocal.add(MusicAudioLocal(title, duration, author, url))
                } while (cursor.moveToNext())
                ApplicationClass.listSongLocal.clear()
                ApplicationClass.listSongLocal.addAll(listSongLocal)
            } catch (ex: Exception) {
                Log.e("Activity", "getSongLocal: ${ex.message}")
            }

        }
        cursor!!.close()
        return listSongLocal
    }
    fun getSongFavourite(context: Context):MutableList<SongFavourite>{
        val songDatabase = SongDatabase(context)
        var songFavourite : MutableList<SongFavourite> = songDatabase.getAllSongFavourite()
        ApplicationClass.listSongFavourite.clear()
        ApplicationClass.listSongFavourite.addAll(songFavourite)
        return songFavourite
    }
}