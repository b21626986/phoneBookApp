package com.example.phonebookapp.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit
) {
    // Animatable kullanıyoruz
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var isActionTriggered by remember { mutableStateOf(false) }

    // Swipe eşikleri
    val swipeThreshold = 200f // İşlem tetikleme eşiği
    val backgroundThreshold = 100f // Arka plan gösterme eşiği

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
                            
                            if (!isActionTriggered) {
                                if (currentValue > swipeThreshold) {
                                    isActionTriggered = true
                                    onSwipeRight()
                                    offsetX.snapTo(0f)
                                } else if (currentValue < -swipeThreshold) {
                                    isActionTriggered = true
                                    onSwipeLeft()
                                    offsetX.snapTo(0f)
                                } else if (currentValue != 0f) {
                                    // Eşiği geçmediyse geri dön
                                    offsetX.animateTo(0f)
                                }
                            }
                        }
                    }
                ) { change, dragAmount ->
                    if (!isActionTriggered) {
                        scope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount)
                        }
                    }
                }
            }
    ) {
        // Arka plan Delete / Edit göstermek için
        if (offsetX.value > backgroundThreshold) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Red)
                    .padding(16.dp)
            ) {
                Text("Delete", color = Color.White)
            }
        } else if (offsetX.value < -backgroundThreshold) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Blue)
                    .padding(16.dp)
            ) {
                Text("Edit", color = Color.White)
            }
        }

        // Satır içeriği
        Box(
            modifier = Modifier.offset { IntOffset(offsetX.value.toInt(), 0) }
        ) {
            itemContent()
        }
    }

    // Reset action trigger when offset returns to 0
    LaunchedEffect(offsetX.value) {
        if (offsetX.value == 0f) {
            isActionTriggered = false
        }
    }
}