package com.example.akillikampus

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BildirimEkleEkrani(geriDon: () -> Unit) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    var baslik by remember { mutableStateOf("") }
    var aciklama by remember { mutableStateOf("") }


    var secilenTur by remember { mutableStateOf(BildirimTuru.GENEL) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Duyuru Yayınla") },
                navigationIcon = { IconButton(onClick = geriDon) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFFD32F2F), titleContentColor = Color.White)
            )
        }
    ) { p ->
        Column(modifier = Modifier.padding(p).padding(16.dp)) {

            Text("Tür Seçin:", fontWeight = FontWeight.Bold)
            // SINIFTAKI TUM TURLERI DONGUYE SOKUYORUZ
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                // Sadece ilk 3-4 tanesini gösterelim sığması için, veya LazyRow kullanırız
                BildirimTuru.entries.take(4).forEach { tur ->
                    FilterChip(
                        selected = (secilenTur == tur),
                        onClick = { secilenTur = tur },
                        label = { Text(tur.ad) },
                        // RENGİ SINIFTAN ALIYOR
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = tur.renk,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            OutlinedTextField(baslik, { baslik = it }, label = { Text("Başlık") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(aciklama, { aciklama = it }, label = { Text("İçerik") }, modifier = Modifier.fillMaxWidth().height(120.dp))

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    if (baslik.isNotEmpty() && aciklama.isNotEmpty()) {
                        val yeniBildirim = Bildirim(
                            id = UUID.randomUUID().toString(),
                            baslik = baslik, aciklama = aciklama,
                            tur = secilenTur.ad, // SINIFTAN ADINI ALIP KAYDEDIYORUZ
                            tarih = System.currentTimeMillis()
                        )
                        db.collection("Bildirimler").document(yeniBildirim.id).set(yeniBildirim)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Yayınlandı!", Toast.LENGTH_SHORT).show()
                                geriDon()
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = secilenTur.renk) // BUTON RENGİ DE DEĞİŞİR
            ) {
                Text("YAYINLA")
            }
        }
    }
}