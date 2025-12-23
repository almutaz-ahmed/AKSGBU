package com.example.akillikampus

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnaSayfa(
    cikisYap: () -> Unit,
    bildirimEkleSayfasinaGit: () -> Unit,
    talepOlusturSayfasinaGit: () -> Unit,
    talepleriGorSayfasinaGit: () -> Unit,
    haritayaGit: () -> Unit
) {
    // FIREBASE VE SISTEM DEGISKENLERI
    val yetkilendirme = FirebaseAuth.getInstance()
    val veritabani = FirebaseFirestore.getInstance()
    val mevcutKullanici = yetkilendirme.currentUser

    // --- DURUM (STATE) DEGISKENLERI ---
    var bildirimListesi by remember { mutableStateOf<List<Bildirim>>(emptyList()) }
    var aramaMetni by remember { mutableStateOf("") }
    var secilenFiltre by remember { mutableStateOf("Tümü") }
    var yukleniyorMu by remember { mutableStateOf(true) }
    var kullaniciRolu by remember { mutableStateOf("User") }

    // 1. KULLANICI ROLUNU CEKME
    LaunchedEffect(mevcutKullanici) {
        mevcutKullanici?.uid?.let { kimlikNo ->
            veritabani.collection("Kullanicilar").document(kimlikNo).get()
                .addOnSuccessListener { dokuman ->
                    if (dokuman != null) {
                        kullaniciRolu = dokuman.getString("rol") ?: "User"
                    }
                }
        }
    }

    // 2. BILDIRIMLERI CEKME
    LaunchedEffect(Unit) {
        veritabani.collection("Bildirimler")
            .orderBy("tarih", Query.Direction.DESCENDING)
            .addSnapshotListener { deger, hata ->
                if (hata != null) {
                    yukleniyorMu = false
                    return@addSnapshotListener
                }
                if (deger != null) {
                    val liste = deger.toObjects(Bildirim::class.java)
                    bildirimListesi = liste
                    yukleniyorMu = false
                }
            }
    }

    // 3. ARAMA VE FILTRELEME
    val gosterilecekListe = bildirimListesi.filter { bildirim ->
        val aramaSonucu = bildirim.baslik.contains(aramaMetni, ignoreCase = true) ||
                bildirim.aciklama.contains(aramaMetni, ignoreCase = true)
        val kategoriSonucu = if (secilenFiltre == "Tümü") true else bildirim.tur == secilenFiltre
        aramaSonucu && kategoriSonucu
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Akıllı Kampüs", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF6200EA),
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = {
                        yetkilendirme.signOut()
                        cikisYap()
                    }) {
                        // EXIT IKONU ICIN STANDART KULLANIM
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Çıkış", tint = Color.White)
                    }
                }
            )
        }
    ) { dolguDegerleri ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(dolguDegerleri)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- KULLANICI BILGISI ---
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF6200EA), modifier = Modifier.size(40.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = "Merhaba,", fontSize = 14.sp, color = Color.Gray)
                    Text(text = mevcutKullanici?.email ?: "Misafir", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- GOREV BUTONLARI ---
            if (kullaniciRolu == "Admin") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    IslemKarti(renk = Color(0xFFFFD7D7), yazi = "BİLDİRİM\nEKLE", yaziRengi = Color.Red, tiklaninca = bildirimEkleSayfasinaGit)
                    IslemKarti(renk = Color(0xFFFFF8E1), yazi = "TALEPLERİ\nGÖR", yaziRengi = Color(0xFFF57F17), tiklaninca = talepleriGorSayfasinaGit)
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    IslemKarti(renk = Color(0xFFE7FFD7), yazi = "TALEP\nOLUŞTUR", yaziRengi = Color(0xFF2E7D32), tiklaninca = talepOlusturSayfasinaGit)
                    IslemKarti(renk = Color(0xFFE1F5FE), yazi = "TALEPLERİMİ\nGÖR", yaziRengi = Color(0xFF0277BD), tiklaninca = talepleriGorSayfasinaGit)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- HARITA BUTONU (HATANIN DUZELDIGI YER) ---
            Button(
                onClick = { haritayaGit() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                // BURADA 'color' DEGIL 'tint' KULLANILMALIYDI, DUZELTTIK:
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("KAMPÜS HARİTASI", color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- AYIRICI CIZGI ---
            Divider(color = Color.LightGray, thickness = 1.dp)

            Spacer(modifier = Modifier.height(16.dp))

            // --- ARAMA KUTUSU ---
            OutlinedTextField(
                value = aramaMetni,
                onValueChange = { aramaMetni = it },
                label = { Text("Bildirim Ara...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // --- FILTRE BUTONLARI ---
            ScrollableTabRow(
                selectedTabIndex = 0,
                edgePadding = 0.dp,
                containerColor = Color.Transparent,
                divider = {},
                indicator = {}
            ) {
                listOf("Tümü", "Acil", "Ders", "Etkinlik", "Genel").forEach { filtre ->
                    FilterChip(
                        selected = (secilenFiltre == filtre),
                        onClick = { secilenFiltre = filtre },
                        label = { Text(filtre) },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- LISTE ---
            if (yukleniyorMu) {
                CircularProgressIndicator()
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(gosterilecekListe) { bildirim ->
                        BildirimSatiri(bildirim)
                    }
                }
            }
        }
    }
}

// --- YARDIMCI BILESENLER ---

@Composable
fun IslemKarti(renk: Color, yazi: String, yaziRengi: Color, tiklaninca: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = renk),
        modifier = Modifier
            .height(80.dp)
            .clickable { tiklaninca() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = yazi, color = yaziRengi, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun BildirimSatiri(bildirim: Bildirim) {
    val tarihFormati = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
    val tarihMetni = try { tarihFormati.format(Date(bildirim.tarih)) } catch (e: Exception) { "" }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val ikon = when (bildirim.tur) {
                "Acil" -> Icons.Default.Warning
                "Ders" -> Icons.Default.Info
                else -> Icons.Default.Notifications
            }
            val renk = if (bildirim.tur == "Acil") Color.Red else Color.Gray

            Icon(imageVector = ikon, contentDescription = null, tint = renk, modifier = Modifier.size(32.dp))

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = bildirim.baslik, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = bildirim.aciklama, maxLines = 1, color = Color.Gray, fontSize = 14.sp)
                Text(text = tarihMetni, fontSize = 12.sp, color = Color.LightGray)
            }

            Surface(
                color = if (bildirim.durum == "Acik") Color(0xFFFFF3E0) else Color(0xFFE8F5E9),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = bildirim.durum,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    color = if (bildirim.durum == "Acik") Color(0xFFE65100) else Color(0xFF2E7D32)
                )
            }
        }
    }
}