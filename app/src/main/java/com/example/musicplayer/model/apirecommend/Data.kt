package com.example.musicplayer.model.apirecommend

data class Data(
    val image_url: String,
    val items: List<Item>,
    val total: Int
)