package com.example.phonebookapp.ui.utils

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color

/**
 * Sağ, sol ve altı tamamen kaplayan, sadece üstte arka planı gösteren,
 * yuvarlak üst köşelere sahip özel Dialog yapısı (Bottom Sheet taklidi).
 *
 * NOT: Bu bileşen, navigasyon için NavHost/composable içinde değil,
 * doğrudan ekran içeriğinde çağrılmak üzere tasarlanmıştır.
 */
@Composable
fun FullWidthBottomSheetDialog(
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    // Android'deki tam ekran bottom sheet davranışını taklit eder
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false, // Tam ekran genişliği için KESİNLİKLE GEREKLİ
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            // Arka planı hafifçe transparan yaparak arkadaki ekranı gösterir
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(Color.Black.copy(alpha = 0.5f)) // Arka plan gölgelendirmesi
                .clickable { onDismissRequest() }, // Dışarı tıklama ile kapatma
            contentAlignment = Alignment.BottomCenter
        ) {
            // Asıl içerik kutusu
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    // Altı tamamen kaplar
                    .height(800.dp) // Yüksekliği ekranın büyük bir kısmı olarak ayarlıyoruz
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(topStart = 128.dp, topEnd = 128.dp)
                    )
            ) {
                // İçerik, dialog'u kapatma işlevini üstten almalıdır.
                content()
            }
        }
    }
}