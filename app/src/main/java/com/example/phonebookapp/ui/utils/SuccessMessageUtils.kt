package com.example.phonebookapp.ui.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SuccessMessagePopup(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE8F5E9) // Açık Yeşil
            ),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = Color(0xFF4CAF50), // Koyu Yeşil
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF4CAF50) // Koyu Yeşil
                )
            }
        }
    }
}