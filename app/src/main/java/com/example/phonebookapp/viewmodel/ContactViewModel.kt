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

    // Arama metni StateFlow
    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText
    private val apiService: PhonebookService = ApiClient.retrofit.create(PhonebookService::class.java)

    init {
        fetchContactsFromApi() // Uygulama başladığında API'den kişileri çek
    }

    fun fetchContactsFromApi() {
        viewModelScope.launch {
            try {
                val response = apiService.getAllContacts()
                if (response.isSuccessful && response.body() != null) {
                    _contacts.value = response.body()!!
                    // Başarılı olursa, filtrelemeyi ve gruplamayı otomatik tetikler
                } else {
                    // Hata durumu (401, 404, vb.)
                    println("API Hatası: ${response.code()}")
                }
            } catch (e: Exception) {
                // Ağ hatası, JSON hatası vb.
                e.printStackTrace()
            }
        }
    }

    // Arama Geçmişi StateFlow (Son 5 aramayı tutar)
    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory: StateFlow<List<String>> = _searchHistory

    private val sortedContacts: StateFlow<List<Contact>> = _contacts
        .map { contactList ->
            contactList.sortedWith(
                compareBy<Contact> {
                    // Boş/blank isimleri en sona koy
                    it.name.isBlank()
                }.thenBy(String.CASE_INSENSITIVE_ORDER) {
                    // İsim boşsa telefonu kullanarak sıralama yap, değilse isme göre
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

            // Gruplandırma: isim boşsa veya harfle başlamıyorsa '#' grubuna koy
            baseList.groupBy { contact ->
                val firstChar = contact.name.trim().firstOrNull()
                if (firstChar != null && firstChar.isLetter()) firstChar.uppercaseChar() else '#'
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )


    // Arama metnini güncelleyen fonksiyon
    fun onSearchTextChanged(text: String) {
        _searchText.value = text
    }

    // Arama geçmişine yeni bir terim ekler.
    fun addSearchTermToHistory(term: String) {
        val trimmedTerm = term.trim()
        if (trimmedTerm.isNotBlank()) {
            _searchHistory.update { currentHistory ->
                // Yeni terimi listenin başına ekle ve varsa tekrarını sil.
                val updatedList = listOf(trimmedTerm) + currentHistory.filter { it != trimmedTerm }
                // Sadece son 5 terimi tut.
                updatedList.take(5)
            }
        }
    }

    // Arama geçmişinden bir terimi siler.
    fun removeSearchTermFromHistory(term: String) {
        _searchHistory.update { currentHistory ->
            currentHistory.filter { it != term }
        }
    }


    // Lottie animasyonu için genel success state
// ... (Geri kalan kodlar aynı kaldı) ...
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


    // Yeni: Kişinin cihazda kayıtlı olup olmadığını kontrol eden Composable dışı fonksiyon
    fun checkDeviceContactStatus(context: Context, phoneNumber: String): Boolean {
        return ContactUtils.isContactInDevice(context.contentResolver, phoneNumber)
    }

    // TEK VE BİRLEŞTİRİLMİŞ addContact FONKSİYONU
    fun addContact(contact: Contact) {
        viewModelScope.launch {
            try {
                // 1. API'ye kaydetme isteği gönderilir.
                val response = apiService.addContact(contact)

                if (response.isSuccessful) {
                    // 2. Başarılı olursa, listedeki tutarsızlıkları önlemek için tüm kişileri API'den yeniden çeker.
                    // Not: Daha iyi performans için sadece yeni eklenen kişiyi listeye ekleyebilirsiniz,
                    // ancak API ile tutarlılığı garanti etmek için tekrar çekmek daha güvenlidir.

                    // 2a. Başarılı olursa, UI'nin anında güncellenmesi için listeye optimistik olarak ekle.
                    _contacts.update { currentList ->
                        val alreadyExists = currentList.any { it.phone == contact.phone }
                        if (alreadyExists) currentList else currentList + contact
                    }

                    // 2b. Ardından sunucu ile tam tutarlılık için arka planda yeniden çek.
                    fetchContactsFromApi()

                    // 3. UI Başarı animasyonunu tetikler.
                    _showSuccessAnimation.value = true
                    _animationType.value = "add" // Varsayılan animasyon tipi
                } else {
                    // 4. API hata kodu gelirse (Örnek: 400 Bad Request)
                    println("API Ekleme Başarısız: ${response.code()}. Cevap: ${response.errorBody()?.string()}")
                    // Kullanıcıya hata mesajı gösterme mantığı buraya eklenebilir.
                }
            } catch (e: Exception) {
                // 5. Ağ hatası veya Coroutine hatası
                e.printStackTrace()
                // Kullanıcıya ağ hatası mesajı gösterme mantığı buraya eklenebilir.
            }
        }
    }

    // Contact güncelleme
    fun updateContactWithOldPhone(oldPhone: String, updatedContact: Contact) {
        // Eğer kişi hiç değişmediyse güncelleme yapma
        val existingContact = _contacts.value.find { it.phone == oldPhone }
        if (existingContact != null &&
            existingContact.name == updatedContact.name &&
            existingContact.surname == updatedContact.surname &&
            existingContact.phone == updatedContact.phone &&
            existingContact.imageUri == updatedContact.imageUri) return

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

    // (Kaldırıldı) ContentResolver alan overload gereksizdi; Context üzerinden çağrıyoruz.
}