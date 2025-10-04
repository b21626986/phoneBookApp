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
import kotlin.math.abs

@Composable
fun SwipeRow(
    // itemContent artık kaydırma durumunu alacak
    itemContent: @Composable (isSwiped: Boolean) -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var isSwipeOpen by remember { mutableStateOf(false) }

    val swipeThreshold = 120f
    val backgroundThreshold = 80f

    // Kaydırma miktarının sıfırdan farklı olup olmadığını kontrol eder
    val isActivelySwiped by remember {
        derivedStateOf { abs(offsetX.value) > 1f }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surface)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        scope.launch {
                            val currentValue = offsetX.value

                            if (currentValue < -swipeThreshold) {
                                isSwipeOpen = true
                                offsetX.animateTo(-120f)
                            } else if (currentValue > 0) {
                                isSwipeOpen = false
                                offsetX.animateTo(0f)
                            } else if (currentValue != 0f && !isSwipeOpen) {
                                offsetX.animateTo(0f)
                            }
                        }
                    }
                ) { change, dragAmount ->
                    scope.launch {
                        val newValue = offsetX.value + dragAmount
                        if (newValue <= 0) {
                            offsetX.snapTo(newValue)
                        } else if (isSwipeOpen) {
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
            // Kaydırma durumunu iletiyoruz
            itemContent(isActivelySwiped)
        }
    }

    // Reset swipe state when offset returns to 0
    LaunchedEffect(offsetX.value) {
        if (offsetX.value == 0f) {
            isSwipeOpen = false
        }
    }
}