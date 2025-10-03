package com.example.phonebookapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.*
import com.example.phonebookapp.R
import com.example.phonebookapp.ui.utils.getDominantColor
import com.example.phonebookapp.viewmodel.ContactViewModel
import com.example.phonebookapp.model.Contact
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.phonebookapp.ui.utils.ContactImage
import android.net.Uri
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    phone: String,
    viewModel: ContactViewModel,
    navController: NavHostController,
    mode: String = "edit"
) {
    val contacts by viewModel.contacts.collectAsState()
    val showSuccessAnimation by viewModel.showSuccessAnimation.collectAsState()
    //val context = LocalContext.current         //gecici denemelerde kullanmak üzere

    val isEditMode = mode == "edit"

    // ... (Lottie kodları aynı kaldı) ...
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.done))
    val progress by animateLottieCompositionAsState(
        composition,
        isPlaying = showSuccessAnimation,
        iterations = 1,
        restartOnPlay = true
    )

    val initialContact = remember(phone) { contacts.find { it.phone == phone } }



    if (initialContact == null) {
        LaunchedEffect(Unit) { navController.popBackStack() }
        return
    }

    LaunchedEffect(progress) {
        if (progress >= 1f && showSuccessAnimation) {
            viewModel.resetSuccessAnimation()
            navController.popBackStack()
        }
    }

    var name by remember { mutableStateOf(initialContact.name) }
    var surname by remember { mutableStateOf(initialContact.surname) }
    var phoneNumber by remember { mutableStateOf(initialContact.phone) }
    var imageUri by remember { mutableStateOf(initialContact.imageUri) } // Yeni state
    // YENİ: Baskın rengi hesaplama (Simülasyon)
    val shadowColor by getDominantColor(imageUri) // Baskın rengi izle


    // Fotoğraf Seçimi Launcher'ı
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        // Seçim iptal edilirse mevcut fotoğrafı koru
        if (uri != null) {
            imageUri = uri.toString()
        }
    }

    // ... (Scaffold ve TopAppBar kodları aynı kaldı) ...
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
                    horizontalAlignment = Alignment.CenterHorizontally, // Ortalamak için
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // FOTOĞRAF GÖRÜNTÜLEME VE DÜZENLEME ALANI
                    ContactImage(
                        imageUri = imageUri,
                        // shadowColor'ı ContactImage'e iletiyoruz
                        shadowColor = shadowColor,
                        modifier = Modifier
                            .size(120.dp)
                            .then(if (isEditMode) Modifier.clickable {
                                // Edit modundaysa galeriye erişimi başlat
                                imagePickerLauncher.launch("image/*")

                         } else Modifier)
                    )
                    Spacer(Modifier.height(16.dp))

                    if (isEditMode && imageUri != null) {
                        TextButton(onClick = { imageUri = null }) {
                            Text("Remove Photo")
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    // ... (OutlinedTextField'ler aynı kaldı) ...
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        readOnly = !isEditMode,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = surname,
                        onValueChange = { surname = it },
                        label = { Text("Surname") },
                        readOnly = !isEditMode,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Phone") },
                        readOnly = !isEditMode,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(8.dp))

                    // Save Button: Sadece edit modunda gösterilir
                    if (isEditMode) {
                        Button(
                            onClick = {
                                // imageUri'yi Contact nesnesine ekle
                                val updatedContact = Contact(name, surname, phoneNumber, imageUri)
                                viewModel.updateContactWithOldPhone(
                                    oldPhone = initialContact.phone,
                                    updatedContact = updatedContact
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            // Sadece bir değişiklik olduğunda aktif olsun
                            enabled = phoneNumber.isNotBlank() && (
                                name != initialContact.name ||
                                surname != initialContact.surname ||
                                phoneNumber != initialContact.phone ||
                                imageUri != initialContact.imageUri
                            )
                        ) {
                            Text("Save Changes")
                        }

                        Spacer(Modifier.height(8.dp))

                        // Delete Button: Sadece edit modunda gösterilir
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
