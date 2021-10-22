package com.example.musicplayer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.R
import com.example.musicplayer.`object`.MusicAudioLocal
import kotlinx.android.synthetic.main.item_music_audio_local.view.*

class MusicAdapter(var listSong: MutableList<MusicAudioLocal>) :
    RecyclerView.Adapter<MusicAdapter.ViewHolder>() {
    lateinit var onClick: (position: Int) -> Unit
    fun setOnClickSongItem(click: (position: Int) -> Unit) {
        onClick = click
    }

    inner class ViewHolder(itemview: View) : RecyclerView.ViewHolder(itemview) {
        fun bindData(song: MusicAudioLocal) {
            itemView.tvSongName.text = "${adapterPosition + 1}. ${song.name}"
            itemView.tvAuthor.text = song.author
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