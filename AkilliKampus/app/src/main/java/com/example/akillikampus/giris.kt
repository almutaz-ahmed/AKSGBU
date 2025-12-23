package com.example.akillikampus

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GirisEkrani(
    kayitEkraninaGit: () -> Unit, // Kayıt ekranına geçiş fonksiyonu
    girisBasarili: () -> Unit     // Giriş başarılı olunca çalışacak fonksiyon
) {
    // Firebase ve Android araçlarını tanımlıyoruz
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    // Ekranda değişen verileri (State) tutuyoruz
    var eposta by remember { mutableStateOf("") }
    var sifre by remember { mutableStateOf("") }
    var yukleniyorMu by remember { mutableStateOf(false) }

    // Şifre Sıfırlama Penceresi Açık mı?
    var sifreSifirlamaPenceresiGoster by remember { mutableStateOf(false) }
    var sifirlamaEposta by remember { mutableStateOf("") }

    // Arka plan için Renk Geçişi (Gradient) ayarı
    val renkGecisi = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF6200EA), // Üst taraf Mor
            Color(0xFFC51162)  // Alt taraf Pembe
        )
    )

    Box( // Ana kutu (Tüm ekranı kaplar)
        modifier = Modifier
            .fillMaxSize()
            .background(renkGecisi), // Arka planı boya
        contentAlignment = Alignment.Center
    ) {
        Column( // Elemanları alt alta dizeceğimiz sütun
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 1. Üstteki Büyük İkon
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Giriş İkonu",
                tint = Color.White,
                modifier = Modifier.size(100.dp)
            )

            Spacer(modifier = Modifier.height(16.dp)) // Boşluk

            // 2. Hoş Geldiniz Yazısı
            Text(
                text = "Hoş Geldiniz",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 3. E-posta Giriş Kutusu
            OutlinedTextField(
                value = eposta,
                onValueChange = { eposta = it },
                label = { Text("E-posta", color = Color.White.copy(alpha = 0.8f)) },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color.White) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.White,
                    unfocusedIndicatorColor = Color.White.copy(alpha = 0.5f),
                    cursorColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Şifre Giriş Kutusu
            OutlinedTextField(
                value = sifre,
                onValueChange = { sifre = it },
                label = { Text("Şifre", color = Color.White.copy(alpha = 0.8f)) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White) },
                visualTransformation = PasswordVisualTransformation(), // Şifreyi gizle (****)
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.White,
                    unfocusedIndicatorColor = Color.White.copy(alpha = 0.5f),
                    cursorColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true
            )

            // --- ŞİFREMİ UNUTTUM YAZISI (YENİ EKLENDİ) ---
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Şifremi Unuttum",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.End) // Sağa yasla
                    .clickable {
                        sifreSifirlamaPenceresiGoster = true // Pencereyi aç
                    }
                    .padding(4.dp)
            )
            // ---------------------------------------------

            Spacer(modifier = Modifier.height(24.dp))

            // 5. Giriş Butonu veya Yükleniyor Simgesi
            if (yukleniyorMu) {
                CircularProgressIndicator(color = Color.White) // Dönme efekti
            } else {
                Button(
                    onClick = { // Alanlar dolu mu kontrol et
                        if (eposta.isNotEmpty() && sifre.isNotEmpty()) {
                            yukleniyorMu = true
                            // Firebase ile Giriş Yap
                            auth.signInWithEmailAndPassword(eposta, sifre).addOnSuccessListener { sonuc ->
                                    val uid = sonuc.user?.uid
                                    if (uid != null) { // Giriş başarılı, şimdi kullanıcının bilgilerini veritabanından çek
                                        firestore.collection("Kullanicilar").document(uid).get()
                                            .addOnSuccessListener { dokuman ->
                                                yukleniyorMu = false
                                                if (dokuman.exists()) {
                                                    val rol = dokuman.getString("rol") ?: "User"
                                                    val ad = dokuman.getString("adSoyad") ?: "Kullanıcı"
                                                    Toast.makeText(context, "Hoşgeldin $ad ($rol)", Toast.LENGTH_SHORT).show()
                                                    girisBasarili() // Ana sayfaya yönlendir
                                                }
                                            }
                                            .addOnFailureListener {
                                                yukleniyorMu = false
                                                Toast.makeText(context, "Bilgiler alınamadı", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                }
                                .addOnFailureListener {
                                    yukleniyorMu = false
                                    Toast.makeText(context, "Giriş Başarısız: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                                }
                        } else {
                            Toast.makeText(context, "Lütfen alanları doldurun", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text(text = "Giriş Yap", color = Color(0xFF6200EA), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 6. Kayıt Ol Linki
            Text(
                text = "Hesabın yok mu? Kayıt Ol",
                color = Color.White,
                modifier = Modifier.clickable {
                    kayitEkraninaGit() // Kayıt ekranına geçiş yap
                }
            )
        }
    }

    // --- ŞİFRE SIFIRLAMA PENCERESİ (POPUP) ---
    if (sifreSifirlamaPenceresiGoster) {
        AlertDialog(onDismissRequest = { sifreSifirlamaPenceresiGoster = false }, // Dışarı tıklanınca kapat
            title = { Text(text = "Şifre Sıfırlama") },
            text = { Column {
                    Text("E-posta adresinizi girin, size sıfırlama bağlantısı gönderelim.")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = sifirlamaEposta,
                        onValueChange = { sifirlamaEposta = it },
                        label = { Text("E-posta") },
                        singleLine = true
                    )
                }
            },
            confirmButton = { Button(
                    onClick = {
                        if (sifirlamaEposta.isNotEmpty()) {
                            // Firebase Şifre Sıfırlama Fonksiyonu
                            auth.sendPasswordResetEmail(sifirlamaEposta)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Bağlantı gönderildi! E-postanızı kontrol edin.", Toast.LENGTH_LONG).show()
                                    sifreSifirlamaPenceresiGoster = false // Pencereyi kapat
                                    sifirlamaEposta = "" // Kutuyu temizle
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Hata: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                                }
                        } else {
                            Toast.makeText(context, "Lütfen e-posta yazın.", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Gönder")
                }
            },
            dismissButton = { TextButton(onClick = { sifreSifirlamaPenceresiGoster = false }) {
                    Text("İptal")
                }
            }
        )
    }
}