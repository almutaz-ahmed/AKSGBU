package com.example.akillikampus

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilEkrani(
    geriDon: () -> Unit,
    cikisYap: () -> Unit,
    detayaGit: (String) -> Unit
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val uid = auth.currentUser?.uid

    // --- DEGISKENLER ---
    var adSoyad by remember { mutableStateOf("") }
    var birim by remember { mutableStateOf("") }
    var rol by remember { mutableStateOf("User") }
    var email by remember { mutableStateOf("") }

    // Ayarlar
    var bildirimAcil by remember { mutableStateOf(true) }
    var bildirimDers by remember { mutableStateOf(true) }

    // Takip Edilenler Listesi
    var takipEdilenler by remember { mutableStateOf<List<Talep>>(emptyList()) }

    // VERİLERİ ÇEK
    LaunchedEffect(uid) {
        if (uid != null) {
            // 1. Kullanıcı Bilgilerini Çek
            db.collection("Kullanicilar").document(uid).get().addOnSuccessListener { doc ->
                if (doc != null) {
                    adSoyad = doc.getString("adSoyad") ?: ""
                    birim = doc.getString("birim") ?: ""
                    rol = doc.getString("rol") ?: "User"
                    email = auth.currentUser?.email ?: ""

                    // Ayarları çek (varsa)
                    bildirimAcil = doc.getBoolean("ayarAcil") ?: true
                    bildirimDers = doc.getBoolean("ayarDers") ?: true
                }
            }

            // 2. Takip Edilen Talepleri Çek (Array Contains Sorgusu)
            db.collection("Talepler")
                .whereArrayContains("takipciler", uid)
                .addSnapshotListener { value, _ ->
                    if (value != null) {
                        takipEdilenler = value.toObjects(Talep::class.java)
                    }
                }
        }
    }

    // KAYDETME FONKSIYONU
    fun profiliGuncelle() {
        if (uid != null) {
            val veri = mapOf(
                "adSoyad" to adSoyad,
                "birim" to birim,
                "ayarAcil" to bildirimAcil,
                "ayarDers" to bildirimDers
            )
            db.collection("Kullanicilar").document(uid).update(veri)
                .addOnSuccessListener {
                    Toast.makeText(context, "Profil Güncellendi ✅", Toast.LENGTH_SHORT).show()
                }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profilim") },
                navigationIcon = {
                    IconButton(onClick = geriDon) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { auth.signOut(); cikisYap() }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, "Çıkış", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF6200EA), titleContentColor = Color.White)
            )
        }
    ) { p ->
        Column(
            modifier = Modifier
                .padding(p)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- 1. PROFIL KARTI ---
            Icon(Icons.Default.AccountCircle, null, modifier = Modifier.size(100.dp), tint = Color(0xFF6200EA))
            Spacer(Modifier.height(8.dp))
            Text(email, color = Color.Gray)
            Spacer(Modifier.height(8.dp))
            Surface(color = if(rol=="Admin") Color.Red else Color.Blue, shape = RoundedCornerShape(50)) {
                Text(
                    text = rol.uppercase(),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            Spacer(Modifier.height(24.dp))

            // --- 2. BILGILERI DUZENLE ---
            Text("Kişisel Bilgiler", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = adSoyad,
                onValueChange = { adSoyad = it },
                label = { Text("Ad Soyad") },
                leadingIcon = { Icon(Icons.Default.Person, null) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = birim,
                onValueChange = { birim = it },
                label = { Text("Birim / Bölüm") },
                leadingIcon = { Icon(Icons.Default.Edit, null) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            // --- 3. BILDIRIM AYARLARI ---
            Text("Bildirim Ayarları", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Acil Durum Bildirimleri")
                        Switch(checked = bildirimAcil, onCheckedChange = { bildirimAcil = it })
                    }
                    Divider()
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Ders / Etkinlik Bildirimleri")
                        Switch(checked = bildirimDers, onCheckedChange = { bildirimDers = it })
                    }
                }
            }

            // KAYDET BUTONU
            Button(onClick = { profiliGuncelle() }, modifier = Modifier.fillMaxWidth()) {
                Text("DEĞİŞİKLİKLERİ KAYDET")
            }

            Spacer(Modifier.height(24.dp))

            // --- 4. TAKIP EDILENLER ---
            Text("Takip Ettiğim Bildirimler (${takipEdilenler.size})", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
            if (takipEdilenler.isEmpty()) {
                Text("Henüz takip ettiğiniz bir talep yok.", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.align(Alignment.Start))
            } else {
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(takipEdilenler) { talep ->
                        Card(
                            modifier = Modifier.width(160.dp).clickable { detayaGit(talep.id) },
                            colors = CardDefaults.cardColors(containerColor = if(talep.tur=="Acil") Color(0xFFFFEBEE) else Color.White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(talep.tur, fontSize = 10.sp, color = Color.Gray)
                                Text(talep.baslik, fontWeight = FontWeight.Bold, maxLines = 1)
                                Spacer(Modifier.height(4.dp))
                                Text(talep.durum, color = if(talep.durum=="Çözüldü") Color(0xFF2E7D32) else Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // CIKIS BUTONU (Kirmizi)
            OutlinedButton(
                onClick = { auth.signOut(); cikisYap() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, null)
                Spacer(Modifier.width(8.dp))
                Text("ÇIKIŞ YAP")
            }
        }
    }
}