package com.example.musicplayer.model.apirecommend

data class MusicRecommend(
    val `data`: Data,
    val err: Int,
    val msg: String,
    val timestamp: Long
)