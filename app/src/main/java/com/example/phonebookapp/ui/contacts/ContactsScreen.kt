package com.example.phonebookapp.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.*
import com.example.phonebookapp.R
import com.example.phonebookapp.ui.SwipeRow
import com.example.phonebookapp.ui.utils.ContactImage
import com.example.phonebookapp.viewmodel.ContactViewModel
import kotlinx.coroutines.launch
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ContactsScreen(
    viewModel: ContactViewModel,
    navController: NavHostController
) {
    val groupedContacts by viewModel.filteredGroupedContacts.collectAsState()
    val searchText by viewModel.searchText.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()
    val showSuccessAnimation by viewModel.showSuccessAnimation.collectAsState()
    val context = LocalContext.current // Context alınır

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var isSearchFocused by remember { mutableStateOf(false) }

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.done))
    val progress by animateLottieCompositionAsState(
        composition,
        isPlaying = showSuccessAnimation,
        iterations = 1,
        restartOnPlay = true
    )

    // Lottie animasyonu bittiğinde durumu sıfırla
    LaunchedEffect(progress) {
        if (progress >= 1f && showSuccessAnimation) {
            viewModel.resetSuccessAnimation()
        }
    }

    // READ_CONTACTS izni kontrolü ve talebi
    var hasReadContactsPermission by remember { mutableStateOf(
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    ) }

    val readContactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasReadContactsPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasReadContactsPermission) {
            readContactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    Scaffold(
        topBar = {
            Column { // TopAppBar ve SearchBar'ı dikeyde tutmak için Column kullandık
                TopAppBar(
                    title = { Text("Contacts") },
                    actions = {
                        IconButton(onClick = { navController.navigate("add") }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Contact")
                        }
                    }
                )
                // Arama Çubuğu
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { viewModel.onSearchTextChanged(it) },
                    label = { Text("Search Name, Surname or Phone...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (searchText.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchTextChanged("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear Search")
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            // ENTER'a basıldığında geçmişe kaydet ve klavyeyi kapat
                            viewModel.addSearchTermToHistory(searchText)
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .focusRequester(focusRequester)
                        .onFocusChanged { isSearchFocused = it.isFocused } // Odak değişimini yakalar
                )
            }
        },
        content = { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

                // Kişiler Listesi
                val totalContactsInGroups = groupedContacts.values.sumOf { it.size }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    if (totalContactsInGroups == 0) {
                        item {
                            val message = if (searchText.isNotEmpty()) {
                                "'$searchText' ile eşleşen kişi bulunamadı."
                            } else {
                                "Henüz kişi eklenmedi. '+' butonuna basarak yeni kişi ekleyin."
                            }

                            Text(
                                text = message,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        // Grupları alfabetik olarak sırala (A, B, C...)
                        groupedContacts.keys.sorted().forEach { initial ->
                            // Grup Başlığı (Initial)
                            item {
                                Text(
                                    text = initial.toString(),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }

                            // O gruba ait kişiler
                            items(groupedContacts[initial] ?: emptyList(), key = { it.phone }) { contact ->

                                /*val isDeviceContact = remember(contact.phone) {
                                    viewModel.checkDeviceContactStatus(context, contact.phone)
                                }*/
                                var isDeviceContact by remember(contact.phone) { mutableStateOf(false) }

                                LaunchedEffect(contact.phone) {
                                    try {
                                        val result = withContext(Dispatchers.IO) {
                                            viewModel.checkDeviceContactStatus(context, contact.phone)
                                        }
                                        isDeviceContact = result
                                    } catch (_: Exception) {
                                        isDeviceContact = false
                                    }
                                }

                                SwipeRow(
                                    itemContent = {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { navController.navigate("profile/${contact.phone}?mode=view") }
                                                .padding(16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically // Dikeyde hizalama
                                        ) {
                                            // YENİ: Fotoğraf
                                            ContactImage(
                                                imageUri = contact.imageUri,
                                                modifier = Modifier.size(40.dp)
                                            )
                                            Spacer(Modifier.width(16.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                val displayName = (contact.name + " " + contact.surname).trim()
                                                Text(
                                                    if (displayName.isNotEmpty()) displayName else contact.phone,
                                                    style = MaterialTheme.typography.titleMedium
                                                )
                                                if (displayName.isNotEmpty()) {
                                                    Text(contact.phone, color = MaterialTheme.colorScheme.primary)
                                                }
                                            }
                                            // YENİ: Cihaz Rehberi İkonu
                                            if (isDeviceContact) {
                                                Spacer(Modifier.width(8.dp))
                                                Icon(
                                                    imageVector = Icons.Default.PhoneAndroid,
                                                    contentDescription = "Device Contact",
                                                    tint = MaterialTheme.colorScheme.tertiary
                                                )
                                            }
                                        }
                                        },
                                    onSwipeLeft = {
                                        navController.navigate("profile/${contact.phone}")
                                    },
                                    onSwipeRight = {
                                        viewModel.deleteContact(contact)
                                    }
                                )
                                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                            }
                        }
                    }
                }

                // Arama Geçmişi Alanı (Listenin üzerine katman olarak eklenmiştir)
                if (isSearchFocused && searchText.isEmpty() && searchHistory.isNotEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 0.dp), // Listenin hemen üstüne hizala
                        shadowElevation = 4.dp // Gölgelendirme ile listenin üstünde olduğu belli olur
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(horizontal = 16.dp)
                        ) {
                            searchHistory.forEach { term ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.onSearchTextChanged(term)
                                            viewModel.addSearchTermToHistory(term)
                                            focusManager.clearFocus()
                                            keyboardController?.hide()
                                        }
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = term,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(onClick = { viewModel.removeSearchTermFromHistory(term) }) {
                                        Icon(Icons.Default.Close, contentDescription = "Sil", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                                Divider()
                            }
                        }
                    }
                }

                // Lottie Animasyonu (Listenin üstünde kalmalı)
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