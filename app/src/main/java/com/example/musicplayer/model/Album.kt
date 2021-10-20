package com.example.musicplayer.model
data class Album(
    val artists: List<Artist>,
    val artists_names: String,
    val id: String,
    val isoffical: Boolean,
    val link: String,
    val name: String,
    val thumbnail: String,
    val thumbnail_medium: String,
    val title: String
)