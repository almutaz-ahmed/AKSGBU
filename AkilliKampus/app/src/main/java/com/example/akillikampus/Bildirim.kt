package com.example.akillikampus

// VERITABANI SABLONUMUZ (DATA MODEL)
data class Bildirim(
    val id: String = "",             // BILDIRIMIN BENZERSIZ NUMARASI (ID)
    val baslik: String = "",         // OLAYIN BASLIGI
    val aciklama: String = "",       // DETAYLI ACIKLAMA
    val tur: String = "Genel",       // TURU
    val konum: String = "",          // OLAYIN YERI
    val tarih: Long = 0,             // OLAYIN OLDUGU ZAMAN
    val durum: String = "Acik",      // DURUMU
    val resimUrl: String = "",       // FOTOGRAF
    val kullaniciId: String = ""     // KIM GONDERDI
)