package com.example.phonebookapp.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch

@Composable
fun SwipeRow(
    itemContent: @Composable () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    // Animatable kullanıyoruz
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var isSwipeOpen by remember { mutableStateOf(false) }

    // Swipe eşikleri
    val swipeThreshold = 120f // İşlem tetikleme eşiği
    val backgroundThreshold = 80f // Arka plan gösterme eşiği

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surface)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        // Drag bittiğinde işlem kontrolü yap
                        scope.launch {
                            val currentValue = offsetX.value

                            if (currentValue < -swipeThreshold) {
                                // Swipe left - açık pozisyonda kal
                                isSwipeOpen = true
                                offsetX.animateTo(-120f)
                            } else if (currentValue > 0) {
                                // Sağa swipe - kapat
                                isSwipeOpen = false
                                offsetX.animateTo(0f)
                            } else if (currentValue != 0f && !isSwipeOpen) {
                                // Eşiği geçmediyse geri dön
                                offsetX.animateTo(0f)
                            }
                        }
                    }
                ) { change, dragAmount ->
                    scope.launch {
                        val newValue = offsetX.value + dragAmount
                        // Sadece sola swipe'a izin ver, sağa swipe'da kapat
                        if (newValue <= 0) {
                            offsetX.snapTo(newValue)
                        } else if (isSwipeOpen) {
                            // Açıkken sağa swipe ile kapat
                            offsetX.snapTo(newValue)
                        }
                    }
                }
            }
    ) {
        // Arka plan Edit / Delete ikonları göstermek için
        if (offsetX.value < -backgroundThreshold) {
            Row(
                modifier = Modifier
                    .matchParentSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Edit ikonu
                IconButton(
                    onClick = {
                        isSwipeOpen = false
                        scope.launch { offsetX.animateTo(0f) }
                        onEditClick()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Delete ikonu
                IconButton(
                    onClick = {
                        isSwipeOpen = false
                        scope.launch { offsetX.animateTo(0f) }
                        onDeleteClick()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // Satır içeriği
        Box(
            modifier = Modifier.offset { IntOffset(offsetX.value.toInt(), 0) }
        ) {
            itemContent()
        }
    }

    // Reset swipe state when offset returns to 0
    LaunchedEffect(offsetX.value) {
        if (offsetX.value == 0f) {
            isSwipeOpen = false
        }
    }
}