package com.example.posyanduapp;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class rekam extends AppCompatActivity {

    private LinearLayout container;
    private LinearLayout navJadwal, navProfile, navMedis;
    private DatabaseReference usersRef, rekamMedisRef;
    private String emailLogin;
    private String namaUser;
    private Set<String> previousKeys = new HashSet<>();

    private static final String CHANNEL_ID = "rekam_medis_channel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rekam);

        // Inisialisasi views
        navJadwal = findViewById(R.id.navJadwal);
        navProfile = findViewById(R.id.navProfile);
        navMedis = findViewById(R.id.navMedis);  // Pastikan ini ada di layout XML
        container = findViewById(R.id.container);

        // Ambil email dari intent
        emailLogin = getIntent().getStringExtra("email");
        if (emailLogin == null || emailLogin.isEmpty()) {
            Toast.makeText(this, "Email tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        checkNotificationPermission();
        createNotificationChannel();

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        rekamMedisRef = FirebaseDatabase.getInstance().getReference("rekamMedis");

        // Navigasi tombol jadwal
        navJadwal.setOnClickListener(v -> {
            Intent intent = new Intent(rekam.this, home.class);
            intent.putExtra("email", emailLogin);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();  // Optional: hentikan activity rekam agar tidak menumpuk
        });

        // Navigasi tombol profil
        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(rekam.this, profil.class);
            intent.putExtra("email", emailLogin);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        });

        // Navigasi tombol rekam medis — karena sudah di halaman rekam, kasih toast atau reload
        navMedis.setOnClickListener(v -> {
            Toast.makeText(this, "Anda sudah di halaman Rekam Medis", Toast.LENGTH_SHORT).show();
        });

        // Cari data user berdasar email lalu mulai listen data rekam medis
        usersRef.orderByChild("email").equalTo(emailLogin).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnap : snapshot.getChildren()) {
                        Object namaObj = userSnap.child("nama").getValue();
                        if (namaObj != null) {
                            namaUser = namaObj.toString();
                            listenForRekamMedisUpdates(namaUser);
                            loadRekamMedisByName(namaUser);
                        }
                        break; // ambil hanya satu user
                    }
                } else {
                    Toast.makeText(rekam.this, "User tidak ditemukan", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(rekam.this, "Gagal mengambil data user: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadRekamMedisByName(String namaUser) {
        rekamMedisRef.orderByChild("nama").equalTo(namaUser).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                container.removeAllViews();
                if (snapshot.exists()) {
                    List<DataSnapshot> snapshots = new ArrayList<>();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        snapshots.add(dataSnapshot);
                    }
                    Collections.reverse(snapshots); // Terbaru di atas
                    for (DataSnapshot dataSnapshot : snapshots) {
                        tambahDataKeLayout(dataSnapshot);
                    }
                } else {
                    TextView emptyMessage = new TextView(rekam.this);
                    emptyMessage.setText("Data rekam medis tidak ditemukan untuk nama: " + namaUser);
                    emptyMessage.setTextSize(16f);
                    emptyMessage.setTextColor(Color.RED);
                    container.addView(emptyMessage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(rekam.this, "Gagal mengambil data rekam medis: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void listenForRekamMedisUpdates(String namaUser) {
        rekamMedisRef.orderByChild("nama").equalTo(namaUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                container.removeAllViews();

                List<DataSnapshot> snapshots = new ArrayList<>();
                Set<String> currentKeys = new HashSet<>();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    snapshots.add(dataSnapshot);
                    currentKeys.add(dataSnapshot.getKey());
                }

                Collections.reverse(snapshots);

                for (DataSnapshot dataSnapshot : snapshots) {
                    tambahDataKeLayout(dataSnapshot);
                }

                if (!previousKeys.isEmpty()) {
                    for (String key : currentKeys) {
                        if (!previousKeys.contains(key)) {
                            showNotification(
                                    "Rekam Medis Terbaru",
                                    "Halo " + namaUser + ", ada data rekam medis terbaru."
                            );
                            break;
                        }
                    }
                }

                previousKeys = currentKeys;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(rekam.this, "Gagal memuat data rekam medis: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void tambahDataKeLayout(DataSnapshot dataSnapshot) {
        String tanggal = safeGetString(dataSnapshot, "tanggal");
        String waktu = safeGetString(dataSnapshot, "waktu");
        String tanggalWaktu = tanggal + " / " + waktu;
        String keluhan = safeGetString(dataSnapshot, "keluhan");
        String asamUrat = safeGetString(dataSnapshot, "asamUrat");
        String bb = safeGetString(dataSnapshot, "bb");
        String kolesterol = safeGetString(dataSnapshot, "kolesterol");
        String gulaDarah = safeGetString(dataSnapshot, "gulaDarah");
        String tensi = safeGetString(dataSnapshot, "tensi");
        String resep = safeGetString(dataSnapshot, "resep");

        LinearLayout itemLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.item_rekam_medis, container, false);

        ((TextView) itemLayout.findViewById(R.id.tvTanggalWaktu)).setText("Waktu: " + tanggalWaktu);
        ((TextView) itemLayout.findViewById(R.id.tvKeluhan)).setText("Keluhan: " + keluhan);
        ((TextView) itemLayout.findViewById(R.id.tvAsamUrat)).setText("Asam Urat: " + asamUrat);
        ((TextView) itemLayout.findViewById(R.id.tvBB)).setText("Berat Badan: " + bb);
        ((TextView) itemLayout.findViewById(R.id.tvKolesterol)).setText("Kolesterol: " + kolesterol);
        ((TextView) itemLayout.findViewById(R.id.tvGulaDarah)).setText("Gula Darah: " + gulaDarah);
        ((TextView) itemLayout.findViewById(R.id.tvTensi)).setText("Tensi: " + tensi);
        ((TextView) itemLayout.findViewById(R.id.tvResep)).setText("Resep: " + resep);

        container.addView(itemLayout);
    }

    private String safeGetString(DataSnapshot snapshot, String key) {
        Object val = snapshot.child(key).getValue();
        return val != null ? val.toString() : "-";
    }

    private void showNotification(String title, String message) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return; // Tidak ada izin
            }
        }

        Intent intent = new Intent(this, rekam.class);
        intent.putExtra("email", emailLogin);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                        PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification) // Pastikan icon ini ada
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(1, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Rekam Medis Channel";
            String description = "Notifikasi untuk data rekam medis baru";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if(notificationManager != null){
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Izin notifikasi diberikan", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Izin notifikasi ditolak", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
