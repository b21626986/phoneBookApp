package com.example.phonebookapp.data.api

import com.example.phonebookapp.model.Contact
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface PhonebookService {
    @GET("User/GetAll")
    suspend fun getAllContacts(): Response<List<Contact>>

    @POST("User")
    suspend fun addContact(@Body contact: Contact): Response<Unit>
}


