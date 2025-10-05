package com.example.phonebookapp.ui.screens

import android.R.attr.tint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.*
import com.example.phonebookapp.R
import com.example.phonebookapp.ui.SwipeRow
import com.example.phonebookapp.ui.theme.FigmaPrimaryBlue
import com.example.phonebookapp.ui.theme.FigmaGray_50
import androidx.compose.foundation.shape.CircleShape
import com.example.phonebookapp.ui.theme.FigmaWhite
import com.example.phonebookapp.ui.theme.FigmaGray_950
import com.example.phonebookapp.ui.utils.ContactImage
import com.example.phonebookapp.ui.utils.SuccessMessagePopup
import com.example.phonebookapp.viewmodel.ContactViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import com.example.phonebookapp.ui.theme.FigmaGray_200

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

    val contactToDelete by viewModel.contactToDelete.collectAsState()
    val showDeleteConfirmation by viewModel.showDeleteConfirmation.collectAsState()

    val successMessage by viewModel.successMessage.collectAsState()
    val context = LocalContext.current

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var isSearchFocused by remember { mutableStateOf(false) }

    // Lottie Animation Setup
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.done))
    val progress by animateLottieCompositionAsState(
        composition,
        isPlaying = showSuccessAnimation,
        iterations = 1,
        restartOnPlay = true
    )

    // Used to reset the state when the animation finishes
    LaunchedEffect(progress) {
        if (progress >= 1f && showSuccessAnimation) {
            viewModel.resetSuccessAnimation()
        }
    }

    // Clears the success message after 3 seconds of being displayed
    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearSuccessMessage()
        }
    }

    Scaffold(
        containerColor = FigmaGray_50,
        topBar = {
            Column(
                modifier = Modifier.background(FigmaGray_50)
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = "Contacts",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = FigmaGray_50
                    ),
                    actions = {
                        // Add New Contact Button
                        FloatingActionButton(
                            onClick = { navController.navigate("add") },
                            modifier = Modifier
                                .padding(end = 16.dp, top = 8.dp, bottom = 8.dp)
                                .size(40.dp),
                            shape = CircleShape,
                            containerColor = FigmaPrimaryBlue,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Contact")
                        }
                    }
                )
                // Search Bar
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { viewModel.onSearchTextChanged(it) },
                    label = { Text("Search by name") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        // Clears if text exists or Closes if focused
                        if (searchText.isNotEmpty() || isSearchFocused) {
                            IconButton(onClick = {
                                if (searchText.isNotEmpty()) {
                                    viewModel.onSearchTextChanged("")
                                } else if (isSearchFocused) {
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                }
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear Search or Exit Search Mode")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        // When the 'Search' button on the keyboard is pressed
                        onSearch = {
                            viewModel.addSearchTermToHistory(searchText)
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = FigmaWhite,
                        unfocusedContainerColor = FigmaWhite,
                        disabledContainerColor = FigmaWhite,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        errorBorderColor = Color.Transparent,
                        disabledBorderColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .focusRequester(focusRequester)
                        .onFocusChanged { isSearchFocused = it.isFocused }
                )
            }
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {

                val totalContactsInGroups = groupedContacts.values.sumOf { it.size }

                val showContactList = !isSearchFocused || searchText.isNotEmpty()

                if (showContactList) {

                    //No Search Results Screen
                    if (totalContactsInGroups == 0 && searchText.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 64.dp, start = 32.dp, end = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Top
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_no_contact_found),
                                contentDescription = "No results icon",
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = "No Results",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "The user you are looking for could not be found.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    //Empty Contact List Screen
                    else if (totalContactsInGroups == 0 && searchText.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.no_contacts),
                                contentDescription = "No Contacts Icon",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = "No Contacts",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Contacts you've added will appear here.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(24.dp))
                            TextButton(onClick = { navController.navigate("add") }) {
                                Text(
                                    text = "Create New Contact",
                                    color = FigmaPrimaryBlue,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }
                    }
                    //Contact List
                    else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            groupedContacts.keys.sorted().forEach { initial ->
                                item(key = "group_${initial}") {
                                    // Card for each initial letter group
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        elevation = CardDefaults.cardElevation(2.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = initial.toString(),
                                                style = MaterialTheme.typography.titleLarge,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                            )
                                            Divider(modifier = Modifier.padding(start = 8.dp))
                                            groupedContacts[initial]?.forEachIndexed { index, contact ->
                                                // Device Contact Check
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

                                                // Swipable Row (for Edit/Delete actions)
                                                SwipeRow(
                                                    itemContent = { isSwiped ->
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .background(MaterialTheme.colorScheme.surface)
                                                                .clickable { navController.navigate("profile/${contact.phone}?mode=view") }
                                                                .padding(16.dp),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
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
                                                            if (isDeviceContact && !isSwiped) {
                                                                Spacer(Modifier.width(8.dp))
                                                                Icon(
                                                                    imageVector = Icons.Default.PhoneAndroid,
                                                                    contentDescription = "Device Contact",
                                                                    tint = MaterialTheme.colorScheme.tertiary
                                                                )
                                                            }
                                                        }
                                                    },
                                                    onEditClick = {
                                                        navController.navigate("profile/${contact.phone}?mode=edit")
                                                    },
                                                    onDeleteClick = {
                                                        viewModel.startDelete(contact)
                                                    }
                                                )

                                                if (index < (groupedContacts[initial]?.size ?: 0) - 1) {
                                                    Divider(modifier = Modifier.padding(start = 16.dp),
                                                        thickness = 0.5.dp,
                                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                //Search History
                if (isSearchFocused && searchText.isEmpty() && searchHistory.isNotEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .zIndex(1f),
                        shadowElevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(FigmaWhite)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(FigmaGray_50)
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "SEARCH HISTORY",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                TextButton(onClick = { viewModel.clearSearchHistory() }) {
                                    Text(
                                        text = "Clear All",
                                        color = FigmaPrimaryBlue,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }

                            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
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
                                        IconButton(
                                            onClick = { viewModel.removeSearchTermFromHistory(term) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Remove Search Term",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                            )
                                        }

                                        Spacer(Modifier.width(8.dp))

                                        Text(
                                            text = term,
                                            modifier = Modifier.weight(1f),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                }
                            }
                        }
                    }
                }

                //Delete Confirmation Pop-up
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
                                .background(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                                )
                                .padding(24.dp)
                                .clickable(enabled = false, onClick = { /* İçeride tıklamayı engelle */ }),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Delete Contact",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Are you sure you want to delete this contact?",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(24.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.cancelDelete() },
                                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        FigmaGray_950
                                    ),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = FigmaGray_950,
                                        containerColor = Color.Transparent
                                    )
                                ) {
                                    Text("No")
                                }

                                Button(
                                    onClick = {
                                        viewModel.deleteContactConfirmed(contactToDelete!!)
                                    },
                                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = FigmaGray_950,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text("Yes")
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}