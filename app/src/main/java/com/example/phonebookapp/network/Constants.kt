package com.example.phonebookapp.network

object Constants {
    // Kural 1'e uyarak Base URL'yi kullanıyoruz.
    const val BASE_URL = "http://146.59.52.68:11235/api/"

    // Size verilen API Anahtarı. Bu, Header'a eklenecektir.
    // Kullanacağın Api Anahtarı: 2ff037e0-c6de-4a73-be31-a3266470bdb9
    const val API_KEY = "2ff037e0-c6de-4a73-be31-a3266470bdb9"

    // API Key için Header adı. (Örnek Curl'deki gibi)
    const val API_KEY_HEADER = "ApiKey"
}