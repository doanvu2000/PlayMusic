package com.example.musicplayer.api

import com.example.musicplayer.model.Music
import retrofit2.Call
import retrofit2.http.GET

interface ApiChartRealtime {

    @GET("xhr/chart-realtime")
    fun getSong(): Call<Music>
}