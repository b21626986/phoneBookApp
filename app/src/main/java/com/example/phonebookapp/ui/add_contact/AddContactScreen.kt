package com.example.phonebookapp.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.*
import com.example.phonebookapp.R
import com.example.phonebookapp.model.Contact
import com.example.phonebookapp.ui.theme.FigmaGray_950
import com.example.phonebookapp.ui.theme.FigmaPrimaryBlue
import com.example.phonebookapp.ui.theme.ThemedScreen
import com.example.phonebookapp.ui.utils.ContactImage
import com.example.phonebookapp.ui.utils.FullWidthBottomSheetDialog
import com.example.phonebookapp.ui.utils.getDominantColor
import com.example.phonebookapp.viewmodel.ContactViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactScreen(
    viewModel: ContactViewModel,
    navController: NavHostController
) {
    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<String?>(null) }
    val shadowColor by getDominantColor(imageUri)
    val context = LocalContext.current
    var isSavingSuccessful by remember { mutableStateOf(false) }


    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.done))
    val progress by animateLottieCompositionAsState(
        composition,
        isPlaying = isSavingSuccessful,
        iterations = 1,
        restartOnPlay = true
    )

    //Navigates to Contacts Screen if saving is successful
    LaunchedEffect(isSavingSuccessful) {
        if (isSavingSuccessful) {
            delay(3000)
            navController.navigate("contacts") {
                popUpTo("contacts") { inclusive = true }
            }
        }
    }

    //Starts camera or gallery to pick a photo
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri.toString()
        }
    }

    //Opens the bottom sheet when it is wanted to add a new contact
    FullWidthBottomSheetDialog(
        onDismissRequest = { navController.popBackStack() } // Closes when you tap out of the bottom sheet
    ) {
        ThemedScreen(primaryColor = FigmaPrimaryBlue) {
            if (isSavingSuccessful) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Lottie animation and its texts
                        if (composition != null) {
                            LottieAnimation(
                                composition,
                                progress = { progress },
                                modifier = Modifier.size(150.dp)
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = "All Done!",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp
                            ),
                            color = FigmaGray_950
                        )

                        Text(
                            text = "New contact saved 🎉",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Normal,
                                fontSize = 18.sp
                            ),
                            color = FigmaGray_950
                        )
                    }
                }
                return@ThemedScreen
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                    )
                    .padding(top = 42.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                //Design of the tabBar which has title and done/cancel buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("Cancel", color = FigmaPrimaryBlue)
                    }

                    Text(
                        text = "New Contact",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )

                    //Gets new contacts info when it is clicked Done button
                    val isFormValid = phoneNumber.isNotBlank()
                    TextButton(
                        onClick = {
                            val newContact = Contact(name, surname, phoneNumber, imageUri)
                            viewModel.addContact(newContact)
                            isSavingSuccessful = true
                        },
                        enabled = isFormValid
                    ) {
                        Text("Done", color = FigmaPrimaryBlue)
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ContactImage(
                        imageUri = imageUri,
                        shadowColor = shadowColor,
                        modifier = Modifier
                            .size(120.dp)
                            .clickable {
                                imagePickerLauncher.launch("image/*")
                            }
                    )
                    Spacer(Modifier.height(16.dp))

                    TextButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = FigmaPrimaryBlue
                        )
                    ) {
                        Text(if (imageUri == null) "Add Photo" else "Change Photo")
                    }

                    if (imageUri != null) {
                        TextButton(onClick = { imageUri = null }) {
                            Text("Remove Photo", color = MaterialTheme.colorScheme.error)
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("First Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = surname,
                        onValueChange = { surname = it },
                        label = { Text("Last Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}