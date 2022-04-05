package com.pointer.wave.easyship.net

import com.pointer.wave.easyship.pojo.TipsBen
import okhttp3.FormBody
import retrofit2.http.Body
import retrofit2.http.POST

interface Api {
    @POST("http://ly.lumnytool.club/api/read.php")
    suspend fun tips(
        @Body body: FormBody
    ): TipsBen
}