package com.example.akillikampus

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

// HARITA PIN MODELI (VERI YAPISI)
data class HaritaPini(
    val baslik: String,
    val tur: String,
    val neKadarOnce: String, // NE KADAR ONCE OLUSTURULDU
    val konum: LatLng
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HaritaEkrani(
    geriDon: () -> Unit,
    detayaGit: (String, String, String) -> Unit // BASLIK, TUR, ZAMAN
) {
    // ORNEK VERILER (ILK ETAPTA SABIT VERILER)
    val kampusKonumlari = remember {
        listOf(
            HaritaPini("Buyuk Sinav", "Duyuru", "2 saat once", LatLng(39.93, 32.85)),
            HaritaPini("Bahar Senligi", "Etkinlik", "1 gun once", LatLng(39.94, 32.86)),
            HaritaPini("Kutuphane Dolu", "Bilgi", "15 dk once", LatLng(39.935, 32.855)),
            HaritaPini("Rektorluk", "Bilgi", "3 gun once", LatLng(39.925, 32.845))
        )
    }

    // SECILEN PINI TUTMAK ICIN (KARTI GOSTERMEK ICIN)
    var secilenPin by remember { mutableStateOf<HaritaPini?>(null) }

    // KAMERA POZISYONU AYARLARI (BASLANGIC KONUMU)
    val kameraPozisyonu = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(39.93, 32.85), 13f)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Kampüs Haritası", fontWeight = FontWeight.Bold) },
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
        Box(modifier = Modifier.padding(dolguDegerleri).fillMaxSize()) {

            // 1. HARITA BILESENI
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = kameraPozisyonu,
                onMapClick = { secilenPin = null } // BOSLUGA TIKLAYINCA KARTI KAPAT
            ) {
                kampusKonumlari.forEach { pin ->
                    // TURE GORE RENK AYARLAMASI
                    val pinRengi = when (pin.tur) {
                        "Duyuru" -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                        "Etkinlik" -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                        else -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                    }

                    Marker(
                        state = MarkerState(position = pin.konum),
                        title = pin.baslik,
                        icon = pinRengi,
                        onClick = {
                            secilenPin = pin // TIKLANAN PINI SEC
                            true // KAMERAYI OTOMATIK OYNATMA
                        }
                    )
                }
            }

            // 2. BILGI KARTI (PIN TIKLANINCA ALTTA CIKAR)
            if (secilenPin != null) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(8.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // KAPATMA IKONU VE BASLIK
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = secilenPin!!.baslik,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { secilenPin = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Kapat")
                            }
                        }

                        // TUR VE ZAMAN BILGISI
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val renk = if(secilenPin!!.tur == "Duyuru") Color.Red else Color.Blue
                            Text(text = "Tür: ${secilenPin!!.tur}", color = renk, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = "🕒 ${secilenPin!!.neKadarOnce}", color = Color.Gray)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // DETAYI GOR BUTONU
                        Button(
                            onClick = {
                                // DETAY SAYFASINA GIT (VERILERI TASI)
                                detayaGit(secilenPin!!.baslik, secilenPin!!.tur, secilenPin!!.neKadarOnce)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EA))
                        ) {
                            Text("DETAYI GÖR", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

