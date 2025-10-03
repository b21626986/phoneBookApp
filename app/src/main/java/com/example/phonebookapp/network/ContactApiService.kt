package com.example.phonebookapp.network

import com.example.phonebookapp.model.Contact
import retrofit2.Response
import retrofit2.http.*

interface ContactApiService {

    // Örnek: Tüm kişileri çekme (Swagger'a göre güncelleyin)
    // Varsayım: Endpoint GetAll'dur.
    @GET("User/GetAll")
    suspend fun getAllContacts(): Response<List<Contact>>

    // Örnek: Yeni kişi ekleme
    // Swagger'daki POST /api/AddContact endpoint'ini kullanır.
    @POST("User")
    suspend fun addContact(@Body contact: Contact): Response<Unit>

    // Örnek: Kişi güncelleme
    @PUT("UpdateContact")
    suspend fun updateContact(@Body contact: Contact): Response<Unit>

    // Örnek: Kişi silme
    // Swagger'a göre silme işlemi ID ile yapılıyorsa modelde ID tutulmalıdır.
    // Eğer telefon numarası ile silme destekleniyorsa bu kullanılır.
    @DELETE("DeleteContact")
    suspend fun deleteContact(@Query("id") contactId: String): Response<Unit>

    // Not: Tüm bu endpoint'lerin adları ve parametreleri,
    // http://146.59.52.68:11235/swagger adresindeki dökümantasyona göre kontrol edilmeli ve uyarlanmalıdır.
}