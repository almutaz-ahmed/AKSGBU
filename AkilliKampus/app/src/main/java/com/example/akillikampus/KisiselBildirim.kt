package com.example.akillikampus

data class KisiselBildirim(
    val id: String = "",
    val kullaniciId: String = "", // Kime gidecek?
    val mesaj: String = "",
    val tarih: Long = 0,
    val tur: String = "Bilgi" // "Bilgi" veya "Acil"
)