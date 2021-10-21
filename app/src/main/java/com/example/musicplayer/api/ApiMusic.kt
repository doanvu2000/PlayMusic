package com.example.musicplayer.api

import com.example.musicplayer.model.Music
import com.example.musicplayer.model.apirecommend.MusicRecommend
import com.example.musicplayer.model.apisearch.MusicSearch
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiMusic {

    @GET("xhr/chart-realtime")
    fun getSong(): Call<Music>

    @GET("xhr/recommend")
    fun getSongRecommend(@Query("type") type: String, @Query("id") id: String): Call<MusicRecommend>

    @GET("complete")
    fun getSongSearch(
        @Query("type") type: String, @Query("num") num: Int, @Query("query")
        query: String
    ): Call<MusicSearch>

    companion object {
        val api = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create())
            .baseUrl("http://mp3.zing.vn/")
            .build().create(ApiMusic::class.java)
        val search = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create())
            .baseUrl("http://ac.mp3.zing.vn/")
            .build()
            .create(ApiMusic::class.java)
    }
}