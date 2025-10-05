package com.example.phonebookapp.network

import com.example.phonebookapp.model.Contact
import retrofit2.Response
import retrofit2.http.*

interface ContactApiService {

    @GET("User/GetAll")
    suspend fun getAllContacts(): Response<List<Contact>>

    @POST("User")
    suspend fun addContact(@Body contact: Contact): Response<Unit>

    @PUT("UpdateContact")
    suspend fun updateContact(@Body contact: Contact): Response<Unit>

    @DELETE("DeleteContact")
    suspend fun deleteContact(@Query("id") contactId: String): Response<Unit>

}