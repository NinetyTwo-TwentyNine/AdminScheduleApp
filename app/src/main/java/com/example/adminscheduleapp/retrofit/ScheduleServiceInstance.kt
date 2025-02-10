package com.example.adminscheduleapp.retrofit

import com.example.adminscheduleapp.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import java.util.concurrent.TimeUnit.MILLISECONDS

@Suppress("DEPRECATION")
class ScheduleServiceInstance {
    companion object {
        private val serverLink = BuildConfig.API_LINK

        private val retrofit: Retrofit =
            Retrofit.Builder()
                .baseUrl(serverLink)
                .addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .client(
                    OkHttpClient().newBuilder().addInterceptor(
                        HttpLoggingInterceptor().setLevel(
                            HttpLoggingInterceptor.Level.BASIC
                        )
                    )
                        .connectTimeout(16000L, MILLISECONDS)
                        .readTimeout(16000L, MILLISECONDS)
                        .writeTimeout(16000L, MILLISECONDS).build()
                )
                .build()

        fun <T> createService(serviceClass: Class<T>): T {
            return retrofit.create(serviceClass)
        }
    }
}