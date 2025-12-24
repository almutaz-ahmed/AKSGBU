package com.example.akillikampus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                // --- 1. DEGISKENLER ---
                var suankiEkran by remember { mutableStateOf("Giris") }

                // Rol Bilgisi (Veritabanindan Cekilecek)
                var kullaniciRolu by remember { mutableStateOf("User") }

                // Veri Taşıma
                var secilenTalepId by remember { mutableStateOf("") }
                var secilenBildirimId by remember { mutableStateOf("") }

                // Harita Detay
                var haritaBaslik by remember { mutableStateOf("") }
                var haritaTur by remember { mutableStateOf("") }
                var haritaZaman by remember { mutableStateOf("") }

                val auth = FirebaseAuth.getInstance()
                val db = FirebaseFirestore.getInstance()

                // --- 2. ROLU CEKME ISLEMI ---
                // Uygulama acilinca veya ekran degisince rolu gunceller
                LaunchedEffect(suankiEkran) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        db.collection("Kullanicilar").document(uid).get()
                            .addOnSuccessListener { belge ->
                                if (belge != null && belge.exists()) {
                                    kullaniciRolu = belge.getString("rol") ?: "User"
                                }
                            }
                    }
                }

                // --- 3. EKRAN YONETIMI ---
                when (suankiEkran) {
                    "Giris" -> {
                        GirisEkrani(
                            kayitEkraninaGit = { suankiEkran = "Kayit" },
                            girisBasarili = { suankiEkran = "AnaSayfa" }
                        )
                    }
                    "Kayit" -> {
                        KayitEkrani(
                            girisEkraninaDon = { suankiEkran = "Giris" }
                        )
                    }
                    "AnaSayfa" -> {
                        AnaSayfa(
                            cikisYap = { suankiEkran = "Giris" },
                            bildirimEkleSayfasinaGit = { suankiEkran = "BildirimEkle" },
                            talepOlusturSayfasinaGit = { suankiEkran = "TalepOlustur" },
                            talepleriGorSayfasinaGit = { suankiEkran = "TalepListesi" },
                            haritayaGit = { suankiEkran = "Harita" },
                            bildirimDetayaGit = { id ->
                                secilenBildirimId = id
                                suankiEkran = "BildirimDetay"
                            },
                            // YENİ BAĞLANTIYI BURAYA EKLE:
                            profilSayfasinaGit = { suankiEkran = "Profil" }
                        )
                    }
                    "BildirimEkle" -> {
                        BildirimEkleEkrani(
                            geriDon = { suankiEkran = "AnaSayfa" }
                        )
                    }
                    "TalepOlustur" -> {
                        TalepOlusturEkrani(
                            geriDon = { suankiEkran = "AnaSayfa" }
                        )
                    }
                    "TalepListesi" -> {
                        // --- DUZELTILEN KISIM BURASI ---
                        TalepListesiEkrani(
                            kullaniciRolu = kullaniciRolu, // ARTIK ROLU GONDERIYORUZ!
                            geriDon = { suankiEkran = "AnaSayfa" },
                            detayaGit = { id ->
                                secilenTalepId = id
                                suankiEkran = "TalepDetay"
                            }
                        )
                    }
                    "TalepDetay" -> {
                        TalepDetayEkrani(
                            talepId = secilenTalepId,
                            geriDon = { suankiEkran = "TalepListesi" }
                        )
                    }
                    "BildirimDetay" -> {
                        BildirimDetayEkrani(
                            bildirimId = secilenBildirimId,
                            geriDon = { suankiEkran = "AnaSayfa" }
                        )
                    }
                    "Harita" -> {
                        HaritaEkrani(
                            geriDon = { suankiEkran = "AnaSayfa" },
                            detayaGit = { b, t, z ->
                                haritaBaslik = b; haritaTur = t; haritaZaman = z
                                suankiEkran = "HaritaDetay"
                            }
                        )
                    }
                    "HaritaDetay" -> {
                        HaritaDetayEkrani(
                            baslik = haritaBaslik,
                            tur = haritaTur,
                            neKadarOnce = haritaZaman,
                            geriDon = { suankiEkran = "Harita" }
                        )
                    }
                    "Profil" -> {
                        ProfilEkrani(
                            geriDon = { suankiEkran = "AnaSayfa" },
                            cikisYap = { suankiEkran = "Giris" },
                            detayaGit = { id ->
                                secilenTalepId = id
                                suankiEkran = "TalepDetay"
                            }
                        )
                    }
                }
            }
        }
    }
}