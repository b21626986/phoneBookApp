package com.example.phonebookapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.*
import com.example.phonebookapp.R
import com.example.phonebookapp.viewmodel.ContactViewModel
import com.example.phonebookapp.model.Contact

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    phone: String,
    viewModel: ContactViewModel,
    navController: NavHostController,
    mode: String = "edit" // Yeni parametre eklendi (varsayılan: edit)
) {
    val contacts by viewModel.contacts.collectAsState()
    val showSuccessAnimation by viewModel.showSuccessAnimation.collectAsState()

    // Düzenleme (edit) modunda olup olmadığını kontrol eden bir boolean
    val isEditMode = mode == "edit"

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.done))
    val progress by animateLottieCompositionAsState(
        composition,
        isPlaying = showSuccessAnimation,
        iterations = 1,
        restartOnPlay = true
    )

    // phone değişirse (örneğin güncellediğimizde) bu kontak nesnesinin güncel kalmasını sağlar.
    val initialContact = remember(phone) { contacts.find { it.phone == phone } }

    // Eğer initialContact null ise geri dön
    if (initialContact == null) {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    // Lottie animasyonu bittiğinde geri dön
    LaunchedEffect(progress) {
        if (progress >= 1f && showSuccessAnimation) {
            viewModel.resetSuccessAnimation()
            navController.popBackStack()
        }
    }

    // Kişi bilgilerini mutable state ile tutuyoruz.
    var name by remember { mutableStateOf(initialContact.name) }
    var surname by remember { mutableStateOf(initialContact.surname) }
    var phoneNumber by remember { mutableStateOf(initialContact.phone) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Contact" else "Contact Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri"
                        )
                    }
                }
            )
        },
        content = { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        readOnly = !isEditMode, // Edit modunda değilse salt okunur
                        modifier = Modifier.fillMaxWidth()
                    )
                    // Surname
                    OutlinedTextField(
                        value = surname,
                        onValueChange = { surname = it },
                        label = { Text("Surname") },
                        readOnly = !isEditMode, // Edit modunda değilse salt okunur
                        modifier = Modifier.fillMaxWidth()
                    )
                    // Phone
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Phone") },
                        readOnly = !isEditMode, // Edit modunda değilse salt okunur
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(8.dp))

                    // SAVE BUTTON: Sadece edit modunda gösterilir
                    if (isEditMode) {
                        Button(
                            onClick = {
                                val updatedContact = Contact(name, surname, phoneNumber)
                                viewModel.updateContactWithOldPhone(
                                    oldPhone = initialContact.phone,
                                    updatedContact = updatedContact
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = name.isNotBlank() && surname.isNotBlank() && phoneNumber.isNotBlank() &&
                                    !(name == initialContact.name && surname == initialContact.surname && phoneNumber == initialContact.phone)
                        ) {
                            Text("Save Changes")
                        }

                        Spacer(Modifier.height(8.dp))

                        // DELETE BUTTON: Sadece edit modunda gösterilir
                        Button(
                            onClick = {
                                viewModel.deleteContact(initialContact)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Delete", color = MaterialTheme.colorScheme.onError)
                        }
                    }
                }

                // Lottie Animasyonu
                if (showSuccessAnimation && composition != null) {
                    LottieAnimation(
                        composition,
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White.copy(alpha = 0.8f))
                    )
                }
            }
        }
    )
}