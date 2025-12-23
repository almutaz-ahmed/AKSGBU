package com.example.akillikampus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { // Uygulamanın genel teması
        MaterialTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                // Hangi ekranın gösterileceğini tutan basit bir değişken
                // Başlangıçta "Giris" ekranı açılacak
                //var suankiEkran by remember { mutableStateOf("Giris") }
                //MAIN ACTIVITY ICINDEKI DEGISKENLER
                var suankiEkran by remember { mutableStateOf("Giris") }

                // HARITA DETAY SAYFASI ICIN GECICI VERILER
                var secilenDetayBaslik by remember { mutableStateOf("") }
                var secilenDetayTur by remember { mutableStateOf("") }
                var secilenDetayZaman by remember { mutableStateOf("") }

                // --- NAVIGASYON BLOGU ---
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
                            talepOlusturSayfasinaGit = { /* Yapilacak */ },
                            talepleriGorSayfasinaGit = { /* Yapilacak */ },
                            haritayaGit = { suankiEkran = "Harita" } // BURAYI GUNCELLEDİK
                        )
                    }
                    "BildirimEkle" -> {
                        BildirimEkleEkrani(
                            geriDon = { suankiEkran = "AnaSayfa" }
                        )
                    }
                    "Harita" -> {
                        HaritaEkrani(
                            geriDon = { suankiEkran = "AnaSayfa" },
                            detayaGit = { baslik, tur, zaman ->
                                // VERILERI KAYDET VE EKRANI DEGISTIR
                                secilenDetayBaslik = baslik
                                secilenDetayTur = tur
                                secilenDetayZaman = zaman
                                suankiEkran = "HaritaDetay"
                            }
                        )
                    }
                    "HaritaDetay" -> {
                        HaritaDetayEkrani(
                            baslik = secilenDetayBaslik,
                            tur = secilenDetayTur,
                            neKadarOnce = secilenDetayZaman,
                            geriDon = { suankiEkran = "Harita" }
                        )
                    }
                }
            }
        }
    }
    }
}


