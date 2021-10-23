package com.example.musicplayer.`object`

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MusicAudioLocal(var name: String, var duration: Int, var author: String, var url: String) : Parcelable