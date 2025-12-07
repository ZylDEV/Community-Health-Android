package com.example.posyanduapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class login extends AppCompatActivity {
    private EditText inputEmail, inputPassword;
    private Button buttonLogin;
    private DatabaseReference usersRef;

    private static final String PREFS_NAME = "loginPrefs";
    private static final String KEY_LOGGED_IN_EMAIL = "loggedInEmail";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Cek SharedPreferences apakah sudah login
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedEmail = prefs.getString(KEY_LOGGED_IN_EMAIL, null);

        if (savedEmail != null) {
            // Sudah login, langsung ke home
            Intent intent = new Intent(login.this, home.class);
            intent.putExtra("email", savedEmail);
            startActivity(intent);
            finish();
            return; // jangan lanjut ke setContentView
        }

        setContentView(R.layout.activity_login);

        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        buttonLogin = findViewById(R.id.buttonLogin);

        usersRef = FirebaseDatabase.getInstance().getReference("users");

        buttonLogin.setOnClickListener(v -> performLogin());
    }

    private void performLogin() {
        String email = inputEmail.getText().toString().trim().toLowerCase();
        String password = inputPassword.getText().toString().trim();

        if (email.isEmpty()) {
            inputEmail.setError("Email tidak boleh kosong");
            inputEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            inputPassword.setError("Password tidak boleh kosong");
            inputPassword.requestFocus();
            return;
        }

        usersRef.orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.d("LoginDebug", "Data snapshot exists: " + snapshot.exists());
                        if (snapshot.exists()) {
                            boolean isValidUser = false;
                            for (DataSnapshot userSnap : snapshot.getChildren()) {
                                String dbEmail = userSnap.child("email").getValue(String.class);
                                String dbPassword = userSnap.child("password").getValue(String.class);
                                Log.d("LoginDebug", "DB Email: " + dbEmail + ", DB Password: " + dbPassword);

                                if (dbPassword != null && dbPassword.equals(password)) {
                                    isValidUser = true;

                                    Toast.makeText(login.this, "Login berhasil!", Toast.LENGTH_SHORT).show();

                                    // Simpan email ke SharedPreferences
                                    SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.putString(KEY_LOGGED_IN_EMAIL, email);
                                    editor.apply();

                                    Intent intent = new Intent(login.this, home.class);
                                    intent.putExtra("email", email);
                                    startActivity(intent);
                                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                    finish();

                                    break;
                                }
                            }

                            if (!isValidUser) {
                                Toast.makeText(login.this, "Password salah", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(login.this, "Email tidak ditemukan", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(login.this, "Gagal koneksi database: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
