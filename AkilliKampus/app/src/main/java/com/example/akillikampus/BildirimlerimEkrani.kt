package com.example.akillikampus

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BildirimlerimEkrani(
    geriDon: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid

    // İki listeyi birleştireceğiz: Kişisel + Acil Duyurular
    var kisiselListe by remember { mutableStateOf<List<KisiselBildirim>>(emptyList()) }
    var acilDuyurular by remember { mutableStateOf<List<Bildirim>>(emptyList()) }

    // 1. KİŞİSEL BİLDİRİMLERİ ÇEK
    LaunchedEffect(uid) {
        if (uid != null) {
            db.collection("KisiselBildirimler")
                .whereEqualTo("kullaniciId", uid)
                .addSnapshotListener { v, _ ->
                    if (v != null) kisiselListe = v.toObjects(KisiselBildirim::class.java)
                }
        }
    }

    // 2. ACİL DUYURULARI ÇEK (Herkese Gider)
    LaunchedEffect(Unit) {
        db.collection("Bildirimler")
            .whereEqualTo("tur", "Acil")
            .addSnapshotListener { v, _ ->
                if (v != null) acilDuyurular = v.toObjects(Bildirim::class.java)
            }
    }

    // LİSTELERİ BİRLEŞTİR VE TARİHE GÖRE SIRALA
    val tumBildirimler = remember(kisiselListe, acilDuyurular) {
        val liste1 = kisiselListe.map {
            KisiselBildirim(it.id, it.kullaniciId, it.mesaj, it.tarih, "Bilgi")
        }
        val liste2 = acilDuyurular.map {
            KisiselBildirim(it.id, "Herkes", "ACİL DUYURU: ${it.baslik}", it.tarih, "Acil")
        }
        (liste1 + liste2).sortedByDescending { it.tarih }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Bildirim Kutusu") },
                navigationIcon = {
                    IconButton(onClick = geriDon) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF6200EA), titleContentColor = Color.White)
            )
        }
    ) { p ->
        Column(modifier = Modifier.padding(p).padding(16.dp)) {
            if (tumBildirimler.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Hiç yeni bildiriminiz yok.", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(tumBildirimler) { bildirim ->
                        val isAcil = bildirim.tur == "Acil"
                        Card(
                            colors = CardDefaults.cardColors(containerColor = if(isAcil) Color(0xFFFFEBEE) else Color.White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if(isAcil) Icons.Default.Warning else Icons.Default.Notifications,
                                    null,
                                    tint = if(isAcil) Color.Red else Color(0xFF6200EA)
                                )
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text(bildirim.mesaj, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    val tarih = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault()).format(Date(bildirim.tarih))
                                    Text(tarih, fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}