package com.example.akillikampus

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TalepOlusturEkrani(
    geriDon: () -> Unit
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var baslik by remember { mutableStateOf("") }
    var aciklama by remember { mutableStateOf("") }
    var tur by remember { mutableStateOf("Genel") }
    var konum by remember { mutableStateOf("") }

    // FOTOGRAF ICIN DEGISKENLER
    var secilenResimUri by remember { mutableStateOf<Uri?>(null) }

    // GALERIYI ACAN KOD
    val galeriBaslatici = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        secilenResimUri = uri
    }

    var menuAcik by remember { mutableStateOf(false) }
    var yukleniyor by remember { mutableStateOf(false) }

    val turler = listOf("Genel", "Acil", "Ders", "Etkinlik", "Arıza", "Şikayet")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Talep Oluştur") },
                navigationIcon = {
                    IconButton(onClick = geriDon) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF6200EA),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TUR SECIMI
            Box {
                OutlinedTextField(
                    value = tur, onValueChange = {}, readOnly = true, label = { Text("Tür (Acil Durum Seçebilirsiniz)") },
                    trailingIcon = { IconButton({ menuAcik = true }) { Icon(Icons.Default.ArrowDropDown, null) } },
                    modifier = Modifier.fillMaxWidth()
                )
                DropdownMenu(expanded = menuAcik, onDismissRequest = { menuAcik = false }) {
                    turler.forEach { secenek ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = secenek,
                                    color = if(secenek == "Acil") Color.Red else Color.Black,
                                    fontWeight = if(secenek == "Acil") FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = { tur = secenek; menuAcik = false }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            OutlinedTextField(baslik, { baslik = it }, label = { Text("Başlık") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(aciklama, { aciklama = it }, label = { Text("Açıklama") }, modifier = Modifier.fillMaxWidth().height(100.dp))
            Spacer(Modifier.height(16.dp))

            // FOTOGRAF SECME ALANI
            if (secilenResimUri != null) {
                // Secilen Resmi Goster
                Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.height(200.dp).fillMaxWidth()) {
                    Image(
                        painter = rememberAsyncImagePainter(secilenResimUri),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                TextButton(onClick = { secilenResimUri = null }) { Text("Fotoğrafı Kaldır", color = Color.Red) }
            } else {
                // Fotograf Ekle Butonu
                OutlinedButton(
                    onClick = { galeriBaslatici.launch("image/*") },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("FOTOĞRAF EKLE (İsteğe Bağlı)")
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { konum = "Konum Alındı" },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = if(konum.isEmpty()) Color.Gray else Color(0xFF2E7D32))
            ) {
                Icon(Icons.Default.LocationOn, null); Spacer(Modifier.width(8.dp))
                Text(if(konum.isEmpty()) "KONUM EKLE" else "KONUM EKLENDİ")
            }

            Spacer(Modifier.height(24.dp))

            if (yukleniyor) CircularProgressIndicator()
            else {
                Button(
                    onClick = {
                        if (baslik.isNotEmpty() && aciklama.isNotEmpty()) {
                            yukleniyor = true
                            val yeniTalep = Talep(
                                id = UUID.randomUUID().toString(),
                                ogrenciEmail = auth.currentUser?.email ?: "Anonim",
                                baslik = baslik, aciklama = aciklama, tur = tur, konum = konum,
                                resimUri = secilenResimUri?.toString() ?: "", // URI'yi String yapip kaydediyoruz
                                tarih = System.currentTimeMillis()
                            )
                            db.collection("Talepler").document(yeniTalep.id).set(yeniTalep)
                                .addOnSuccessListener {
                                    yukleniyor = false
                                    Toast.makeText(context, "Talep İletildi!", Toast.LENGTH_SHORT).show()
                                    geriDon()
                                }
                        } else {
                            Toast.makeText(context, "Başlık ve Açıklama zorunludur.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        // Eger ACIL secildiyse Buton Kirmizi olsun
                        containerColor = if(tur == "Acil") Color.Red else Color(0xFF6200EA)
                    )
                ) {
                    Text(if(tur=="Acil") "ACİL TALEP GÖNDER" else "GÖNDER", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}