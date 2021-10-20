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
import com.example.musicplayer.R
import com.example.musicplayer.activity.PlayMusicActivity
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
            val song = PlayMusicActivity.musicList[PlayMusicActivity.indexSong]
            songNameNP.text = "${song.position}.${song.name}"
            Glide.with(this).load(song.thumbnail).into(imageNP)
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
        if (PlayMusicActivity.isShuffle) index = Random.nextInt(0, PlayMusicActivity.musicList.size)
        else {
            if (index < PlayMusicActivity.musicList.size - 1) {
                index++
            } else {
                index = 0
            }
        }
        PlayMusicActivity.indexSong = index
        PlayMusicActivity.musicService!!.createMedia()
        val songIndex = PlayMusicActivity.musicList[index]
        songNameNP.text = "${songIndex.position}. " + songIndex.name
        Glide.with(this).load(songIndex.thumbnail).into(imageNP)
        PlayMusicActivity.musicService!!.showNotification(R.drawable.ic_pause)
        playSong()
    }

    private fun prevSong() {
        var index = PlayMusicActivity.indexSong
        if (PlayMusicActivity.isShuffle) index = Random.nextInt(0, PlayMusicActivity.musicList.size)
        else {
            if (index > 0) {
                index--
            } else {
                index = PlayMusicActivity.musicList.size - 1
            }
        }
        PlayMusicActivity.indexSong = index
        PlayMusicActivity.musicService!!.createMedia()
        val songIndex = PlayMusicActivity.musicList[index]
        songNameNP.text = "${songIndex.position}. " + songIndex.name
        Glide.with(this).load(songIndex.thumbnail).into(imageNP)
        PlayMusicActivity.musicService!!.showNotification(R.drawable.ic_pause)
        playSong()
    }
}