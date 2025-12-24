package com.example.akillikampus

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnaSayfa(
    cikisYap: () -> Unit,
    bildirimEkleSayfasinaGit: () -> Unit,
    talepOlusturSayfasinaGit: () -> Unit,
    talepleriGorSayfasinaGit: () -> Unit,
    haritayaGit: () -> Unit,
    bildirimDetayaGit: (String) -> Unit,
    profilSayfasinaGit: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    // --- DEGISKENLER ---
    var kullaniciRolu by remember { mutableStateOf("User") }
    var bildirimListesi by remember { mutableStateOf<List<Bildirim>>(emptyList()) }

    // 1. KULLANICI ROLUNU CEK
    LaunchedEffect(auth.currentUser) {
        auth.currentUser?.uid?.let { uid ->
            db.collection("Kullanicilar").document(uid).get().addOnSuccessListener {
                kullaniciRolu = it.getString("rol") ?: "User"
            }
        }
    }

    // 2. BİLDİRİMLERİ ÇEK
    LaunchedEffect(Unit) {
        db.collection("Bildirimler")
            .orderBy("tarih", Query.Direction.DESCENDING)
            .addSnapshotListener { value, _ ->
                if (value != null) {
                    bildirimListesi = value.toObjects(Bildirim::class.java)
                }
            }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Akıllı Kampüs") },
                // SOL ÜST: PROFIL IKONU (Burayı 'Person' yaptık, garanti çalışır)
                navigationIcon = {
                    IconButton(onClick = profilSayfasinaGit) {
                        Icon(Icons.Default.Person, contentDescription = "Profil", tint = Color.White)
                    }
                },
                // SAĞ ÜST: ÇIKIŞ IKONU
                actions = {
                    IconButton(onClick = { auth.signOut(); cikisYap() }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Çıkış", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF6200EA), titleContentColor = Color.White)
            )
        }
    ) { p ->
        Column(modifier = Modifier.padding(p).padding(16.dp)) {

            // KULLANICI BILGISI
            Text("Merhaba, ${auth.currentUser?.email?.substringBefore("@")}", color = Color.Gray)
            Text("Yetki: $kullaniciRolu", fontWeight = FontWeight.Bold, color = if(kullaniciRolu=="Admin") Color.Red else Color.Blue)

            Spacer(Modifier.height(16.dp))

            // --- BUTONLAR ---
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                if (kullaniciRolu == "Admin") {
                    IslemKarti(Color(0xFFFFD7D7), "DUYURU\nEKLE", Color.Red, bildirimEkleSayfasinaGit, Modifier.weight(1f))
                    IslemKarti(Color(0xFFFFF8E1), "YÖNETİM\nPANELİ", Color(0xFFF57F17), talepleriGorSayfasinaGit, Modifier.weight(1f))
                } else {
                    IslemKarti(Color(0xFFE7FFD7), "TALEP\nOLUŞTUR", Color(0xFF2E7D32), talepOlusturSayfasinaGit, Modifier.weight(1f))
                    IslemKarti(Color(0xFFE1F5FE), "TALEPLERİMİ\nGÖR", Color(0xFF0277BD), talepleriGorSayfasinaGit, Modifier.weight(1f))
                }
            }

            Spacer(Modifier.height(16.dp))
            Button(onClick = haritayaGit, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) {
                Icon(Icons.Default.LocationOn, null); Spacer(Modifier.width(8.dp)); Text("KAMPÜS HARİTASI")
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider() // Eski Divider yerine bu kullaniliyor
            Spacer(Modifier.height(16.dp))

            // --- DUYURU LISTESI ---
            Text("Son Duyurular", fontWeight = FontWeight.Bold, fontSize = 18.sp)

            if (bildirimListesi.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    Text("Henüz yayınlanmış bir duyuru yok.", color = Color.LightGray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(bildirimListesi) { bildirim ->

                        val turSinifi = BildirimTuru.getir(bildirim.tur)

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp),
                            onClick = { bildirimDetayaGit(bildirim.id) }
                        ) {
                            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = turSinifi.ikon,
                                    contentDescription = null,
                                    tint = turSinifi.renk,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text(bildirim.baslik, fontWeight = FontWeight.Bold)
                                    Text(bildirim.aciklama, maxLines = 1, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IslemKarti(renk: Color, yazi: String, yaziRengi: Color, tiklaninca: () -> Unit, modifier: Modifier = Modifier) {
    Card(colors = CardDefaults.cardColors(containerColor = renk), modifier = modifier.height(80.dp).clickable { tiklaninca() }) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(yazi, color = yaziRengi, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}