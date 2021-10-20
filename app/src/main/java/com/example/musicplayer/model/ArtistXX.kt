package com.example.musicplayer.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ArtistXX(
    val link: String,
    val name: String
):Parcelable