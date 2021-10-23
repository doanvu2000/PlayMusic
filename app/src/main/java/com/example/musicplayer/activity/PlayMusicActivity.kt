package com.example.musicplayer.activity

import android.Manifest
import android.app.DownloadManager
import android.content.*
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.util.Log
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.musicplayer.ApplicationClass
import com.example.musicplayer.R
import com.example.musicplayer.`object`.MusicAudioLocal
import com.example.musicplayer.adapter.SongRecommendAdapter
import com.example.musicplayer.api.ApiMusic
import com.example.musicplayer.database.SongFavourite
import com.example.musicplayer.model.Song
import com.example.musicplayer.model.apirecommend.Item
import com.example.musicplayer.model.apirecommend.MusicRecommend
import com.example.musicplayer.receiver.NotificationReceiver
import com.example.musicplayer.service.MusicService
import kotlinx.android.synthetic.main.activity_play_music.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.TimeUnit
import kotlin.Exception
import kotlin.random.Random


class PlayMusicActivity : AppCompatActivity(), ServiceConnection, MediaPlayer.OnCompletionListener {

    companion object {
        lateinit var musicList: MutableList<Song>
        var songSearchList: MutableList<com.example.musicplayer.model.apisearch.Song> = ArrayList()
        var recommendList: MutableList<Item> = ArrayList()
        var songLocalList: MutableList<MusicAudioLocal> = ArrayList()
        var songFavouriteList: MutableList<SongFavourite> = ArrayList()

        var indexSong = 0
        var isPlaying: Boolean = false
        var musicService: MusicService? = null
        var repeatOne = false
        var isShuffle = false
        var isFavourite = false
        var song: Song? = null
        var songSearch: com.example.musicplayer.model.apisearch.Song? = null
        var songLocal: MusicAudioLocal? = null
        var songFavourite: SongFavourite? = null

        lateinit var recommendAdapter: SongRecommendAdapter
        var currentSongName: String = ""
        var currentSongArtist: String = ""
        var currentSongThumb: String = ""
        var currentID: String = ""

        private val TAG = "PlayMusicActivity"
        lateinit var notificationReceiver: NotificationReceiver
    }


    private var broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            indexSong = intent!!.getIntExtra("indexSong", 0)
            setCurrentSong()
        }
    }
    private var broadcastPlayPause = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            val flag = p1?.getStringExtra("flag")
            if (flag == "play") playAudio() else pauseAudio()
        }
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
        indexSong = intent.getIntExtra("indexSong", 0)

        if (flag != "resumePlay") { //start service
            val intentService = Intent(this, MusicService::class.java)
            bindService(intentService, this, BIND_AUTO_CREATE)
            startService(intentService)
        } else {

            tvNameSong.text = currentSongName
            tvArtistsPlay.text = currentSongArtist
            val url = currentSongThumb
            try {
                when (ApplicationClass.type) {
                    "chart-realtime" -> {
                        val token = url.split("/")
                        val rm = token[3]
                        val shortUrl =
                            url.substring(
                                0,
                                url.indexOf(rm)
                            ) + url.substring(url.indexOf(rm) + rm.length + 1)
                        Glide.with(this).load(shortUrl).into(imageSongPlay)
                    }
                    "search" -> {
                        Glide.with(this).load(currentSongThumb).into(imageSongPlay)
                    }
                    "offline" -> {
                        imageSongPlay.setImageResource(R.drawable.musical_note)
                    }
                    "favourite" -> {
                        if (songFavourite!!.isOnline) {
                            Glide.with(this).load(songFavourite!!.thumb).into(imageSongPlay)
                        }
                    }
                }
            } catch (ex: Exception) {
                Log.e(TAG, "onCreate: ${ex.message}")
                imageSongPlay.setImageResource(R.drawable.musical_note)
            }

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
        musicList = ArrayList()
        when (ApplicationClass.type) {
            "chart-realtime" -> {
                musicList.clear()
                musicList.addAll(ApplicationClass.listChartRealtime)
                song = musicList[indexSong]
                currentSongName = song!!.name
                currentSongArtist = song!!.artists_names
                currentSongThumb = song!!.thumbnail
                currentID = song!!.id
            }
            "search" -> {
                songSearchList.clear()
                songSearchList.addAll(ApplicationClass.listSongSearch)
                songSearch = songSearchList[indexSong]
                currentSongName = songSearch!!.name
                currentID = songSearch!!.id
                currentSongArtist = songSearch!!.artist
                currentSongThumb = "https://photo-resize-zmp3.zadn.vn/" + songSearch!!.thumb
            }
            "offline" -> {
                songLocalList.clear()
                songLocalList.addAll(ApplicationClass.listSongLocal)
                songLocal = songLocalList[indexSong]
                currentSongName = songLocal!!.name
                currentID = ""
                currentSongArtist = songLocal!!.author
                currentSongThumb = ""
            }
            "favourite" -> {
                songFavouriteList.clear()
                songFavouriteList.addAll(ApplicationClass.listSongFavourite)
                songFavourite = songFavouriteList[indexSong]
                currentSongName = songFavourite!!.name
                currentID = songFavourite!!.id
                currentSongArtist = songFavourite!!.artist
                currentSongThumb = songFavourite!!.thumb
            }
        }
        if (flag != "resumePlay") {
            tvNameSong.text = currentSongName
            tvArtistsPlay.text = currentSongArtist
            val url = currentSongThumb
            try {
                when (ApplicationClass.type) {
                    "chart-realtime" -> {
                        val token = url.split("/")
                        val rm = token[3]
                        val shortUrl =
                            url.substring(
                                0,
                                url.indexOf(rm)
                            ) + url.substring(url.indexOf(rm) + rm.length + 1)
                        Glide.with(this).load(shortUrl).into(imageSongPlay)
                    }
                    "search" -> {
                        Glide.with(this).load(currentSongThumb).into(imageSongPlay)
                    }
                    "offline" -> {
                        imageSongPlay.setImageResource(R.drawable.musical_note)
                    }
                    "favourite" -> {
                        if (songFavourite!!.isOnline) {
                            Glide.with(this).load(songFavourite!!.thumb).into(imageSongPlay)
                        }
                    }
                }
            } catch (ex: Exception) {
                Log.e(TAG, "onCreate: ${ex.message}")
                imageSongPlay.setImageResource(R.drawable.musical_note)
            }
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
                when (ApplicationClass.type) {
                    "chart-realtime" -> {
                        if (indexSong <= 0) indexSong = musicList.size - 1 else indexSong--
                    }
                    "search" -> {
                        if (indexSong <= 0) indexSong = songSearchList.size - 1 else indexSong--
                    }
                    "offline" -> {
                        if (indexSong <= 0) indexSong = songLocalList.size - 1 else indexSong--
                    }
                    "favourite" -> {
                        if (indexSong <= 0) indexSong = songFavouriteList.size - 1 else indexSong--
                    }
                }
            } else {
                when (ApplicationClass.type) {
                    "chart-realtime" -> indexSong = Random.nextInt(0, musicList.size)
                    "search" -> indexSong = Random.nextInt(0, songSearchList.size)
                    "offline" -> indexSong = Random.nextInt(0, songLocalList.size)
                    "favourite" -> indexSong = Random.nextInt(0, songFavouriteList.size)
                }
            }
            setCurrentSong()
        }
        btnNext.setOnClickListener {
            if (!isShuffle) {
                when (ApplicationClass.type) {
                    "chart-realtime" -> if (indexSong < musicList.size - 1) indexSong++ else indexSong =
                        0
                    "search" -> if (indexSong < songSearchList.size - 1) indexSong++ else indexSong =
                        0
                    "offline" -> if (indexSong < songLocalList.size) indexSong++ else indexSong = 0
                    "favourite" -> if (indexSong < songFavouriteList.size) indexSong++ else indexSong =
                        0
                }
            } else {
                when (ApplicationClass.type) {
                    "chart-realtime" -> indexSong = Random.nextInt(0, musicList.size)
                    "search" -> indexSong = Random.nextInt(0, songSearchList.size)
                    "offline" -> indexSong = Random.nextInt(0, songLocalList.size)
                    "favourite" -> indexSong = Random.nextInt(0, songFavouriteList.size)
                }
            }
            setCurrentSong()
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
            if (isShuffle) {
                btnShuffleSong.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
            } else {
                btnShuffleSong.setColorFilter(ContextCompat.getColor(this, R.color.white))
            }
        }

        //storage runtime permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 100)
            }
        }
        var myDownload: Long = 0
        btnDownload.setOnClickListener {

            val url = getUrlPlayOnline(currentID)
            Log.d(TAG, "url: $url")
            val request = DownloadManager.Request(url)
                .setTitle(currentSongName)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedOverMetered(true)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, currentSongName)
            var dm = (getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager)
            myDownload = dm.enqueue(request)
        }
        var brDownload = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                var id: Long? = p1?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == myDownload) {
                    Toast.makeText(baseContext, "Download complete", Toast.LENGTH_SHORT).show()
                }
            }
        }
        registerReceiver(brDownload, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if (p2) musicService!!.mediaPlayer?.seekTo(p1)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })

        //call api recommend
        if (ApplicationClass.type == "chart-realtime")
            getListRecommend(song!!.id)
        else if (ApplicationClass.type == "search") {
            getListRecommend(songSearch!!.id)
        }
        recommendAdapter = SongRecommendAdapter(recommendList, this)
        rcvSongRecommend.adapter = recommendAdapter
        rcvSongRecommend.layoutManager = LinearLayoutManager(this)
        recommendAdapter.setOnSongClick {
            val recommendSong = recommendList[it]
            currentID = recommendSong.id
            createMedia(
                recommendSong.id,
                recommendSong.name,
                recommendSong.artists_names,
                recommendSong.thumbnail,
                recommendSong.duration
            )
        }

        btnFavourite.setOnClickListener {
            isFavourite = !isFavourite
            if (isFavourite)
                btnFavourite.setImageResource(R.drawable.bg)
            else btnFavourite.setImageResource(R.drawable.ic_favorite_border)

        }
    }

    private fun checkFavourite(id: String, name: String, artistsNames: String): Boolean {
        val songCheck = SongFavourite(id, name, artistsNames, 0, "", "", false)
        for (i in 0 until songFavouriteList.size) {

        }
        return true
    }

    private fun setCurrentSong() {
        when (ApplicationClass.type) {
            "chart-realtime" -> {
                song = musicList[indexSong]
                createMedia(
                    song!!.id,
                    song!!.name,
                    song!!.artists_names,
                    song!!.thumbnail,
                    song!!.duration
                )
            }
            "search" -> {
                songSearch = songSearchList[indexSong]
                createMedia(
                    songSearch!!.id,
                    songSearch!!.name,
                    songSearch!!.artist,
                    "https://photo-resize-zmp3.zadn.vn/" + songSearch!!.thumb,
                    songSearch!!.duration.toInt()
                )
            }
            "offline" -> {
                songLocal = songLocalList[indexSong]
                createMedia("", songLocal!!.name, songLocal!!.author, "", songLocal!!.duration)
            }
            "favourite" -> {
                songFavourite = songFavouriteList[indexSong]
                createMedia(
                    songFavourite!!.id,
                    songFavourite!!.name,
                    songFavourite!!.artist,
                    songFavourite!!.thumb,
                    songFavourite!!.duration
                )
            }
        }
    }

    private fun getListRecommend(id: String) {
        ApiMusic.api.getSongRecommend("audio", id)
            .enqueue(object : Callback<MusicRecommend> {
                override fun onResponse(
                    call: Call<MusicRecommend>,
                    response: Response<MusicRecommend>
                ) {
                    recommendList.clear()
                    recommendList.addAll(response.body()!!.data.items)
                    recommendAdapter.notifyDataSetChanged()
                }

                override fun onFailure(call: Call<MusicRecommend>, t: Throwable) {
                    Log.e(TAG, "error on call api recommend: ${t.message}")
                }
            })
    }


    private fun getUrlPlayOnline(id: String): Uri {
        return Uri.parse("http://api.mp3.zing.vn/api/streaming/audio/$id/320")
    }

    fun createMedia(
        id: String,
        name: String,
        artistsNames: String,
        urlImage: String,
        duration: Int
    ) {
        try {
            currentID = id
            if (musicService!!.mediaPlayer == null) {
                musicService!!.mediaPlayer = MediaPlayer()
            }
            musicService!!.mediaPlayer!!.reset()
            tvNameSong.text = name
            currentSongName = name

            tvArtistsPlay.text = artistsNames
            currentSongArtist = artistsNames
            currentSongThumb = urlImage
            try {
                when (ApplicationClass.type) {
                    "chart-realtime" -> {
                        val token = urlImage.split("/")
                        val rm = token[3]
                        val shortUrl =
                            urlImage.substring(
                                0,
                                urlImage.indexOf(rm)
                            ) + urlImage.substring(urlImage.indexOf(rm) + rm.length + 1)
                        Glide.with(this).load(shortUrl).into(imageSongPlay)
                    }
                    "search" -> {
                        Glide.with(this).load(currentSongThumb).into(imageSongPlay)
                    }
                    "offline" -> {
                        imageSongPlay.setImageResource(R.drawable.musical_note)
                    }
                    "favourite" -> {
                        if (songFavourite!!.isOnline) {
                            Glide.with(this).load(songFavourite!!.thumb).into(imageSongPlay)
                        }
                    }
                }
            } catch (ex: Exception) {
                Log.e(TAG, "onCreate: ${ex.message}")
                imageSongPlay.setImageResource(R.drawable.musical_note)
            }
            getListRecommend(id)
            //set path
            when (ApplicationClass.type) {
                "chart-realtime", "search" -> {
                    musicService!!.mediaPlayer!!.setDataSource(this, getUrlPlayOnline(id))
                }
                "offline" -> {
                    musicService!!.mediaPlayer!!.setDataSource(songLocal!!.url)
                }
                "favourite" -> {
                    if (songFavourite!!.isOnline)
                        musicService!!.mediaPlayer!!.setDataSource(
                            this,
                            getUrlPlayOnline(songFavourite!!.id)
                        )
                    else
                        musicService!!.mediaPlayer!!.setDataSource(songFavourite!!.url)
                }
            }
            musicService!!.mediaPlayer!!.prepare()
            musicService!!.mediaPlayer!!.start()
            isPlaying = true
            btnPlay.setImageResource(R.drawable.ic_pause)
            musicService!!.showNotification(R.drawable.ic_pause)
            Log.d(TAG, "push notify in createMedia() PlayMusicActivity")
            setAudioProgress(duration)
            musicService!!.mediaPlayer!!.setOnCompletionListener(this)
        } catch (ex: Exception) {
            Log.e(TAG, "createMedia-error: ${ex.message}")
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

    private fun setAudioProgress(duration: Int) {
        val totalDuration = duration * 1000
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
        Log.d(TAG, "timerConversion: $value")
        var audioTime: String = ""

        when (ApplicationClass.type) {
            "chart-realtime", "search" -> {
                val hrs = value / 3600000
                val mns = value / 60000 % 60000
                val scs = value % 60000 / 1000

                audioTime = if (hrs > 0) {
                    String.format("%02d:%02d:%02d", hrs, mns, scs)
                } else {
                    String.format("%02d:%02d", mns, scs)
                }
            }
            "offline" -> {

                val mns: Long = (value / 1000).toLong()
                var minutes = TimeUnit.MILLISECONDS.toMinutes(mns)
                val seconds =
                    TimeUnit.MILLISECONDS.toSeconds(mns) - TimeUnit.MINUTES.toSeconds(minutes)
                val hours = minutes / 60
                minutes -= hours * 60
                audioTime = if (hours > 0) {
                    String.format("%02d:%02d:%02d", hours, minutes, seconds)
                } else {
                    String.format("%02d:%02d", minutes, seconds)
                }
            }
            "favourite" -> {
                if (songFavourite!!.isOnline) {
                    val hrs = value / 3600000
                    val mns = value / 60000 % 60000
                    val scs = value % 60000 / 1000

                    audioTime = if (hrs > 0) {
                        String.format("%02d:%02d:%02d", hrs, mns, scs)
                    } else {
                        String.format("%02d:%02d", mns, scs)
                    }
                } else {
                    val mns: Long = (value / 1000).toLong()
                    var minutes = TimeUnit.MILLISECONDS.toMinutes(mns)
                    val seconds =
                        TimeUnit.MILLISECONDS.toSeconds(mns) - TimeUnit.MINUTES.toSeconds(minutes)
                    val hours = minutes / 60
                    minutes -= hours * 60
                    audioTime = if (hours > 0) {
                        String.format("%02d:%02d:%02d", hours, minutes, seconds)
                    } else {
                        String.format("%02d:%02d", minutes, seconds)
                    }
                }
            }
        }

        return audioTime
    }

    override fun onCompletion(p0: MediaPlayer?) {
        if (!repeatOne) {
            if (isShuffle) {//checkType
                when (ApplicationClass.type) {
                    "chart-realtime" -> indexSong = Random.nextInt(0, musicList.size)
                    "search" -> indexSong = Random.nextInt(0, songSearchList.size)
                    "offline" -> indexSong = Random.nextInt(0, songLocalList.size)
                    "favourite" -> indexSong = Random.nextInt(0, songFavouriteList.size)
                }
            } else {
                when (ApplicationClass.type) {
                    "chart-realtime" -> if (indexSong < musicList.size - 1) indexSong++ else indexSong =
                        0
                    "search" -> if (indexSong < songSearchList.size - 1) indexSong++ else indexSong =
                        0
                    "offline" -> if (indexSong < songLocalList.size) indexSong++ else indexSong = 0
                    "favourite" -> if (indexSong < songFavouriteList.size) indexSong++ else indexSong =
                        0
                }
            }
        }
        setCurrentSong()
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as MusicService.MyBinder
        musicService = binder.currentService()
        when (ApplicationClass.type) {
            "chart-realtime" -> {
                song = musicList[indexSong]
                createMedia(
                    song!!.id,
                    song!!.name,
                    song!!.artists_names,
                    song!!.thumbnail,
                    song!!.duration
                )
            }
            "search" -> {
                songSearch = songSearchList[indexSong]
                createMedia(
                    songSearch!!.id,
                    songSearch!!.name,
                    songSearch!!.artist,
                    "https://photo-resize-zmp3.zadn.vn/" + songSearch!!.thumb,
                    songSearch!!.duration.toInt()
                )
            }
            "offline" -> {
                songLocal = songLocalList[indexSong]
                createMedia("", songLocal!!.name, songLocal!!.author, "", songLocal!!.duration)
            }
            "favourite" -> {
                songFavourite = songFavouriteList[indexSong]
                createMedia(
                    songFavourite!!.id,
                    songFavourite!!.name,
                    songFavourite!!.artist,
                    songFavourite!!.thumb,
                    songFavourite!!.duration
                )
            }
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
    }


    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastPlayPause)
    }

}