package com.pointer.wave.easyship.net.repo

import com.pointer.wave.easyship.net.Api
import com.pointer.wave.easyship.net.retrofit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.FormBody

class UpdateRepo {
    private val api by lazy { retrofit.create(Api::class.java) }

    suspend fun update() =
        flow {
            val body = FormBody.Builder().apply {
                add("id","103169318")
                add("api","easy_ship")
                add("dir","update")
                add("name","update.txt")
            }
            emit(
                api.tips(
                    body.build()
                )
            )
        }.flowOn(Dispatchers.IO)
}