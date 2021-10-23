package com.example.musicplayer.database

data class SongFavourite(
    val id: String,
    val name: String,
    val artist: String,
    val duration: Int,
    val url: String,
    val thumb: String,
    val isOnline: Boolean
)
