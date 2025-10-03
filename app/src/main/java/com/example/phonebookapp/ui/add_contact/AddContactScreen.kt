package com.example.phonebookapp.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.phonebookapp.ui.utils.getDominantColor
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactScreen(
    viewModel: ContactViewModel,
    navController: NavHostController
) {
    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<String?>(null) } // Yeni state
    //val context = LocalContext.current         //gecici denemelerde kullanmak üzere

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        // Seçilen URI'yi imageUri state'ine kaydet
        imageUri = uri?.toString()
    }

    // Baskın renk hesaplama (Gölge için)
    val shadowColor by getDominantColor(imageUri)
    val showSuccessAnimation by viewModel.showSuccessAnimation.collectAsState()


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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Contact") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri"
                        )
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
                horizontalAlignment = Alignment.CenterHorizontally, // Ortalamak için
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // FOTOĞRAF SEÇİM ALANI
                ContactImage(
                    imageUri = imageUri,
                    shadowColor = shadowColor, // Gölge rengini uygula
                    modifier = Modifier
                        .size(100.dp)
                        .clickable {
                        // Galeriye erişimi başlat
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

                Button(
                    onClick = {
                        // imageUri'yi Contact nesnesine ekle
                        val contact = Contact(name.trim(), surname.trim(), phone.trim(), imageUri)
                        viewModel.addContact(contact)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = phone.isNotBlank()
                ) {
                    Text("Save")
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
}

// Mükerrer ContactImage kaldırıldı; utils/ContactImage kullanılacak