package com.example.akillikampus

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.compose.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TalepDetayEkrani(
    talepId: String,
    geriDon: () -> Unit
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    // --- DEGISKENLER ---
    var baslik by remember { mutableStateOf("Yükleniyor...") }
    var aciklama by remember { mutableStateOf("") }
    var durum by remember { mutableStateOf("Açık") }
    var tur by remember { mutableStateOf("") }
    var tarih by remember { mutableStateOf(0L) }
    var resimUri by remember { mutableStateOf("") }
    // YENİ EKLENDİ: GÖNDEREN KİŞİNİN MAİLİ
    var gonderenEmail by remember { mutableStateOf("") }

    var takipciler by remember { mutableStateOf(listOf<String>()) }
    var gercekRol by remember { mutableStateOf("User") }

    // HARITA
    val mapPosition = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(39.93, 32.85), 15f)
    }

    // 1. ROLÜ ÇEK
    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { uid ->
            db.collection("Kullanicilar").document(uid).get().addOnSuccessListener { doc ->
                if (doc != null) gercekRol = doc.getString("rol") ?: "User"
            }
        }
    }

    // 2. TALEP VERİSİNİ ÇEK
    LaunchedEffect(talepId) {
        if (talepId.isNotEmpty()) {
            db.collection("Talepler").document(talepId)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && snapshot.exists()) {
                        baslik = snapshot.getString("baslik") ?: ""
                        aciklama = snapshot.getString("aciklama") ?: ""
                        durum = snapshot.getString("durum") ?: "Açık"
                        tur = snapshot.getString("tur") ?: "Genel"
                        tarih = snapshot.getLong("tarih") ?: 0L
                        resimUri = snapshot.getString("resimUri") ?: ""
                        // GÖNDEREN BİLGİSİNİ ALIYORUZ
                        gonderenEmail = snapshot.getString("ogrenciEmail") ?: "Anonim"

                        val followersData = snapshot.get("takipciler")
                        if (followersData is List<*>) {
                            takipciler = followersData.filterIsInstance<String>()
                        }
                    }
                }
        }
    }

    val takipEdiyor = takipciler.contains(currentUser?.uid)
    // TARIHI DETAYLI FORMATLA (Saat dahil)
    val tarihFormati = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("tr"))
    val tarihStr = if (tarih > 0) tarihFormati.format(Date(tarih)) else ""

    fun durumGuncelle(yeniDurum: String) {
        db.collection("Talepler").document(talepId).update("durum", yeniDurum)
        Toast.makeText(context, "Durum: $yeniDurum yapıldı", Toast.LENGTH_SHORT).show()
    }

    fun takipEtBirak() {
        if (currentUser == null) return
        val ref = db.collection("Talepler").document(talepId)
        if (takipEdiyor) {
            ref.update("takipciler", FieldValue.arrayRemove(currentUser.uid))
            Toast.makeText(context, "Takipten çıkıldı", Toast.LENGTH_SHORT).show()
        } else {
            ref.update("takipciler", FieldValue.arrayUnion(currentUser.uid))
            Toast.makeText(context, "Takip ediliyor", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Talep Detayı") },
                navigationIcon = {
                    IconButton(onClick = geriDon) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF6200EA), titleContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // --- 1. GÖNDEREN BİLGİSİ KARTI (YENİ EKLENDİ) ---
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // PROFIL IKONU
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFE0E0E0),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                        }
                    }

                    Spacer(Modifier.width(12.dp))

                    // ISIM VE TARIH
                    Column {
                        Text("Gönderen:", fontSize = 10.sp, color = Color.Gray)
                        Text(gonderenEmail, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DateRange, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                            Spacer(Modifier.width(4.dp))
                            Text(tarihStr, fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // --- 2. BAŞLIK VE TÜR ---
            Text(text = tur, color = if(tur=="Acil") Color.Red else Color(0xFF6200EA), fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(text = baslik, fontSize = 24.sp, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(16.dp))

            // --- 3. DURUM KUTUSU ---
            val (kutuRengi, yaziRengi) = when (durum) {
                "Çözüldü" -> Color(0xFFE7FFD7) to Color(0xFF2E7D32)
                "İnceleniyor" -> Color(0xFFFFF3E0) to Color(0xFFFF9800)
                else -> Color(0xFFFFEBEE) to Color.Red
            }
            Card(colors = CardDefaults.cardColors(containerColor = kutuRengi), modifier = Modifier.fillMaxWidth()) {
                Box(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "DURUM:", fontWeight = FontWeight.Bold, color = Color.Black)
                        Spacer(Modifier.width(8.dp))
                        Text(text = durum.uppercase(), fontWeight = FontWeight.Bold, color = yaziRengi)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // --- 4. AÇIKLAMA ---
            Text("Açıklama:", fontWeight = FontWeight.Bold)
            Text(aciklama, lineHeight = 20.sp)

            Spacer(Modifier.height(16.dp))

            // --- 5. FOTOGRAF ---
            if (resimUri.isNotEmpty()) {
                Text("Eklenen Fotoğraf:", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Card(modifier = Modifier.fillMaxWidth().height(250.dp), shape = RoundedCornerShape(12.dp)) {
                    Image(
                        painter = rememberAsyncImagePainter(Uri.parse(resimUri)),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            // --- 6. HARITA ---
            Text("Konum:", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Card(modifier = Modifier.fillMaxWidth().height(200.dp), shape = RoundedCornerShape(12.dp)) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = mapPosition,
                    uiSettings = MapUiSettings(zoomControlsEnabled = false)
                ) {
                    Marker(state = MarkerState(position = LatLng(39.93, 32.85)))
                }
            }

            Spacer(Modifier.height(32.dp))

            // --- 7. ADMIN BUTONLARI ---
            if (gercekRol == "Admin") {
                Text("Yönetici İşlemleri:", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Button(onClick = { durumGuncelle("Açık") }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color.Red), shape = RoundedCornerShape(8.dp)) { Text("Açık", fontSize = 12.sp) }
                    Button(onClick = { durumGuncelle("İnceleniyor") }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)), shape = RoundedCornerShape(8.dp)) { Text("İnceleniyor", fontSize = 10.sp, maxLines = 1) }
                    Button(onClick = { durumGuncelle("Çözüldü") }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)), shape = RoundedCornerShape(8.dp)) { Text("Çözüldü", fontSize = 12.sp) }
                }
            } else {
                Button(
                    onClick = { takipEtBirak() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = if(takipEdiyor) Color.Gray else Color(0xFF6200EA))
                ) {
                    Icon(if(takipEdiyor) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if(takipEdiyor) "TAKİBİ BIRAK" else "BU TALEBİ TAKİP ET")
                }
            }
        }
    }
}