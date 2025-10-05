package com.example.phonebookapp.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import com.example.phonebookapp.R
import com.example.phonebookapp.ui.theme.FigmaPrimaryBlue
import com.example.phonebookapp.ui.theme.FigmaRedError
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun SwipeRow(
    itemContent: @Composable (isSwiped: Boolean) -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var isSwipeOpen by remember { mutableStateOf(false) }

    val swipeThreshold = 140f
    val backgroundWidth = 140.dp
    val backgroundThreshold = 80f

    // Derived state to check if the row is actively being dragged or animated away from 0.
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
                                offsetX.animateTo(-backgroundWidth.toPx())
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
        //To display the background edit and delete icons
        if (offsetX.value < -backgroundThreshold) {
            Row(
                modifier = Modifier
                    .matchParentSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .width(backgroundWidth)
                        .fillMaxHeight(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Edit button
                    Button(
                        onClick = {
                            isSwipeOpen = false
                            scope.launch { offsetX.animateTo(0f) }
                            onEditClick()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FigmaPrimaryBlue),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_edit),
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Delete button
                    Button(
                        onClick = {
                            isSwipeOpen = false
                            scope.launch { offsetX.animateTo(0f) }
                            onDeleteClick()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FigmaRedError),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_delete),
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier.offset { IntOffset(offsetX.value.toInt(), 0) }
        ) {
            itemContent(isActivelySwiped)
        }
    }

    // If the offset animates back to exactly 0, ensure the swipe state is reset.
    LaunchedEffect(offsetX.value) {
        if (offsetX.value == 0f) {
            isSwipeOpen = false
        }
    }
}