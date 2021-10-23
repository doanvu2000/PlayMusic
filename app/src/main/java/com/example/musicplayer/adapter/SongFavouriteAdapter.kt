package com.example.musicplayer.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicplayer.R
import com.example.musicplayer.database.SongFavourite
import com.example.musicplayer.model.apisearch.Song
import kotlinx.android.synthetic.main.item_song_chart_realtime.view.*

class SongFavouriteAdapter(val listSong: MutableList<SongFavourite>, val context: Context) :
    RecyclerView.Adapter<SongFavouriteAdapter.ViewHolder>() {
    var onclick: ((position: Int) -> Unit)? = null

    fun setOnSongClick(event: (it: Int) -> Unit) {
        onclick = event
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun bindData(song: SongFavourite) {
            itemView.tvRank.setTextColor(Color.parseColor("#FFFFFF"))
            itemView.tvRank.text = "${adapterPosition + 1}"
            itemView.tvName.text = song.name
            itemView.tvArtists.text = song.artist
            if (song.isOnline)
                Glide.with(context).load(song.thumb)
                    .into(itemView.image)
            else itemView.image.setImageResource(R.drawable.musical_note)
        }

        init {
            itemView.setOnClickListener {
                onclick!!.invoke(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song_chart_realtime, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(listSong[position])
    }

    override fun getItemCount(): Int {
        return listSong.size
    }
}