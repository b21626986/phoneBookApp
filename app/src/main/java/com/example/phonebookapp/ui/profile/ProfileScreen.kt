package com.example.phonebookapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info // YENİ İçe aktarma
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
import com.example.phonebookapp.ui.utils.ContactUtils.saveContactToDevice
import androidx.compose.ui.platform.LocalContext
import com.example.phonebookapp.viewmodel.ContactViewModel
import com.example.phonebookapp.model.Contact
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.phonebookapp.ui.utils.ContactImage
import com.example.phonebookapp.ui.utils.SuccessMessagePopup
import com.example.phonebookapp.ui.utils.ContactUtils.isContactInDevice
import android.net.Uri
import androidx.compose.ui.zIndex

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
    val contactToDelete by viewModel.contactToDelete.collectAsState()
    val showDeleteConfirmation by viewModel.showDeleteConfirmation.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val context = LocalContext.current

    val isEditMode = mode == "edit"
    var showMenu by remember { mutableStateOf(false) }

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.done))
    val progress by animateLottieCompositionAsState(
        composition,
        isPlaying = showSuccessAnimation,
        iterations = 1,
        restartOnPlay = true
    )

    val initialContact = remember(phone) { contacts.find { it.phone == phone } }

    if (initialContact == null) {
        // Kişi silinmişse veya bulunamazsa geri dön
        LaunchedEffect(Unit) { navController.popBackStack() }
        return
    }

    LaunchedEffect(progress) {
        if (progress >= 1f && showSuccessAnimation) {
            viewModel.resetSuccessAnimation()
            // Başarılı işlem sonrası Profile ekranından Contacts ekranına dön
            // Silme işlemi viewmodel'da halledildiğinde bu kısım tetiklenmez, sadece update.
            // Update sonrası ContactsScreen'e dönüyoruz.
            navController.popBackStack()
        }
    }

    // Success mesajı görününce sıfırla (3 saniye sonra)
    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearSuccessMessage()
        }
    }


    var name by remember { mutableStateOf(initialContact.name) }
    var surname by remember { mutableStateOf(initialContact.surname) }
    var phoneNumber by remember { mutableStateOf(initialContact.phone) }
    var imageUri by remember { mutableStateOf(initialContact.imageUri) }
    val shadowColor by getDominantColor(imageUri)

    // Cihaz rehberi durumunu kontrol et
    var isContactSavedToDevice by remember { mutableStateOf(false) }
    LaunchedEffect(phoneNumber) {
        isContactSavedToDevice = isContactInDevice(context.contentResolver, phoneNumber)
    }

    // Fotoğraf Seçimi Launcher'ı
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri.toString()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Contact" else "Contact Profile") },
                navigationIcon = {
                    if (isEditMode) {
                        // CANCEL butonu
                        TextButton(onClick = { navController.popBackStack() }) {
                            Text("Cancel", color = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Geri"
                            )
                        }
                    }
                },
                actions = {
                    if (isEditMode) {
                        // DONE butonu
                        val hasChanges = (
                                name != initialContact.name ||
                                        surname != initialContact.surname ||
                                        phoneNumber != initialContact.phone ||
                                        imageUri != initialContact.imageUri
                                )

                        TextButton(
                            onClick = {
                                val updatedContact = Contact(name, surname, phoneNumber, imageUri)
                                viewModel.updateContactWithOldPhone(
                                    oldPhone = initialContact.phone,
                                    updatedContact = updatedContact
                                )
                            },
                            enabled = hasChanges && phoneNumber.isNotBlank()
                        ) {
                            Text("Done", color = MaterialTheme.colorScheme.primary)
                        }

                    } else {
                        // View mode'da üç nokta menüsü göster
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Menu"
                                )
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Edit") },
                                    onClick = {
                                        showMenu = false
                                        navController.navigate("profile/${phone}?mode=edit")
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit"
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete") },
                                    onClick = {
                                        showMenu = false
                                        // Silme pop-up'ını tetikle
                                        viewModel.startDelete(initialContact)
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete"
                                        )
                                    }
                                )
                            }
                        }
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
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // FOTOĞRAF GÖRÜNTÜLEME VE DÜZENLEME ALANI
                    ContactImage(
                        imageUri = imageUri,
                        shadowColor = shadowColor,
                        modifier = Modifier
                            .size(120.dp)
                            .then(if (isEditMode) Modifier.clickable {
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

                    // BURASI KALDIRILDI: Edit modundaki alt Delete butonu artık yok.
                    if (isEditMode) {
                        Spacer(Modifier.height(48.dp)) // Boşluk tutucu
                    }
                    else {
                        // Rehbere Kaydet Butonu (View modunda gösterilir)
                        Button(
                            onClick = {
                                saveContactToDevice(context, initialContact)
                                isContactSavedToDevice = true // Anında UI güncellemesi
                                // Başarı mesajını ayarla
                                viewModel._successMessage.value = "User is added to your phone!"
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isContactSavedToDevice, // Rehbere kayıtlıysa disabled
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                        ) {
                            Text("Rehbere Kaydet", color = MaterialTheme.colorScheme.onTertiary)
                        }

                        // Rehbere kayıtlıysa bilgi mesajı
                        if (isContactSavedToDevice) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Info",
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "This contact is already saved your phone.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }

                // Silme Onayı Pop-up'ı (AlertDialog)
                if (showDeleteConfirmation && contactToDelete != null) {
                    AlertDialog(
                        onDismissRequest = { viewModel.cancelDelete() },
                        title = { Text("Delete Contact") },
                        text = { Text("Are you sure you want to delete this contact?") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    viewModel.deleteContactConfirmed(contactToDelete!!)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Yes", color = MaterialTheme.colorScheme.onError)
                            }
                        },
                        dismissButton = {
                            OutlinedButton(onClick = { viewModel.cancelDelete() }) {
                                Text("No")
                            }
                        }
                    )
                }

                // Lottie Animasyonu
                if (showSuccessAnimation && composition != null) {
                    LottieAnimation(
                        composition,
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White.copy(alpha = 0.8f))
                            .zIndex(1f)
                    )
                }

                // Başarı Mesajı
                successMessage?.let { message ->
                    SuccessMessagePopup(
                        message = message,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
    )
}