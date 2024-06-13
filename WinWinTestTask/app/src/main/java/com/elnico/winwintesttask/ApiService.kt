package com.elnico.winwintesttask

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("click.php")
    suspend fun performClick(@Query("key") key: String): ResponseBody
}