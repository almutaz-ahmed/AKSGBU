package com.example.akillikampus

// kullanici veritabaninda kaydedilecek veri şablounu
class Kullanici (
    val id: String = "",       // firebase Authentication ID'si (Kimlik No)
    val adSoyad: String = "",    // kullanıcının Adı ve Soyadı
    val email: String = "",  // kullanıcının E-posta adresi
    val rol: String = "User",    // Rol: varsayılan "User" (Kullanıcı), yönetici ise "Admin"
    val birim: String = ""      // Kullanıcının okuduğu bölüm veya fakülte
)