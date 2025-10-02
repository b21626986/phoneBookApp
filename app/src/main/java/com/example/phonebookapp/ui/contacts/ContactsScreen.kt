package com.example.phonebookapp.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.*
import com.example.phonebookapp.R
import com.example.phonebookapp.ui.SwipeRow
import com.example.phonebookapp.viewmodel.ContactViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    viewModel: ContactViewModel,
    navController: NavHostController
) {
    val contacts by viewModel.contacts.collectAsState()
    val showSuccessAnimation by viewModel.showSuccessAnimation.collectAsState()
    val animationType by viewModel.animationType.collectAsState()

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contacts") },
                actions = {
                    IconButton(onClick = { navController.navigate("add") }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Contact")
                    }
                }
            )
        },
        content = { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

                // Boş liste kontrolü
                if (contacts.isEmpty()) {
                    Text(
                        text = "Henüz kişi eklenmedi. '+' butonuna basarak yeni kişi ekleyin.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(contacts, key = { it.phone }) { contact ->
                            // SwipeRow'u ayrı bir dosyadan çağırıyoruz
                            SwipeRow(
                                itemContent = {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("${contact.name} ${contact.surname}", style = MaterialTheme.typography.titleMedium)
                                        Text(contact.phone, color = MaterialTheme.colorScheme.primary)
                                    }
                                },
                                onSwipeLeft = {
                                    navController.navigate("profile/${contact.phone}")
                                },
                                onSwipeRight = {
                                    viewModel.deleteContact(contact)
                                }
                            )
                            Divider()
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

// ContactsScreen.kt'deki eski SwipeRow kaldırılıp sadece ui/SwipeRow.kt kullanılacak.