package com.example.phonebookapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.phonebookapp.model.Contact
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class ContactViewModel : ViewModel() {
    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts

    // Lottie animasyonu için genel success state
    private val _showSuccessAnimation = MutableStateFlow(false)
    val showSuccessAnimation: StateFlow<Boolean> = _showSuccessAnimation

    // Animation type - hangi işlem için animasyon gösterileceğini belirler
    private val _animationType = MutableStateFlow("")
    val animationType: StateFlow<String> = _animationType

    // Animation'ı sıfırlama
    fun resetSuccessAnimation() {
        _showSuccessAnimation.value = false
        _animationType.value = ""
    }

    // Yeni contact ekleme
    fun addContact(contact: Contact) {
        _contacts.value = _contacts.value + contact
        _showSuccessAnimation.value = true
        _animationType.value = "add"
    }

    // Contact güncelleme
    fun updateContactWithOldPhone(oldPhone: String, updatedContact: Contact) {
        // Eğer kişi hiç değişmediyse güncelleme yapma
        val existingContact = _contacts.value.find { it.phone == oldPhone }
        if (existingContact == updatedContact) return

        _contacts.update { currentList ->
            val index = currentList.indexOfFirst { it.phone == oldPhone }
            if (index != -1) {
                currentList.toMutableList().apply {
                    set(index, updatedContact)
                }
            } else {
                currentList
            }
        }
        _showSuccessAnimation.value = true
        _animationType.value = "update"
    }

    // Contact silme
    fun deleteContact(contact: Contact) {
        _contacts.value = _contacts.value.filter { it.phone != contact.phone }
        _showSuccessAnimation.value = true
        _animationType.value = "delete"
    }
}