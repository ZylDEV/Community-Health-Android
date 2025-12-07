package com.example.posyanduapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class profil extends AppCompatActivity {

    private static final String TAG = "ProfilDebug";

    private EditText etNama, etUsia, etEmail, etPassword, etTelepon, etNIK, etNoBPJS, etTanggalLahir, etAlamat;
    private RadioButton rbKelamin;
    private ImageView btnLogout;
    private LinearLayout navJadwal, navMedis;

    private String emailLogin;

    private static final String PREFS_NAME = "loginPrefs";
    private static final String KEY_LOGGED_IN_EMAIL = "loggedInEmail";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);

        // Inisialisasi View
        etNama = findViewById(R.id.etNama);
        etUsia = findViewById(R.id.etUsia);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etTelepon = findViewById(R.id.etTelepon);
        etNIK = findViewById(R.id.etNIK);
        etNoBPJS = findViewById(R.id.etNoBPJS);
        etTanggalLahir = findViewById(R.id.etTanggalLahir);
        etAlamat = findViewById(R.id.etAlamat);
        rbKelamin = findViewById(R.id.rbKelamin);

        btnLogout = findViewById(R.id.btnLogout);
        navMedis = findViewById(R.id.navMedis);
        navJadwal = findViewById(R.id.navJadwal);

        // Ambil email dari intent atau shared preferences
        emailLogin = getIntent().getStringExtra("email");
        if (emailLogin == null || emailLogin.isEmpty()) {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            emailLogin = prefs.getString(KEY_LOGGED_IN_EMAIL, null);
        }

        if (emailLogin == null || emailLogin.isEmpty()) {
            Toast.makeText(this, "Email tidak ditemukan, silakan login ulang.", Toast.LENGTH_SHORT).show();
            redirectToLogin();
            return;
        }

        // Tampilkan password sebagai teks
        etPassword.setInputType(InputType.TYPE_CLASS_TEXT);
        // Bikin semua readonly
        setReadOnly(etNama);
        setReadOnly(etUsia);
        setReadOnly(etEmail);
        setReadOnly(etPassword);
        setReadOnly(etTelepon);
        setReadOnly(etNIK);
        setReadOnly(etNoBPJS);
        setReadOnly(etTanggalLahir);
        setReadOnly(etAlamat);
        rbKelamin.setEnabled(false);

        // Navigasi
        navMedis.setOnClickListener(v -> {
            Intent intent = new Intent(profil.this, rekam.class);
            intent.putExtra("email", emailLogin);
            startActivity(intent);
        });

        navJadwal.setOnClickListener(v -> {
            Intent intent = new Intent(profil.this, home.class);
            intent.putExtra("email", emailLogin);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            sharedPreferences.edit().clear().apply();
            Toast.makeText(profil.this, "Logout berhasil", Toast.LENGTH_SHORT).show();
            redirectToLogin();
        });

        // Ambil data user
        loadUserByEmail(emailLogin.trim().toLowerCase());
    }

    private void loadUserByEmail(final String emailInput) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        usersRef.orderByChild("email").equalTo(emailInput).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String nama = userSnapshot.child("nama").getValue(String.class);
                        String usia = userSnapshot.child("usia").getValue(String.class);
                        String password = userSnapshot.child("password").getValue(String.class);
                        String telepon = userSnapshot.child("telepon").getValue(String.class);
                        String nik = userSnapshot.child("nik").getValue(String.class);
                        String bpjs = userSnapshot.child("bpjs").getValue(String.class);
                        String tanggalLahir = userSnapshot.child("tanggalLahir").getValue(String.class);
                        String alamat = userSnapshot.child("alamat").getValue(String.class);
                        String jk = userSnapshot.child("jk").getValue(String.class);

                        etNama.setText(nama != null ? nama : "");
                        etUsia.setText(usia != null ? usia : "");
                        etEmail.setText(emailInput);
                        etPassword.setText(password != null ? password : "");
                        etTelepon.setText(nik != null ? telepon : "");
                        etNIK.setText(nik != null ? nik : "");
                        etNoBPJS.setText(bpjs != null ? bpjs : "");
                        etTanggalLahir.setText(tanggalLahir != null ? tanggalLahir : "");
                        etAlamat.setText(alamat != null ? alamat : "");


                        // Isi jenis kelamin ke satu RadioButton
                        if (jk != null) {
                            if (jk.equalsIgnoreCase("L")) {
                                rbKelamin.setText("Laki-laki");
                            } else if (jk.equalsIgnoreCase("P")) {
                                rbKelamin.setText("Perempuan");
                            } else {
                                rbKelamin.setText("Tidak diketahui");
                            }
                        }

                        break;
                    }
                } else {
                    Toast.makeText(profil.this, "Email tidak ditemukan.", Toast.LENGTH_SHORT).show();
                    redirectToLogin();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(profil.this, "Gagal mengambil data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                redirectToLogin();
            }
        });
    }

    private void setReadOnly(EditText editText) {
        editText.setFocusable(false);
        editText.setClickable(false);
        editText.setCursorVisible(false);
        editText.setKeyListener(null);
    }

    private void redirectToLogin() {
        Intent intent = new Intent(profil.this, login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
