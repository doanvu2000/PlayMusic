package com.example.musicplayer.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.musicplayer.ApplicationClass
import com.example.musicplayer.R
import com.example.musicplayer.`object`.MusicAudioLocal
import com.example.musicplayer.activity.PlayMusicActivity
import com.example.musicplayer.database.SongFavourite
import com.example.musicplayer.model.Song
import kotlinx.android.synthetic.main.fragment_now_playing.*
import kotlinx.android.synthetic.main.fragment_now_playing.view.*
import kotlin.random.Random

class NowPlaying : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_now_playing, container, false)
        view.visibility = View.INVISIBLE
        view.btnPlayPauseNP.setOnClickListener {
            if (PlayMusicActivity.isPlaying) pauseSong() else playSong()
        }
        view.btnNextSongNP.setOnClickListener {
            nextSong()
        }
        view.btnPrevSongNP.setOnClickListener {
            prevSong()
        }
        view.setOnClickListener {
            val intent = Intent(requireContext(), PlayMusicActivity::class.java)
            intent.putExtra("indexSong", PlayMusicActivity.indexSong)
            intent.putExtra("currentSongName", PlayMusicActivity.currentSongName)
            intent.putExtra("currentSongArtist", PlayMusicActivity.currentSongArtist)
            intent.putExtra("currentSongThumb", PlayMusicActivity.currentSongThumb)
            intent.putExtra("flagMain", "resumePlay")
            ContextCompat.startActivity(requireContext(), intent, null)
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        if (PlayMusicActivity.musicService != null) {
            view?.visibility = View.VISIBLE
            view?.songNameNP?.isSelected = true
            songNameNP.text = PlayMusicActivity.currentSongName
            Glide.with(this).load(PlayMusicActivity.currentSongThumb).into(imageNP)
            if (PlayMusicActivity.isPlaying)
                btnPlayPauseNP.setImageResource(R.drawable.ic_pause)
            else btnPlayPauseNP.setImageResource(R.drawable.ic_play)
        }
    }

    private fun playSong() {
        PlayMusicActivity.musicService!!.mediaPlayer!!.start()
        btnPlayPauseNP.setImageResource(R.drawable.ic_pause)
        PlayMusicActivity.musicService!!.showNotification(R.drawable.ic_pause)
        PlayMusicActivity.isPlaying = true
    }

    private fun pauseSong() {
        PlayMusicActivity.musicService!!.mediaPlayer!!.pause()
        btnPlayPauseNP.setImageResource(R.drawable.ic_play)
        PlayMusicActivity.musicService!!.showNotification(R.drawable.ic_play)
        PlayMusicActivity.isPlaying = false
    }

    private fun nextSong() {
        var index = PlayMusicActivity.indexSong
        val type = ApplicationClass.type
        if (PlayMusicActivity.isShuffle) {
            when (type) {
                "chart-realtime" -> index = Random.nextInt(0, PlayMusicActivity.musicList.size)
                "search" -> index = Random.nextInt(0, PlayMusicActivity.songSearchList.size)
                "offline" -> index = Random.nextInt(0, PlayMusicActivity.songLocalList.size)
                "favourite" -> index = Random.nextInt(0, PlayMusicActivity.songFavouriteList.size)
            }
        } else {
            when (type) {
                "chart-realtime" -> if (index < PlayMusicActivity.musicList.size - 1) index++ else index =
                    0
                "search" -> if (index < PlayMusicActivity.songSearchList.size - 1) index++ else index =
                    0
                "offline" -> if (index < PlayMusicActivity.songLocalList.size - 1) index++ else index =
                    0
                "favourite" -> if (index < PlayMusicActivity.songFavouriteList.size - 1) index++ else index =
                    0
            }
        }
        PlayMusicActivity.indexSong = index
        PlayMusicActivity.musicService!!.createMedia()
        //check Type
        val songIndex = when (type) {
            "chart-realtime" -> PlayMusicActivity.musicList[index]
            "search" -> PlayMusicActivity.songSearchList[index]
            "offline" -> PlayMusicActivity.songLocalList[index]
            else -> PlayMusicActivity.songFavouriteList[index]
        }
        if (songIndex is Song) {
            songNameNP.text = songIndex.name
            Glide.with(this).load(songIndex.thumbnail).into(imageNP)
            PlayMusicActivity.currentSongName = songIndex.name
            PlayMusicActivity.currentSongArtist = songIndex.artists_names
            PlayMusicActivity.currentSongThumb = songIndex.thumbnail
            PlayMusicActivity.currentID = songIndex.id
        } else if (songIndex is com.example.musicplayer.model.apisearch.Song) {
            songNameNP.text = songIndex.name
            Glide.with(this).load("https://photo-resize-zmp3.zadn.vn/" + songIndex.thumb)
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
            else Glide.with(this).load(songIndex.thumb).into(imageNP)
            PlayMusicActivity.currentSongName = songIndex.name
            PlayMusicActivity.currentSongArtist = songIndex.artist
            PlayMusicActivity.currentSongThumb = songIndex.thumb
            PlayMusicActivity.currentID = songIndex.id
        }

        PlayMusicActivity.musicService!!.showNotification(R.drawable.ic_pause)
        Log.d("Activity", "push notify in next NowPlaying ")
        playSong()
    }

    private fun prevSong() {
        var index = PlayMusicActivity.indexSong
        val type = ApplicationClass.type
        if (PlayMusicActivity.isShuffle) {
            when (type) {
                "chart-realtime" -> index = Random.nextInt(0, PlayMusicActivity.musicList.size)
                "search" -> index = Random.nextInt(0, PlayMusicActivity.songSearchList.size)
                "offline" -> index = Random.nextInt(0, PlayMusicActivity.songLocalList.size)
                "favourite" -> index = Random.nextInt(0, PlayMusicActivity.songFavouriteList.size)
            }
        } else {
            when (type) {
                "chart-realtime" -> if (index <=0) index =  PlayMusicActivity.musicList.size - 1 else index--
                "search" -> if (index <=0) index =  PlayMusicActivity.songSearchList.size - 1 else index--
                "offline" -> if (index <=0) index =  PlayMusicActivity.songLocalList.size - 1 else index--
                "favourite" -> if (index <=0) index =  PlayMusicActivity.songFavouriteList.size - 1 else index--
            }
        }
        PlayMusicActivity.indexSong = index
        PlayMusicActivity.musicService!!.createMedia()
        //check Type
        val songIndex = when (type) {
            "chart-realtime" -> PlayMusicActivity.musicList[index]
            "search" -> PlayMusicActivity.songSearchList[index]
            "offline" -> PlayMusicActivity.songLocalList[index]
            else -> PlayMusicActivity.songFavouriteList[index]
        }
        if (songIndex is Song) {
            songNameNP.text = songIndex.name
            Glide.with(this).load(songIndex.thumbnail).into(imageNP)
            PlayMusicActivity.currentSongName = songIndex.name
            PlayMusicActivity.currentSongArtist = songIndex.artists_names
            PlayMusicActivity.currentSongThumb = songIndex.thumbnail
            PlayMusicActivity.currentID = songIndex.id
        } else if (songIndex is com.example.musicplayer.model.apisearch.Song) {
            songNameNP.text = songIndex.name
            Glide.with(this).load("https://photo-resize-zmp3.zadn.vn/" + songIndex.thumb)
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
            else Glide.with(this).load(songIndex.thumb).into(imageNP)
            PlayMusicActivity.currentSongName = songIndex.name
            PlayMusicActivity.currentSongArtist = songIndex.artist
            PlayMusicActivity.currentSongThumb = songIndex.thumb
            PlayMusicActivity.currentID = songIndex.id
        }
        PlayMusicActivity.musicService!!.showNotification(R.drawable.ic_pause)
        Log.d("Activity", "push notify in prev NowPlaying ")
        playSong()
    }
}