package com.example.phonebookapp.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import com.example.phonebookapp.R
import com.example.phonebookapp.model.Contact
import com.example.phonebookapp.ui.theme.FigmaGray_300
import com.example.phonebookapp.ui.theme.FigmaGray_950
import com.example.phonebookapp.ui.theme.FigmaPrimaryBlue
import com.example.phonebookapp.ui.theme.FigmaSuccessGreen
import com.example.phonebookapp.ui.theme.ThemedScreen
import com.example.phonebookapp.ui.utils.ContactImage
import com.example.phonebookapp.ui.utils.ContactUtils.isContactInDevice
import com.example.phonebookapp.ui.utils.ContactUtils.saveContactToDevice
import com.example.phonebookapp.ui.utils.FullWidthBottomSheetDialog // <-- Bottom Sheet Bileşeni
import com.example.phonebookapp.ui.utils.SuccessMessagePopup
import com.example.phonebookapp.ui.utils.getDominantColor
import com.example.phonebookapp.viewmodel.ContactViewModel
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    phone: String,
    viewModel: ContactViewModel,
    navController: NavHostController,
    mode: String = "view"
) {
    // TÜM MEVCUT STATE VE INITIALIZATION KODLARI BURADA AYNI KALIR
    val contacts by viewModel.contacts.collectAsState()
    val contactToDelete by viewModel.contactToDelete.collectAsState()
    val showDeleteConfirmation by viewModel.showDeleteConfirmation.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState() // Düzeltilmiş state kullanımı
    val context = LocalContext.current
    var isEditMode by remember { mutableStateOf(mode == "edit") }
    var showMenu by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val initialContact = remember(phone) { contacts.find { it.phone == phone } }

    if (initialContact == null) {
        // Eğer kontak bulunamazsa, direkt geri dönülür (dialog kapanır)
        LaunchedEffect(Unit) { navController.popBackStack() }
        return
    }

    // ... (LaunchedEffect'ler ve diğer değişken tanımları aynı kalır) ...
    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearSuccessMessage()
        }
    }

    LaunchedEffect(mode) {
        isEditMode = mode == "edit"
    }

    var name by remember { mutableStateOf(initialContact.name) }
    var surname by remember { mutableStateOf(initialContact.surname) }
    var phoneNumber by remember { mutableStateOf(initialContact.phone) }
    var imageUri by remember { mutableStateOf(initialContact.imageUri) }
    val shadowColor by getDominantColor(imageUri)

    var isContactSavedToDevice by remember { mutableStateOf(false) }
    LaunchedEffect(phoneNumber) {
        isContactSavedToDevice = isContactInDevice(context.contentResolver, phoneNumber)
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri.toString()
        }
    }
    FullWidthBottomSheetDialog(
        onDismissRequest = { navController.popBackStack() } // Dışarı tıklayınca kapatma
    ) {
        // ThemedScreen, artık Dialog'un içindeki Box'ın içeriğidir.
        ThemedScreen(primaryColor = FigmaSuccessGreen) {

            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        // Artık üstten 42.dp boşluğa gerek yok, çünkü Dialog kendisi
                        // üstten gölgeli boşluğu sağlıyor.
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                        )
                        .padding(top = 0.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // Bottom Sheet Başlık/Navigasyon Çubuğu (ESKİ KODUNUZUN AYNISI)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // SOL BUTON/YER TUTUCU
                        if (isEditMode) {
                            TextButton(onClick = { navController.popBackStack() }) {
                                Text("Cancel", color = FigmaPrimaryBlue)
                            }
                        } else {
                            Spacer(Modifier.width(60.dp))
                        }

                        // BAŞLIK
                        Text(
                            text = if (isEditMode) "Edit Contact" else (initialContact.name + " " + initialContact.surname).trim(),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // SAĞ BUTON/MENÜ
                        if (isEditMode) {
                            val hasChanges = (name != initialContact.name || surname != initialContact.surname || phoneNumber != initialContact.phone || imageUri != initialContact.imageUri)
                            TextButton(
                                onClick = {
                                    val updatedContact = Contact(name, surname, phoneNumber, imageUri)
                                    viewModel.updateContactWithOldPhone(oldPhone = initialContact.phone, updatedContact = updatedContact)
                                    navController.navigate("contacts") { popUpTo("contacts") { inclusive = true } }
                                },
                                enabled = hasChanges && phoneNumber.isNotBlank()
                            ) {
                                Text("Done", color = FigmaPrimaryBlue)
                            }
                        } else {
                            Box {
                                IconButton(onClick = { showMenu = true }) {
                                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menu")
                                }
                                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                                    DropdownMenuItem(text = { Text("Edit") }, onClick = { showMenu = false; isEditMode = true }, trailingIcon = { Icon(painter = painterResource(id = R.drawable.ic_edit), contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurface) })
                                    DropdownMenuItem(text = { Text("Delete", color = MaterialTheme.colorScheme.error) }, onClick = { showMenu = false; viewModel.startDelete(initialContact) }, trailingIcon = { Icon(painter = painterResource(id = R.drawable.ic_delete), contentDescription = "Delete", tint = MaterialTheme.colorScheme.error) })
                                }
                            }
                        }
                    }

                    // Form İçeriği (Kaydırılabilir - ESKİ KODUNUZUN AYNISI)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(horizontal = 32.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // ... Geri kalan tüm form elemanları ve butonlar aynı kalır ...
                        ContactImage(
                            imageUri = imageUri,
                            shadowColor = shadowColor,
                            modifier = Modifier
                                .size(120.dp)
                                .then(if (isEditMode) Modifier.clickable { imagePickerLauncher.launch("image/*") } else Modifier)
                        )
                        Spacer(Modifier.height(16.dp))

                        TextButton(onClick = { if (isEditMode) imagePickerLauncher.launch("image/*") }, colors = ButtonDefaults.textButtonColors(contentColor = FigmaPrimaryBlue)) { Text(if (imageUri == null) "Add Photo" else "Change Photo") }
                        if (isEditMode && imageUri != null) {
                            TextButton(onClick = { imageUri = null }) { Text("Remove Photo", color = MaterialTheme.colorScheme.error) }
                            Spacer(Modifier.height(8.dp))
                        }

                        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, readOnly = !isEditMode, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = surname, onValueChange = { surname = it }, label = { Text("Surname") }, readOnly = !isEditMode, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = phoneNumber, onValueChange = { phoneNumber = it }, label = { Text("Phone") }, readOnly = !isEditMode, modifier = Modifier.fillMaxWidth())

                        Spacer(Modifier.height(8.dp))

                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            OutlinedButton(onClick = { saveContactToDevice(context, initialContact); isContactSavedToDevice = true; viewModel._successMessage.value = "User is added to your phone!" }, enabled = !isContactSavedToDevice, colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = FigmaGray_950), border = BorderStroke(1.dp, FigmaGray_950), modifier = Modifier.padding(vertical = 4.dp)) {
                                Icon(painter = painterResource(id = R.drawable.ic_save), contentDescription = "Save to phone", modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Save to My Phone Contact", style = MaterialTheme.typography.bodyLarge)
                            }
                        }

                        if (isContactSavedToDevice) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Info, contentDescription = "Info", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(text = "This contact is already saved your phone.", style = MaterialTheme.typography.bodySmall, color = FigmaGray_300)
                            }
                        }

                        Spacer(Modifier.height(48.dp))
                    }
                }

                // ÖZEL SİLME ONAY DİYALOĞU (ESKİ KODUNUZUN AYNISI)
                if (showDeleteConfirmation && contactToDelete != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f))
                            .zIndex(2f)
                            .clickable(onClick = { viewModel.cancelDelete() }),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 200.dp)
                                .background(Color.White, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                                .padding(24.dp)
                                .clickable(enabled = false, onClick = { /* İçeride tıklamayı engelle */ }),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "Delete Contact", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                            Spacer(Modifier.height(8.dp))
                            Text(text = "Are you sure you want to delete this contact?", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(Modifier.height(24.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                OutlinedButton(onClick = { viewModel.cancelDelete() }, modifier = Modifier.weight(1f).padding(end = 8.dp), border = BorderStroke(1.dp, FigmaGray_950), colors = ButtonDefaults.outlinedButtonColors(contentColor = FigmaGray_950)) { Text("No") }
                                Button(onClick = { viewModel.deleteContactConfirmed(contactToDelete!!); navController.navigate("contacts") { popUpTo("contacts") { inclusive = true } } }, modifier = Modifier.weight(1f).padding(start = 8.dp), colors = ButtonDefaults.buttonColors(containerColor = FigmaGray_950, contentColor = Color.White)) { Text("Yes") }
                            }
                        }
                    }
                }

                // Başarı Mesajı (Altta Sabit - ESKİ KODUNUZUN AYNISI)
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