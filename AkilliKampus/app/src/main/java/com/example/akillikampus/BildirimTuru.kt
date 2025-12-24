package com.example.akillikampus

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class BildirimTuru(
    val ad: String,
    val renk: Color,
    val ikon: ImageVector
) {
    GENEL("Genel", Color(0xFF6200EA), Icons.Default.Notifications),
    ACIL("Acil", Color(0xFFD32F2F), Icons.Default.Warning),
    DERS("Ders", Color(0xFF1976D2), Icons.Default.Info),
    ETKINLIK("Etkinlik", Color(0xFF388E3C), Icons.Default.DateRange),
    ARIZA("Arıza", Color(0xFFF57C00), Icons.Default.Build),
    SIKAYET("Şikayet", Color(0xFF5D4037), Icons.Default.Warning), // Warning kullanıyoruz
    GUVENLIK("Güvenlik", Color(0xFF000000), Icons.Default.Lock);

    companion object {
        fun getir(ad: String): BildirimTuru {
            // Eger veritabanindaki isim bulunamazsa varsayilan olarak GENEL dondur
            return entries.find { it.ad == ad } ?: GENEL
        }
    }
}