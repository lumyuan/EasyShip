package com.pointer.wave.easyship.net

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

private val okhttp = OkHttpClient.Builder()
    .addInterceptor(LogInterceptor())
    .build()

val retrofit: Retrofit = Retrofit.Builder()
    .client(okhttp)
    .baseUrl("https://bot.k2t3k.tk/api/")
    .addConverterFactory(ScalarsConverterFactory.create())
    .addConverterFactory(GsonConverterFactory.create())
    .build()