package com.example.musicplayer.activity

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.musicplayer.ApplicationClass
import com.example.musicplayer.R
import com.example.musicplayer.`object`.MusicAudioLocal
import com.example.musicplayer.adapter.MusicAdapter
import com.example.musicplayer.adapter.SongAdapter
import com.example.musicplayer.adapter.SongSearchAdapter
import com.example.musicplayer.api.ApiMusic
import com.example.musicplayer.model.Music
import com.example.musicplayer.model.apisearch.MusicSearch
import com.example.musicplayer.model.apisearch.Song
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_now_playing.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.random.Random
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private val PERMISSION_READ = 0
    private val listSong: MutableList<MusicAudioLocal> = ArrayList()
    private lateinit var musicAdapter: MusicAdapter

    companion object {
        var musicListLocal: MutableList<MusicAudioLocal> = ArrayList()
        var searchList: MutableList<MusicAudioLocal> = ArrayList()
        lateinit var chartRealTimeAdapter: SongAdapter
        lateinit var songSearchAdapter: SongSearchAdapter
        var songSearchList: MutableList<Song> = ArrayList()
    }

    private val broadcastPlayPause = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            val flag = p1?.getStringExtra("flag")
            if (flag == "play") btnPlayPauseNP.setImageResource(R.drawable.ic_pause) else btnPlayPauseNP.setImageResource(
                R.drawable.ic_play
            )
        }
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            val flag = p1?.getStringExtra("flag")
            var index = PlayMusicActivity.indexSong
            if (PlayMusicActivity.isShuffle) index =
                Random.nextInt(0, PlayMusicActivity.musicList.size)
            else {
                if (flag == "next") {
                    if (ApplicationClass.type == "chart-realtime") {
                        if (index < PlayMusicActivity.musicList.size - 1) {
                            index++
                        } else {
                            index = 0
                        }
                    } else if (ApplicationClass.type == "search") {
                        if (index < PlayMusicActivity.songSearchList.size - 1) {
                            index++
                        } else {
                            index = 0
                        }
                    }

                } else if (flag == "previous") {
                    if (ApplicationClass.type == "chart-realtime") {
                        if (index > 0)
                            index--
                        else index = PlayMusicActivity.musicList.size - 1
                    } else if (ApplicationClass.type == "search") {
                        if (index > 0)
                            index--
                        else index = PlayMusicActivity.songSearchList.size - 1
                    }
                }
            }
            PlayMusicActivity.indexSong = index
            //checkType
            val songIndex =
                if (ApplicationClass.type == "chart-realtime") {
                    PlayMusicActivity.musicList[index]
                } else PlayMusicActivity.songSearchList[index]
            if (songIndex is com.example.musicplayer.model.Song) {
                songNameNP.text = "${index + 1}." + songIndex.name
                Glide.with(baseContext).load(songIndex.thumbnail).into(imageNP)
                PlayMusicActivity.currentSongName = songIndex.name
                PlayMusicActivity.currentSongArtist = songIndex.artists_names
                PlayMusicActivity.currentSongThumb = songIndex.thumbnail
            } else if (songIndex is Song) {
                songNameNP.text = "${index + 1}." + songIndex.name
                Glide.with(baseContext).load("https://photo-resize-zmp3.zadn.vn/" + songIndex.thumb)
                    .into(imageNP)
                PlayMusicActivity.currentSongName = songIndex.name
                PlayMusicActivity.currentSongArtist = songIndex.artist
                PlayMusicActivity.currentSongThumb = songIndex.thumb
            }

            PlayMusicActivity.musicService!!.showNotification(R.drawable.ic_pause)
            Log.d(TAG, "push notify in MainActivity")
            PlayMusicActivity.musicService!!.createMedia()
            PlayMusicActivity.musicService!!.mediaPlayer!!.start()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastPlayPause, IntentFilter("play_pause"))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastReceiver, IntentFilter("ChangedSong"))

        //load api chart-realtime
        progressLoadingHome.visibility = View.VISIBLE
        getFromAPI()
        chartRealTimeAdapter = SongAdapter(ApplicationClass.listChartRealtime, this)

        rcvListSong.adapter = chartRealTimeAdapter
        rcvListSong.layoutManager = LinearLayoutManager(this)
        rcvListSong.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        chartRealTimeAdapter.setOnSongClick {
            val intent = Intent(this, PlayMusicActivity::class.java)
            //intent.putExtra("songFromHome", ApplicationClass.listChartRealtime[it])
            intent.putExtra("indexSong", it)
            ApplicationClass.type = "chart-realtime"
            intent.putExtra("type", "chart-realtime")
            startActivity(intent)
        }

        songSearchAdapter = SongSearchAdapter(songSearchList, this)
        songSearchAdapter.setOnSongClick {
            val intent = Intent(this, PlayMusicActivity::class.java)
            //intent.putExtra("songFromHome", ApplicationClass.listChartRealtime[it])
            intent.putExtra("indexSong", it)
            ApplicationClass.type = "search"
            intent.putExtra("type", "search")
            startActivity(intent)
        }
        search_bar.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                rcvListSong.adapter = songSearchAdapter
                getSongSearchFormAPI(p0!!)
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                if (p0.isNullOrEmpty()) {
                    rcvListSong.adapter = chartRealTimeAdapter
                }
                return true
            }
        })
        //Library

        musicAdapter = MusicAdapter(listSong)
        btnLibrary.setOnClickListener {
            if (checkPermission()) {
                loadLocalSongFromDevice()
            }
            //local song
            rcvListSong.adapter = musicAdapter
        }
        musicAdapter.setOnClickSongItem {
            //intent to PlayMusicActivity
            val intent = Intent(this, PlayMusicActivity::class.java)
            ApplicationClass.type = "offline"
            intent.putExtra("indexSong", listSong.indexOf(searchList[it]))
            startActivity(intent)
        }

        tvTopMusic.setOnClickListener {
            rcvListSong.adapter = chartRealTimeAdapter
        }

    }

    private fun getSongSearchFormAPI(query: String) {
        progressLoadingHome.visibility = View.VISIBLE
        ApiMusic.search.getSongSearch("artist,song,key,code", 500, query).enqueue(
            object : Callback<MusicSearch> {
                override fun onResponse(call: Call<MusicSearch>, response: Response<MusicSearch>) {
                    songSearchList.clear()
                    ApplicationClass.listSongSearch.clear()
                    try {
                        songSearchList.addAll(response.body()!!.data[0].song)
                        ApplicationClass.listSongSearch.addAll(songSearchList)
                        tvError.visibility = View.GONE
                    } catch (ex: Exception) {
                        Toast.makeText(baseContext, ex.message, Toast.LENGTH_SHORT).show()
                        tvError.visibility = View.VISIBLE
                        tvError.text = "Không có bài hát nào!"
                    }
                    songSearchAdapter.notifyDataSetChanged()
                    progressLoadingHome.visibility = View.GONE

                }

                override fun onFailure(call: Call<MusicSearch>, t: Throwable) {
                    Log.e(TAG, "onFailure-error: ${t.message}")
                    Toast.makeText(baseContext, "Tìm kiếm thất bại!", Toast.LENGTH_LONG).show()
                    progressLoadingHome.visibility = View.GONE
                }
            }
        )
    }

    private fun getFromAPI() {
        val builder = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create())
            .baseUrl(ApplicationClass.BASE_API)
            .build().create(ApiMusic::class.java)
        val get = builder.getSong()
        get.enqueue(object : Callback<Music> {
            override fun onResponse(call: Call<Music>, response: Response<Music>) {
                ApplicationClass.listChartRealtime.clear()
                ApplicationClass.listChartRealtime.addAll(response.body()!!.data.song)
                progressLoadingHome.visibility = View.GONE
                chartRealTimeAdapter.notifyDataSetChanged()
            }

            override fun onFailure(call: Call<Music>, t: Throwable) {
                Log.d(TAG, "onFailure: ${t.message}")
            }
        })
    }


    private fun loadLocalSongFromDevice() {
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val cursor =
            contentResolver.query(uri, null, MediaStore.Audio.Media.IS_MUSIC + "!=0", null, null)
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
                    listSong.add(MusicAudioLocal(title, duration, author, Uri.parse(url)))
                } while (cursor.moveToNext())
            } catch (ex: Exception) {
                Log.e(TAG, "loadLocalSongFromDevice: ${ex.message}")
            }

        }
        cursor!!.close()
        musicAdapter.notifyDataSetChanged()
        searchList = listSong
        musicListLocal = listSong
    }

    private fun checkPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSION_READ
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_READ -> {
                if (grantResults.isNotEmpty() && permissions[0] == Manifest.permission.READ_EXTERNAL_STORAGE) {
                    if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                        Toast.makeText(this, "Please allow storage permission", Toast.LENGTH_SHORT)
                            .show()
                    } else loadLocalSongFromDevice()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!PlayMusicActivity.isPlaying && PlayMusicActivity.musicService != null) {
            PlayMusicActivity.musicService!!.stopForeground(true)
            PlayMusicActivity.musicService!!.mediaPlayer!!.release()
            PlayMusicActivity.musicService = null
            exitProcess(1)
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastPlayPause)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
    }
}