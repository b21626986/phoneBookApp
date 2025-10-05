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
import coil.compose.AsyncImage // Coil importu
import com.example.phonebookapp.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Bir görsel URI'sinden baskın rengi hesaplar.
 */
@Composable
fun getDominantColor(imageUri: String?): State<Color> {
    val context = LocalContext.current
    val defaultColor = MaterialTheme.colorScheme.primary
    val dominantColor = remember { mutableStateOf(defaultColor) }

    LaunchedEffect(imageUri) {
        if (!imageUri.isNullOrBlank()) {
            val uri = Uri.parse(imageUri)
            try {
                // Bitmap'i asenkron olarak yükle ve Palette API'ı çalıştır
                withContext(Dispatchers.IO) {
                    val bitmap: Bitmap = when {
                        // android.resource://.../resId formatı için doğrudan resource'tan çöz
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
                                // Tip/ad formatı için fallback: contentResolver ile dene
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
 * Kişi fotoğrafını gösteren ve gölge efektini uygulayan Composable.
 */
@Composable
fun ContactImage(imageUri: String?, modifier: Modifier = Modifier, shadowColor: Color = Color.Gray) {
    Surface(
        shape = CircleShape,
        modifier = modifier.shadow(
            elevation = 16.dp, // Gölge derinliği
            shape = CircleShape,
            ambientColor = shadowColor, // Baskın renge göre gölge
            spotColor = shadowColor
        )
    ) {
        if (imageUri.isNullOrBlank()) {
            Image(
                painter = painterResource(id = R.drawable.ic_contact_photo),
                contentDescription = "No Photo",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )
        } else {
            // Coil ile URI'den görsel yükleme
            AsyncImage(
                model = Uri.parse(imageUri),
                contentDescription = "Contact Photo",
                modifier = Modifier.fillMaxSize(),
                // Görselin daire şekline uyması için kırpma
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
        }
    }
}