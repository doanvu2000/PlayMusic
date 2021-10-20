package com.example.musicplayer.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ArtistX(
    val cover: String,
    val id: String,
    val link: String,
    val name: String,
    val thumbnail: String
):Parcelable