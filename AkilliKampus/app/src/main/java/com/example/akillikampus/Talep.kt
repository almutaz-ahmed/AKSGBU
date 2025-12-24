package com.example.akillikampus

data class Talep(
    val id: String = "",
    val ogrenciEmail: String = "",
    val baslik: String = "",
    val aciklama: String = "",
    val durum: String = "Bekliyor",
    val tur: String = "Genel",
    val konum: String = "",
    // YENİ EKLENEN ALAN: FOTOĞRAF YOLU
    val resimUri: String = "",
    val tarih: Long = 0,
    val takipciler: List<String> = emptyList()
)