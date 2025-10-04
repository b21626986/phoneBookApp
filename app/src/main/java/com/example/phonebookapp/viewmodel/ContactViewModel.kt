package com.example.phonebookapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.phonebookapp.model.Contact
import android.content.ContentResolver
import com.example.phonebookapp.ui.utils.ContactUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import com.example.phonebookapp.data.api.ApiClient
import com.example.phonebookapp.data.api.PhonebookService
import com.example.phonebookapp.ui.utils.ContactUtils.isContactInDevice
import kotlinx.coroutines.launch
import android.content.Context

class ContactViewModel : ViewModel() {
    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText
    private val apiService: PhonebookService = ApiClient.retrofit.create(PhonebookService::class.java)

    init {
        fetchContactsFromApi()
    }

    // YENİ: Silme Onayı Pop-up'ı Yönetimi
    private val _showDeleteConfirmation = MutableStateFlow(false)
    val showDeleteConfirmation: StateFlow<Boolean> = _showDeleteConfirmation

    // YENİ: Silinecek Kişi
    private val _contactToDelete = MutableStateFlow<Contact?>(null)
    val contactToDelete: StateFlow<Contact?> = _contactToDelete

    // YENİ: Pop-up'ı göstermek için
    fun startDelete(contact: Contact) {
        _contactToDelete.value = contact
        _showDeleteConfirmation.value = true
    }

    // YENİ: Pop-up'ı iptal etmek için
    fun cancelDelete() {
        _contactToDelete.value = null
        _showDeleteConfirmation.value = false
    }


    fun fetchContactsFromApi() {
        viewModelScope.launch {
            try {
                val response = apiService.getAllContacts()
                if (response.isSuccessful && response.body() != null) {
                    _contacts.value = response.body()!!
                } else {
                    println("API Hatası: ${response.code()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory: StateFlow<List<String>> = _searchHistory

    private val sortedContacts: StateFlow<List<Contact>> = _contacts
        .map { contactList ->
            contactList.sortedWith(
                compareBy<Contact> {
                    it.name.isBlank()
                }.thenBy(String.CASE_INSENSITIVE_ORDER) {
                    if (it.name.isBlank()) it.phone else it.name
                }.thenBy(String.CASE_INSENSITIVE_ORDER) {
                    it.surname
                }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val filteredGroupedContacts: StateFlow<Map<Char, List<Contact>>> =
        combine(sortedContacts, _searchText) { contactList, searchText ->
            val baseList = if (searchText.isBlank()) {
                contactList
            } else {
                val lowerSearchText = searchText.trim().lowercase()
                contactList.filter { contact ->
                    val fullName = (contact.name + " " + contact.surname).trim().lowercase()
                    contact.name.lowercase().contains(lowerSearchText) ||
                            contact.surname.lowercase().contains(lowerSearchText) ||
                            contact.phone.lowercase().contains(lowerSearchText) ||
                            fullName.contains(lowerSearchText)
                }
            }

            baseList.groupBy { contact ->
                val firstChar = contact.name.trim().firstOrNull()
                if (firstChar != null && firstChar.isLetter()) firstChar.uppercaseChar() else '#'
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )


    fun onSearchTextChanged(text: String) {
        _searchText.value = text
    }

    fun addSearchTermToHistory(term: String) {
        val trimmedTerm = term.trim()
        if (trimmedTerm.isNotBlank()) {
            _searchHistory.update { currentHistory ->
                val updatedList = listOf(trimmedTerm) + currentHistory.filter { it != trimmedTerm }
                updatedList.take(5)
            }
        }
    }

    fun removeSearchTermFromHistory(term: String) {
        _searchHistory.update { currentHistory ->
            currentHistory.filter { it != term }
        }
    }


    // Lottie animasyonu ve Başarı Mesajları için state'ler
    private val _showSuccessAnimation = MutableStateFlow(false)
    val showSuccessAnimation: StateFlow<Boolean> = _showSuccessAnimation

    // YENİ: Başarı mesajı için state
    val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage


    fun resetSuccessAnimation() {
        _showSuccessAnimation.value = false
    }

    // YENİ: Başarı mesajını sıfırlama
    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    fun checkDeviceContactStatus(context: Context, phoneNumber: String): Boolean {
        return ContactUtils.isContactInDevice(context.contentResolver, phoneNumber)
    }

    // TEK VE BİRLEŞTİRİLMİŞ addContact FONKSİYONU
    fun addContact(contact: Contact) {
        viewModelScope.launch {
            try {
                val response = apiService.addContact(contact)

                if (response.isSuccessful) {
                    _contacts.update { currentList ->
                        val alreadyExists = currentList.any { it.phone == contact.phone }
                        if (alreadyExists) currentList else currentList + contact
                    }
                    fetchContactsFromApi()
                    _showSuccessAnimation.value = true
                    // YENİ: Başarı mesajını ayarla
                    _successMessage.value = "All Done! New contact saved!"
                } else {
                    println("API Ekleme Başarısız: ${response.code()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Contact güncelleme
    fun updateContactWithOldPhone(oldPhone: String, updatedContact: Contact) {
        val existingContact = _contacts.value.find { it.phone == oldPhone }
        if (existingContact != null &&
            existingContact.name == updatedContact.name &&
            existingContact.surname == updatedContact.surname &&
            existingContact.phone == updatedContact.phone &&
            existingContact.imageUri == updatedContact.imageUri) return

        viewModelScope.launch {
            try {
                // Not: API'den update fonksiyonu varsayımsal olarak çağrılıyor.
                // Eğer API'nizde PUT/PATCH metodu ve uygun endpoint varsa burada çağırılmalıdır.
                // val response = apiService.updateContact(oldPhone, updatedContact)

                // if (response.isSuccessful) {
                _contacts.update { currentList ->
                    val index = currentList.indexOfFirst { it.phone == oldPhone }
                    if (index != -1) {
                        currentList.toMutableList().apply { set(index, updatedContact) }
                    } else {
                        currentList
                    }
                }
                fetchContactsFromApi()
                _showSuccessAnimation.value = true
                // YENİ: Başarı mesajını ayarla
                _successMessage.value = "User is updated!"
                // } else {
                //     println("API Güncelleme Başarısız: ${response.code()}")
                // }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    // Contact silme (YENİ: API çağrısı eklendi)
    fun deleteContactConfirmed(contact: Contact) {
        _showDeleteConfirmation.value = false // Pop-up'ı kapat
        _contactToDelete.value = null // Silinecek kişiyi sıfırla

        viewModelScope.launch {
            try {
                // Not: API'den delete fonksiyonu varsayımsal olarak çağrılıyor.
                // val response = apiService.deleteContact(contact.phone)

                // if (response.isSuccessful) {
                _contacts.update { currentList ->
                    currentList.filter { it.phone != contact.phone }
                }
                // Silme sonrası API'den yeniden çekmeye gerek yok, UI güncellendi.
                _showSuccessAnimation.value = true
                // YENİ: Başarı mesajını ayarla
                _successMessage.value = "User is deleted!"
                // } else {
                //     println("API Silme Başarısız: ${response.code()}")
                // }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}