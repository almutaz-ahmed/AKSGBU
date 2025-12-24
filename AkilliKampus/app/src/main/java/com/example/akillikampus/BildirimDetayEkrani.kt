package com.example.akillikampus

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BildirimDetayEkrani(
    bildirimId: String,
    geriDon: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    var bildirim by remember { mutableStateOf<Bildirim?>(null) }

    LaunchedEffect(bildirimId) {
        db.collection("Bildirimler").document(bildirimId).get().addOnSuccessListener {
            bildirim = it.toObject(Bildirim::class.java)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Duyuru Detayı") },
                navigationIcon = {
                    IconButton(onClick = geriDon) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFFD32F2F), titleContentColor = Color.White)
            )
        }
    ) { p ->
        Column(
            modifier = Modifier
                .padding(p)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (bildirim == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                val b = bildirim!! // Güvenli erişim

                // 1. KATEGORİ VE BAŞLIK
                Text(text = b.tur.uppercase(), color = Color.Red, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(Modifier.height(8.dp))
                Text(text = b.baslik, fontSize = 26.sp, fontWeight = FontWeight.Bold, lineHeight = 32.sp)

                Spacer(Modifier.height(24.dp))

                // 2. ÖZEL BİLGİ KUTUSU (İstediğin Sözel Konum ve Tarih)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)), // Hafif kırmızı arka plan
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Tarih Satırı
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DateRange, null, tint = Color.Red)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Yayınlanma Tarihi", fontSize = 12.sp, color = Color.Gray)
                                val tarih = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("tr")).format(Date(b.tarih))
                                Text(tarih, fontWeight = FontWeight.Bold)
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.Red.copy(alpha = 0.2f))

                        // Konum Satırı
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, tint = Color.Red)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Konum / İlgili Yer", fontSize = 12.sp, color = Color.Gray)
                                Text(b.konum, fontWeight = FontWeight.Bold) // Sözel metin (Örn: Rektörlük Binası)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // 3. AÇIKLAMA METNİ
                Text("Duyuru İçeriği:", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(8.dp))
                Text(text = b.aciklama, fontSize = 16.sp, lineHeight = 24.sp, color = Color.DarkGray)
            }
        }
    }
}