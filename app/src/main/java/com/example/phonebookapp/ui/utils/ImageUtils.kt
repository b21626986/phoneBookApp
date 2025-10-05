package com.example.phonebookapp.ui.utils

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import com.example.phonebookapp.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Composable
fun getDominantColor(imageUri: String?): State<Color> {
    val context = LocalContext.current
    val defaultColor = MaterialTheme.colorScheme.primary
    val dominantColor = remember { mutableStateOf(defaultColor) }

    // Relaunch the effect whenever the imageUri changes.
    LaunchedEffect(imageUri) {
        if (!imageUri.isNullOrBlank()) {
            val uri = Uri.parse(imageUri)
            try {
                withContext(Dispatchers.IO) {
                    val bitmap: Bitmap = when {
                        uri.scheme == "android.resource" -> {
                            val resId = uri.lastPathSegment?.toIntOrNull()
                            if (resId != null) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                    val source = ImageDecoder.createSource(context.resources, resId)
                                    ImageDecoder.decodeBitmap(source)
                                } else {
                                    BitmapFactory.decodeResource(context.resources, resId)
                                }
                            } else {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                                    ImageDecoder.decodeBitmap(source)
                                } else {
                                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                                }
                            }
                        }
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> {
                            val source = ImageDecoder.createSource(context.contentResolver, uri)
                            ImageDecoder.decodeBitmap(source)
                        }
                        else -> MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                    }

                    // Uses the Android Palette library to extract the dominant color
                    Palette.from(bitmap).generate { palette ->
                        palette?.dominantSwatch?.rgb?.let { colorValue ->
                            dominantColor.value = Color(colorValue)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                dominantColor.value = defaultColor
            }
        } else {
            dominantColor.value = defaultColor
        }
    }

    return dominantColor
}

/**
 * Displays a contact photo, handling both image URIs and placeholder display.
 * It applies a circular shadow colored by the provided shadowColor.*/
@Composable
fun ContactImage(imageUri: String?, modifier: Modifier = Modifier, shadowColor: Color = Color.Gray) {
    Surface(
        shape = CircleShape,
        modifier = modifier.shadow(
            elevation = 16.dp,
            shape = CircleShape,
            ambientColor = shadowColor,
            spotColor = shadowColor
        )
    ) {
        if (imageUri.isNullOrBlank()) {
            // Displays a default placeholder image if no URI is provided
            Image(
                painter = painterResource(id = R.drawable.ic_contact_photo),
                contentDescription = "No Photo",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )
        } else {
            // Uses Coil's AsyncImage to load the image from the URI efficiently
            AsyncImage(
                model = Uri.parse(imageUri),
                contentDescription = "Contact Photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
        }
    }
}