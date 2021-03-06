package com.example.musicplayer.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicplayer.R
import com.example.musicplayer.model.Song
import kotlinx.android.synthetic.main.item_song_chart_realtime.view.*

class SongAdapter(val listSong: MutableList<Song>, val context: Context) :
    RecyclerView.Adapter<SongAdapter.ViewHolder>() {
    lateinit var onclick: (position: Int) -> Unit

    fun setOnSongClick(event: (it: Int) -> Unit) {
        onclick = event
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun bindData(song: Song) {
            when (adapterPosition) {
                0 -> itemView.tvRank.setTextColor(Color.parseColor("#FF6666"))
                1 -> itemView.tvRank.setTextColor(Color.parseColor("#33FF00"))
                2 -> itemView.tvRank.setTextColor(Color.parseColor("#FF9900"))
                else -> itemView.tvRank.setTextColor(Color.parseColor("#FFFFFF"))
            }
            itemView.tvRank.text = "${adapterPosition + 1}"
            itemView.tvName.text = song.name
            itemView.tvArtists.text = song.artists_names
            Glide.with(context).load(song.thumbnail).into(itemView.image)
        }

        init {
            itemView.setOnClickListener {
                onclick.invoke(adapterPosition)
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