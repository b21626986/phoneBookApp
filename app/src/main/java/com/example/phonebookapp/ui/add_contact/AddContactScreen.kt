package com.example.phonebookapp.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.*
import com.example.phonebookapp.R
import com.example.phonebookapp.model.Contact
import com.example.phonebookapp.viewmodel.ContactViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.example.phonebookapp.ui.utils.ContactImage
import com.example.phonebookapp.ui.utils.SuccessMessagePopup
import com.example.phonebookapp.ui.utils.getDominantColor
import androidx.compose.ui.zIndex

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactScreen(
    viewModel: ContactViewModel,
    navController: NavHostController
) {
    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<String?>(null) }
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri?.toString()
    }

    val shadowColor by getDominantColor(imageUri)
    val showSuccessAnimation by viewModel.showSuccessAnimation.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.done))
    val progress by animateLottieCompositionAsState(
        composition,
        isPlaying = showSuccessAnimation,
        iterations = 1,
        restartOnPlay = true
    )

    // Lottie animasyonu bittiğinde geri dön
    LaunchedEffect(progress) {
        if (progress >= 1f && showSuccessAnimation) {
            viewModel.resetSuccessAnimation()
            navController.popBackStack()
        }
    }

    // Success mesajı görününce sıfırla (3 saniye sonra)
    LaunchedEffect(successMessage) {
        if (successMessage != null && !showSuccessAnimation) { // Animasyon bitince, sadece mesajı sıfırla
            kotlinx.coroutines.delay(3000)
            viewModel.clearSuccessMessage()
        }
    }

    // Kaydetme işlemi
    val saveContact = {
        keyboardController?.hide()
        val contact = Contact(name.trim(), surname.trim(), phone.trim(), imageUri)
        viewModel.addContact(contact)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Contact") },
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("Cancel", color = MaterialTheme.colorScheme.primary)
                    }
                },
                actions = {
                    TextButton(
                        onClick = saveContact,
                        enabled = phone.isNotBlank()
                    ) {
                        Text("Done", color = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // ... (Giriş Alanları) ...
                ContactImage(
                    imageUri = imageUri,
                    shadowColor = shadowColor,
                    modifier = Modifier
                        .size(100.dp)
                        .clickable {
                            imagePickerLauncher.launch("image/*")
                        }
                )
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = surname,
                    onValueChange = { surname = it },
                    label = { Text("Surname (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone (required)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))
                Spacer(Modifier.height(48.dp))
            }

            // YENİ: Lottie Animasyonu ve Mesajı Birleştirme
            if (showSuccessAnimation && composition != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = 0.8f))
                        .zIndex(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    LottieAnimation(
                        composition,
                        progress = { progress },
                        modifier = Modifier.size(200.dp)
                    )
                    // İSTEK 4: Lottie'nin hemen altında mesajı göster.
                    if (successMessage != null) {
                        // successMessage'ı SuccessMessagePopup ile değil, basit Text ile stilize ederek göster.
                        // Daha sonra SuccessMessagePopup da ekranda alt kısımda gösterilecektir.
                        Text(
                            text = successMessage!!,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            // Başarı Mesajı (Yalnızca animasyon gösterilmiyorsa alt kısımda göster)
            // Lottie gösterilirken mesaj ortada gösterildiği için burada koşul ekledik.
            if (!showSuccessAnimation) {
                successMessage?.let { message ->
                    SuccessMessagePopup(
                        message = message,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }
}