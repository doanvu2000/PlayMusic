package com.example.musicplayer.api

import com.example.musicplayer.model.Music
import com.example.musicplayer.model.apisearch.MusicRecommend
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiChartRealtime {

    @GET("xhr/chart-realtime")
    fun getSong(): Call<Music>

    @GET("xhr/recommend")
    fun getSongRecommend(@Query("type") type: String, @Query("id") id: String): Call<MusicRecommend>

    companion object {
        val api = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://mp3.zing.vn/")
            .build().create(ApiChartRealtime::class.java)
    }
}