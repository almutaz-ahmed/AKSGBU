package com.example.akillikampus

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HaritaDetayEkrani(
    baslik: String,
    tur: String,
    neKadarOnce: String,
    geriDon: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Bildirim Detayı", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { geriDon() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF6200EA),
                    titleContentColor = Color.White
                )
            )
        }
    ) { dolguDegerleri ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(dolguDegerleri)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // IKON GOSTERIMI
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = if (tur == "Duyuru") Color.Red else Color.Blue
            )

            Spacer(modifier = Modifier.height(24.dp))

            // BASLIK
            Text(text = baslik, fontSize = 28.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(16.dp))

            // BILGI KARTI
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)), // HAFIF GRI
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Kategori: $tur", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Paylaşılma Zamanı: $neKadarOnce", color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Bu bildirim kampüs haritası üzerindeki işaretli konumdan oluşturulmuştur. Detaylı bilgi için ilgili birimle iletişime geçebilirsiniz.")
                }
            }
        }
    }
}

