package com.example.musicplayer.model

data class Music(
    val `data`: Data,
    val err: Int,
    val msg: String,
    val timestamp: Long
)