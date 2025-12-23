package com.example.akillikampus

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun KayitEkrani(
    girisEkraninaDon: () -> Unit // Giriş ekranına dönmek için yol
) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    // Form verilerini tutan değişkenler
    var adSoyad by remember { mutableStateOf("") }
    var eposta by remember { mutableStateOf("") }
    var sifre by remember { mutableStateOf("") }
    var birim by remember { mutableStateOf("") }
    var yukleniyorMu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Kayıt Ol", fontSize = 30.sp, style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(32.dp))

        // Kullanıcıdan Bilgi Alma Alanları
        OutlinedTextField(value = adSoyad, onValueChange = { adSoyad = it }, label = { Text("Ad Soyad") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = eposta, onValueChange = { eposta = it }, label = { Text("E-posta") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = sifre, onValueChange = { sifre = it }, label = { Text("Şifre") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = birim, onValueChange = { birim = it }, label = { Text("Birim (Fakülte/Bölüm)") }, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(24.dp))

        if (yukleniyorMu) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    if (eposta.isNotEmpty() && sifre.isNotEmpty() && adSoyad.isNotEmpty()) {
                        yukleniyorMu = true
                        // Firebase'de Kullanıcı Oluştur
                        auth.createUserWithEmailAndPassword(eposta, sifre)
                            .addOnSuccessListener { sonuc ->
                                val uid = sonuc.user?.uid
                                if (uid != null) {
                                    // ÖNEMLİ: Daha önce oluşturduğumuz Kullanici modelini kullanıyoruz
                                    val yeniKullanici = Kullanici(
                                        id = uid,
                                        adSoyad = adSoyad,
                                        email = eposta,
                                        rol = "User", // Varsayılan rol
                                        birim = birim
                                    )

                                    // Veritabanına kaydet
                                    firestore.collection("Kullanicilar").document(uid).set(yeniKullanici)
                                        .addOnSuccessListener {
                                            yukleniyorMu = false
                                            Toast.makeText(context, "Kayıt Başarılı!", Toast.LENGTH_SHORT).show()
                                            girisEkraninaDon() // Başarılı olunca giriş ekranına at
                                        }
                                        .addOnFailureListener {
                                            yukleniyorMu = false
                                            Toast.makeText(context, "Veritabanı Hatası: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                                        }
                                }
                            }
                            .addOnFailureListener {
                                yukleniyorMu = false
                                Toast.makeText(context, "Kayıt Hatası: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        Toast.makeText(context, "Tüm alanları doldurun", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Kayıt Ol")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Giriş Ekranına Dönüş Linki
        Text(
            text = "Zaten hesabın var mı? Giriş Yap",
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable {
                girisEkraninaDon()
            }
        )
    }
}