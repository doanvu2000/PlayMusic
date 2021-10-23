package com.example.musicplayer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.R
import com.example.musicplayer.`object`.MusicAudioLocal
import kotlinx.android.synthetic.main.item_music_audio_local.view.*
import java.util.concurrent.TimeUnit

class MusicLocalAdapter(var listSong: MutableList<MusicAudioLocal>) :
    RecyclerView.Adapter<MusicLocalAdapter.ViewHolder>() {
    lateinit var onClick: (position: Int) -> Unit
    fun setOnClickSongItem(click: (position: Int) -> Unit) {
        onClick = click
    }

    inner class ViewHolder(itemview: View) : RecyclerView.ViewHolder(itemview) {
        fun bindData(song: MusicAudioLocal) {
            itemView.tvSongName.text = "${adapterPosition + 1}. ${song.name}"
            val mns: Long = song.duration.toLong()
            var minutes = TimeUnit.MILLISECONDS.toMinutes(mns)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(mns) - TimeUnit.MINUTES.toSeconds(minutes)
            val hours = minutes / 60
            minutes -= hours * 60
            var audioTime = if (hours > 0) {
                String.format("%02d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%02d:%02d", minutes, seconds)
            }
            itemView.tvAuthor.text = song.author + " " + audioTime
        }

        init {
            itemview.setOnClickListener {
                onClick.invoke(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_music_audio_local, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(listSong[position])
    }

    override fun getItemCount(): Int {
        return listSong.size
    }
}