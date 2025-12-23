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
                var suankiEkran by remember { mutableStateOf("Giris") }

                // Ekranlar arası geçiş mantığı
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
                        // Şimdilik buraya geçici bir yazı koyuyoruz
                        // Bir sonraki adımda buraya gerçek Ana Sayfayı yapacağız
                        Text(text = "Giriş Başarılı! Ana Sayfadasınız.")
                    }
                }
            }
        }
    }
    }
}


