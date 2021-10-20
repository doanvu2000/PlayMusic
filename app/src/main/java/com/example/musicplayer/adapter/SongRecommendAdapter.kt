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
import com.example.musicplayer.model.apisearch.Item
import kotlinx.android.synthetic.main.item_song_chart_realtime.view.*

class SongRecommendAdapter(val listSong: MutableList<Item>, val context: Context) :
    RecyclerView.Adapter<SongRecommendAdapter.ViewHolder>() {
    lateinit var onclick: (position: Int) -> Unit

    fun setOnSongClick(event: (it: Int) -> Unit) {
        onclick = event
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun bindData(item: Item) {
            itemView.tvRank.setTextColor(Color.parseColor("#ffffff"))
            itemView.tvRank.text = "${adapterPosition + 1}"
            itemView.tvName.text = item.name
            itemView.tvArtists.text = item.artists_names
            Glide.with(context).load(item.thumbnail).into(itemView.image)
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

    fun timerConversion(sec: Int): String {
        var sencond = sec
        val audioTime: String
        val hrs = sencond / 3600
        sencond -= hrs * 3600
        val mns = sencond / 60 % 60
        sencond -= mns * 60
        audioTime = if (hrs > 0) {
            String.format("%02d:%02d:%02d", hrs, mns, sencond)
        } else {
            String.format("%02d:%02d", mns, sencond)
        }
        return audioTime
    }
}