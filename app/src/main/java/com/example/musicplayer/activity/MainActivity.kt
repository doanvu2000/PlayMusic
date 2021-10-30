package com.example.musicplayer.activity

import android.Manifest
import android.annotation.SuppressLint
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
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.musicplayer.ApplicationClass
import com.example.musicplayer.R
import com.example.musicplayer.`object`.MusicAudioLocal
import com.example.musicplayer.adapter.MusicLocalAdapter
import com.example.musicplayer.adapter.SongAdapter
import com.example.musicplayer.adapter.SongFavouriteAdapter
import com.example.musicplayer.adapter.SongSearchAdapter
import com.example.musicplayer.api.ApiMusic
import com.example.musicplayer.database.SongDatabase
import com.example.musicplayer.database.SongFavourite
import com.example.musicplayer.model.Music
import com.example.musicplayer.model.apisearch.MusicSearch
import com.example.musicplayer.model.apisearch.Song
import com.example.musicplayer.repository.SongRepository
import com.example.musicplayer.viewmodel.MusicFavouriteViewModel
import com.example.musicplayer.viewmodel.MusicLocalViewModel
import com.example.musicplayer.viewmodel.MusicSearchViewModel
import com.example.musicplayer.viewmodel.MusicTopViewModel
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
    private var listSongLocal: MutableList<MusicAudioLocal> = ArrayList()
    private lateinit var musicLocalAdapter: MusicLocalAdapter
    private lateinit var musicTopViewModel: MusicTopViewModel

    companion object {
        lateinit var chartRealTimeAdapter: SongAdapter
        lateinit var songSearchAdapter: SongSearchAdapter
        var songSearchList: MutableList<Song> = ArrayList()
        var songFavouriteList: MutableList<SongFavourite> = ArrayList()
        lateinit var songFavouriteAdapter: SongFavouriteAdapter
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
            if (PlayMusicActivity.isShuffle)
                when (ApplicationClass.type) {
                    "chart-realtime" -> index = Random.nextInt(0, PlayMusicActivity.musicList.size)
                    "search" -> index = Random.nextInt(0, PlayMusicActivity.songSearchList.size)
                    "offline" -> index = Random.nextInt(0, PlayMusicActivity.songLocalList.size)
                    "favourite" -> index =
                        Random.nextInt(0, PlayMusicActivity.songFavouriteList.size)
                }
            else {
                if (flag == "next") {
                    when (ApplicationClass.type) {
                        "chart-realtime" -> if (index < PlayMusicActivity.musicList.size - 1) index++ else index =
                            0
                        "search" -> if (index < PlayMusicActivity.songSearchList.size - 1) index++ else index =
                            0
                        "offline" -> if (index < PlayMusicActivity.songLocalList.size - 1) index++ else index =
                            0
                        "favourite" -> if (index < PlayMusicActivity.songFavouriteList.size - 1) index++ else index =
                            0
                    }
                } else if (flag == "previous") {
                    when (ApplicationClass.type) {
                        "chart-realtime" -> if (index <= 0) index =
                            PlayMusicActivity.musicList.size - 1 else index--
                        "search" -> if (index <= 0) index =
                            PlayMusicActivity.songSearchList.size - 1 else index--
                        "offline" -> if (index <= 0) index =
                            PlayMusicActivity.songLocalList.size - 1 else index--
                        "favourite" -> if (index <= 0) index =
                            PlayMusicActivity.songFavouriteList.size - 1 else index--
                    }
                }
            }
            PlayMusicActivity.indexSong = index
            //check Type
            val songIndex = when (ApplicationClass.type) {
                "chart-realtime" -> PlayMusicActivity.musicList[index]
                "search" -> PlayMusicActivity.songSearchList[index]
                "offline" -> PlayMusicActivity.songLocalList[index]
                else -> PlayMusicActivity.songFavouriteList[index]
            }
            if (songIndex is com.example.musicplayer.model.Song) {
                songNameNP.text = songIndex.name
                Glide.with(baseContext).load(songIndex.thumbnail).into(imageNP)
                PlayMusicActivity.currentSongName = songIndex.name
                PlayMusicActivity.currentSongArtist = songIndex.artists_names
                PlayMusicActivity.currentSongThumb = songIndex.thumbnail
                PlayMusicActivity.currentID = songIndex.id
            } else if (songIndex is com.example.musicplayer.model.apisearch.Song) {
                songNameNP.text = songIndex.name
                Glide.with(baseContext).load("https://photo-resize-zmp3.zadn.vn/" + songIndex.thumb)
                    .into(imageNP)
                PlayMusicActivity.currentSongName = songIndex.name
                PlayMusicActivity.currentSongArtist = songIndex.artist
                PlayMusicActivity.currentSongThumb = songIndex.thumb
                PlayMusicActivity.currentID = songIndex.id
            } else if (songIndex is MusicAudioLocal) {
                songNameNP.text = songIndex.name
                imageNP.setImageResource(R.drawable.musical_note)
                PlayMusicActivity.currentSongName = songIndex.name
                PlayMusicActivity.currentSongArtist = songIndex.author
                PlayMusicActivity.currentSongThumb = ""
                PlayMusicActivity.currentID = ""
            } else if (songIndex is SongFavourite) {
                songNameNP.text = songIndex.name
                if (!songIndex.isOnline)
                    imageNP.setImageResource(R.drawable.musical_note)
                else Glide.with(baseContext).load(songIndex.thumb).into(imageNP)
                PlayMusicActivity.currentSongName = songIndex.name
                PlayMusicActivity.currentSongArtist = songIndex.artist
                PlayMusicActivity.currentSongThumb = songIndex.thumb
                PlayMusicActivity.currentID = songIndex.id
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

        rcvListSong.layoutManager = LinearLayoutManager(this)
        rcvListSong.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        //load api chart-realtime
        progressLoadingHome.visibility = View.VISIBLE
        getTopSong()

        //Search Music by name in API
        search_bar.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                progressLoadingHome.visibility = View.VISIBLE
                getSongSearchFormAPI(p0!!)
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                if (p0.isNullOrEmpty()) {
                    getTopSong()
                }
                return true
            }
        })

        //Library
        btnLibrary.setOnClickListener {
            if (checkPermission()) {
                loadLocalSongFromDevice()
            }
        }


        tvTopMusic.setOnClickListener {
            rcvListSong.adapter = chartRealTimeAdapter
        }
//        songFavouriteAdapter = SongFavouriteAdapter(songFavouriteList, this)

        btnFavourite.setOnClickListener {
            //get Favourite Song
            getSongFavourite()
        }


    }

    private fun getTopSong() {
        musicTopViewModel = ViewModelProvider(this)[MusicTopViewModel::class.java]
        musicTopViewModel.getTopSongFromAPI()

        musicTopViewModel.mSongTopLiveData.observe(this, {
            chartRealTimeAdapter = SongAdapter(it, this)
            rcvListSong.adapter = chartRealTimeAdapter

            ApplicationClass.listChartRealtime.clear()
            ApplicationClass.listChartRealtime.addAll(it)

            chartRealTimeAdapter.setOnSongClick { index ->
                val intent = Intent(this, PlayMusicActivity::class.java)
                //intent.putExtra("songFromHome", ApplicationClass.listChartRealtime[it])
                intent.putExtra("indexSong", index)
                ApplicationClass.type = "chart-realtime"
                intent.putExtra("type", "chart-realtime")
                startActivity(intent)
            }
            progressLoadingHome.visibility = View.GONE
        })
    }

    private fun getSongFavourite() {
        val songFavouriteViewModel = ViewModelProvider(this)[MusicFavouriteViewModel::class.java]
        songFavouriteViewModel.getSongFavourite(this)
        songFavouriteViewModel.mSongFavouriteLiveData.observe(this,{
            songFavouriteList = it
            songFavouriteAdapter = SongFavouriteAdapter(songFavouriteList, this)
            songFavouriteAdapter.setOnSongClick {
                //click
                ApplicationClass.type = "favourite"
                val intent = Intent(this, PlayMusicActivity::class.java)
                intent.putExtra("indexSong", it)
                startActivity(intent)
            }
            rcvListSong.adapter = songFavouriteAdapter
        })

    }

    private fun getSongSearchFormAPI(query: String) {
        val songSearchViewModel = ViewModelProvider(this)[MusicSearchViewModel::class.java]
        songSearchViewModel.getSongSearchFromAPI(query)
        songSearchViewModel.mSongSearchLiveData.observe(this, {
            if (it != null) {
                songSearchList.clear()
                ApplicationClass.listSongSearch.clear()
                songSearchList.addAll(it)
                ApplicationClass.listSongSearch.addAll(songSearchList)
                tvError.visibility = View.GONE
                songSearchAdapter = SongSearchAdapter(songSearchList, this)
                songSearchAdapter.setOnSongClick { index ->
                    val intent = Intent(this, PlayMusicActivity::class.java)
                    intent.putExtra("indexSong", index)
                    ApplicationClass.type = "search"
                    intent.putExtra("type", "search")
                    startActivity(intent)
                }
                rcvListSong.adapter = songSearchAdapter
            } else {
                rcvListSong.visibility = View.GONE
                tvError.text = "Không có bài hát nào!"
                tvError.visibility = View.VISIBLE
            }
            progressLoadingHome.visibility = View.GONE
        })
    }

    @SuppressLint("Range")
    private fun loadLocalSongFromDevice() {
        listSongLocal.clear()
        val localViewModel = ViewModelProvider(this)[MusicLocalViewModel::class.java]
        localViewModel.getMusicLocal()
        localViewModel.mMusicLocalLiveData.observe(this, {
            listSongLocal = it
            musicLocalAdapter = MusicLocalAdapter(listSongLocal)
            musicLocalAdapter.setOnClickSongItem {
                //intent to PlayMusicActivity
                val intent = Intent(this, PlayMusicActivity::class.java)
                ApplicationClass.type = "offline"
                intent.putExtra("indexSong", it)
                startActivity(intent)
            }
            rcvListSong.adapter = musicLocalAdapter
        })
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