package com.example.musicplayer.activity

import android.content.*
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.musicplayer.ApplicationClass
import com.example.musicplayer.R
import com.example.musicplayer.adapter.SongRecommendAdapter
import com.example.musicplayer.api.ApiChartRealtime
import com.example.musicplayer.model.Song
import com.example.musicplayer.model.apisearch.Item
import com.example.musicplayer.model.apisearch.MusicRecommend
import com.example.musicplayer.receiver.NotificationReceiver
import com.example.musicplayer.service.MusicService
import kotlinx.android.synthetic.main.activity_play_music.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
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
        createMedia(song!!.id, song!!.name, song!!.artists_names, song!!.thumbnail, song!!.duration)
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as MusicService.MyBinder
        musicService = binder.currentService()
        createMedia(song!!.id, song!!.name, song!!.artists_names, song!!.thumbnail, song!!.duration)
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
            createMedia(
                song!!.id,
                song!!.name,
                song!!.artists_names,
                song!!.thumbnail,
                song!!.duration
            )
        }
    }
    var broadcastPlayPause = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            val flag = p1?.getStringExtra("flag")
            if (flag == "play") playAudio() else pauseAudio()
        }
    }


    companion object {
        lateinit var musicList: MutableList<Song>
        var listSong: MutableList<Song> = ArrayList()
        var indexSong = 0
        var isPlaying: Boolean = false
        var musicService: MusicService? = null
        var repeatOne = false
        var isShuffle = false
        var song: Song? = null
        var recommendList: MutableList<Item> = ArrayList()
        lateinit var recommendAdapter: SongRecommendAdapter
        var currentSongName: String = ""
        var currentSongArtist: String = ""
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
        Log.d(TAG, "onCreate flag: $flag")
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
        Log.d(TAG, "indexSong: $indexSong")
        musicList = ArrayList()
        musicList.addAll(ApplicationClass.listChartRealtime)

//        listSong.addAll(ApplicationClass.listChartRealtime)

        song = musicList[indexSong]
        if (song != null) {
            tvNameSong.text = song!!.name
            tvArtistsPlay.text = song!!.artists_names
            val url = song!!.thumbnail
            val token = url.split("/")
            val rm = token[3]
            val shortUrl =
                url.substring(0, url.indexOf(rm)) + url.substring(url.indexOf(rm) + rm.length + 1)
            Glide.with(this).load(shortUrl).into(imageSongPlay)
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
            } else {
                indexSong = Random.nextInt(0, musicList.size)
            }
            song = musicList[indexSong]
            createMedia(
                song!!.id,
                song!!.name,
                song!!.artists_names,
                song!!.thumbnail,
                song!!.duration
            )
        }
        btnNext.setOnClickListener {
            if (!isShuffle) {
                if (indexSong < musicList.size - 1) {
                    indexSong++
                } else {
                    indexSong = 0
                }
            } else {
                indexSong = Random.nextInt(0, musicList.size)
            }
            song = musicList[indexSong]
            createMedia(
                song!!.id,
                song!!.name,
                song!!.artists_names,
                song!!.thumbnail,
                song!!.duration
            )
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
        getListRecommend(song!!.id)
        recommendAdapter = SongRecommendAdapter(recommendList, this)
        rcvSongRecommend.adapter = recommendAdapter
        rcvSongRecommend.layoutManager = LinearLayoutManager(this)
        recommendAdapter.setOnSongClick {
            val recommendSong = recommendList[it]
            createMedia(
                recommendSong.id,
                recommendSong.name,
                recommendSong.artists_names,
                recommendSong.thumbnail,
                recommendSong.duration
            )
        }
    }

    private fun getListRecommend(id: String) {
        ApiChartRealtime.api.getSongRecommend("audio", id)
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
            if (musicService!!.mediaPlayer == null) {
                musicService!!.mediaPlayer = MediaPlayer()
            }
            musicService!!.mediaPlayer!!.reset()
            tvNameSong.text = name
            currentSongName = name

            tvArtistsPlay.text = artistsNames
            currentSongArtist = artistsNames
            val token = urlImage.split("/")
            val rm = token[3]
            val shortUrl =
                urlImage.substring(
                    0,
                    urlImage.indexOf(rm)
                ) + urlImage.substring(urlImage.indexOf(rm) + rm.length + 1)
            Glide.with(this).load(shortUrl).into(imageSongPlay)
            getListRecommend(id)
            //set path
            musicService!!.mediaPlayer!!.setDataSource(this, getUrlPlayOnline(id))
            musicService!!.mediaPlayer!!.prepare()
            musicService!!.mediaPlayer!!.start()
            isPlaying = true
            btnPlay.setImageResource(R.drawable.ic_pause)
            musicService!!.showNotification(R.drawable.ic_pause)
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

}