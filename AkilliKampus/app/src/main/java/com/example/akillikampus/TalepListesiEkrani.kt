package com.example.akillikampus

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
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
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TalepListesiEkrani(
    kullaniciRolu: String,
    geriDon: () -> Unit,
    detayaGit: (String) -> Unit
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUserEmail = auth.currentUser?.email // Şu anki kullanıcının emaili

    var tumTalepler by remember { mutableStateOf<List<Talep>>(emptyList()) }
    var gosterilenListe by remember { mutableStateOf<List<Talep>>(emptyList()) }

    val filtreler = listOf("Tümü", "Açık", "İnceleniyor", "Çözüldü", "Acil")
    var secilenFiltre by remember { mutableStateOf("Tümü") }

    // VERI CEKME
    LaunchedEffect(Unit) {
        db.collection("Talepler").addSnapshotListener { value, _ ->
            if (value != null) {
                tumTalepler = value.toObjects(Talep::class.java)
            }
        }
    }

    // --- KRİTİK BÖLÜM: FİLTRELEME MANTIKLARI ---
    LaunchedEffect(tumTalepler, secilenFiltre, kullaniciRolu) {
        var geciciListe = tumTalepler

        // 1. ADIM: GİZLİLİK FİLTRESİ (KİM NEYİ GÖRECEK?)
        if (kullaniciRolu != "Admin") {
            // Eğer kullanıcı Admin DEĞİLSE, sadece kendi emailine sahip talepleri görsün
            geciciListe = geciciListe.filter { it.ogrenciEmail == currentUserEmail }
        }
        // Not: Admin ise bu if bloğuna girmez, yani hepsini görür.

        // 2. ADIM: KATEGORİ/DURUM FİLTRESİ
        if (secilenFiltre != "Tümü") {
            geciciListe = if (secilenFiltre == "Acil") {
                geciciListe.filter { it.tur == "Acil" }
            } else {
                geciciListe.filter { it.durum == secilenFiltre }
            }
        }

        // 3. ADIM: SIRALAMA (Aciller en üste, sonra tarih)
        gosterilenListe = geciciListe.sortedWith(
            compareBy<Talep> { it.tur != "Acil" }.thenByDescending { it.tarih }
        )
    }

    // SİLME FONKSİYONU
    fun talepSil(id: String) {
        db.collection("Talepler").document(id).delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Kayıt silindi.", Toast.LENGTH_SHORT).show()
            }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if(kullaniciRolu=="Admin") "Yönetim Paneli" else "Taleplerim") },
                navigationIcon = {
                    IconButton(onClick = geriDon) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF6200EA), titleContentColor = Color.White)
            )
        }
    ) { p ->
        Column(modifier = Modifier.padding(p)) {

            // FILTRE BUTONLARI
            LazyRow(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filtreler) { filtre ->
                    FilterChip(
                        selected = (secilenFiltre == filtre),
                        onClick = { secilenFiltre = filtre },
                        label = { Text(filtre) },
                        leadingIcon = if (secilenFiltre == filtre) { { Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp)) } } else null
                    )
                }
            }
            Divider()

            // LISTE
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (gosterilenListe.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                            Text(
                                if(kullaniciRolu=="Admin") "Hiç talep yok." else "Henüz bir talep oluşturmadınız.",
                                color = Color.Gray
                            )
                        }
                    }
                }

                items(gosterilenListe) { talep ->
                    val isAcil = talep.tur == "Acil"
                    val kartArkaPlan = if(isAcil) Color(0xFFFFEBEE) else Color.White
                    val kenarRengi = if(isAcil) Color.Red else Color.Transparent

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { detayaGit(talep.id) },
                        colors = CardDefaults.cardColors(containerColor = kartArkaPlan),
                        border = androidx.compose.foundation.BorderStroke(1.dp, kenarRengi),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if(isAcil) Icons.Default.Warning else Icons.Default.Info,
                                contentDescription = null,
                                tint = if(isAcil) Color.Red else Color(0xFF6200EA),
                                modifier = Modifier.size(32.dp)
                            )

                            Spacer(Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(if(isAcil) "⚠️ ${talep.baslik}" else talep.baslik, fontWeight = FontWeight.Bold, color = if(isAcil) Color.Red else Color.Black)
                                Text("Tür: ${talep.tur}", fontSize = 12.sp, color = Color.Gray)
                                Text(talep.durum.uppercase(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if(talep.durum=="Çözüldü") Color(0xFF2E7D32) else Color.Red)
                            }

                            // SİLME BUTONU (SADECE ADMIN)
                            if (kullaniciRolu == "Admin") {
                                IconButton(onClick = { talepSil(talep.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Sil", tint = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}