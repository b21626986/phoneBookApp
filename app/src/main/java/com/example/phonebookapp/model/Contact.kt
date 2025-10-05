package com.example.phonebookapp.model

data class Contact(
    val id: Long = 0L,
    val name: String,
    val surname: String,
    val phone: String,
    val imageUri: String? = null
)