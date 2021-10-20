package com.example.musicplayer.model.apisearch

data class MusicRecommend(
    val `data`: Data,
    val err: Int,
    val msg: String,
    val timestamp: Long
)