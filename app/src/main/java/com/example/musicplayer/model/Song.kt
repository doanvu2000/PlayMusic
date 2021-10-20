package com.example.musicplayer.model

data class Song(
    val album: Album,
    val artist: ArtistX,
    val artists: List<ArtistXX>,
    val artists_names: String,
    val code: String,
    val content_owner: Int,
    val duration: Int,
    val id: String,
    val isWorldWide: Boolean,
    val isoffical: Boolean,
    val link: String,
    val lyric: String,
    val mv_link: String,
    val name: String,
    val order: Any,
    val performer: String,
    val playlist_id: String,
    val position: Int,
    val rank_num: Any,
    val rank_status: String,
    val thumbnail: String,
    val title: String,
    val total: Int,
    val type: String
)