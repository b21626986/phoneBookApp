package com.example.phonebookapp.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()

        // Kural: API Anahtarını Header'a ekle
        val request = original.newBuilder()
            .header(Constants.API_KEY_HEADER, Constants.API_KEY)
            .build()

        chain.proceed(request)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Hata ayıklama için istek/cevap içeriği gösterilir
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor) // API Anahtarını ekleyen Interceptor
        .addInterceptor(loggingInterceptor) // Logging Interceptor
        .connectTimeout(30, TimeUnit.SECONDS) // Bağlantı zaman aşımı
        .readTimeout(30, TimeUnit.SECONDS)    // Okuma zaman aşımı
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}