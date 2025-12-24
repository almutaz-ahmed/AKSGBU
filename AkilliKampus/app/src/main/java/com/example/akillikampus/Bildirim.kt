package com.example.akillikampus

data class Bildirim(
    val id: String = "",
    val baslik: String = "",
    val aciklama: String = "",
    val tur: String = "Genel", // "Acil", "Ders", "Etkinlik" vb.
    val konum: String = "Kampüs Geneli",
    val tarih: Long = 0,
    val durum: String = "Yayında"
)