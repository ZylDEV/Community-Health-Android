package com.example.posyanduapp;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class home extends AppCompatActivity {

    private LinearLayout container;
    private LinearLayout navMedis, navProfile;
    private DatabaseReference jadwalRef;
    private String emailLogin;

    private static final String PREFS_NAME = "loginPrefs";
    private static final String KEY_LOGGED_IN_EMAIL = "loggedInEmail";
    private static final String CHANNEL_ID = "jadwal_channel";

    private long lastJadwalCount = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home); // Pastikan ini adalah layout utama kamu

        navMedis = findViewById(R.id.navMedis);
        navProfile = findViewById(R.id.navProfile);
        container = findViewById(R.id.container);

        // Ambil email login dari SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        emailLogin = prefs.getString(KEY_LOGGED_IN_EMAIL, null);

        if (emailLogin == null) {
            Intent intent = new Intent(home.this, login.class);
            startActivity(intent);
            finish();
            return;
        }

        // Navigasi
        navMedis.setOnClickListener(v -> {
            Intent intent = new Intent(home.this, rekam.class);
            intent.putExtra("email", emailLogin);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(home.this, profil.class);
            intent.putExtra("email", emailLogin);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        // Notifikasi
        createNotificationChannel();

        // Ambil data jadwal dari Firebase
        jadwalRef = FirebaseDatabase.getInstance().getReference("jadwal");

        jadwalRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long currentCount = snapshot.getChildrenCount();

                if (lastJadwalCount != -1 && currentCount > lastJadwalCount) {
                    showNotification("Informasi Jadwal Baru", "Jadwal kegiatan terbaru telah ditambahkan.");
                }

                lastJadwalCount = currentCount;
                container.removeAllViews();

                List<DataSnapshot> snapshots = new ArrayList<>();
                for (DataSnapshot jadwalSnapshot : snapshot.getChildren()) {
                    snapshots.add(jadwalSnapshot);
                }
                Collections.reverse(snapshots); // Jadwal terbaru di atas

                for (DataSnapshot jadwalSnapshot : snapshots) {
                    String kegiatan = jadwalSnapshot.child("kegiatan").getValue(String.class);
                    String tgl = jadwalSnapshot.child("tanggal").getValue(String.class);
                    String wkt = jadwalSnapshot.child("waktu").getValue(String.class);
                    String tmp = jadwalSnapshot.child("tempat").getValue(String.class);

                    // Inflate layout item_kegiatan.xml
                    View itemView = getLayoutInflater().inflate(R.layout.item_kegiatan, container, false);

                    TextView tvJudul = itemView.findViewById(R.id.tvJudul);
                    TextView tvTanggal = itemView.findViewById(R.id.tvTanggal);
                    TextView tvWaktu = itemView.findViewById(R.id.tvWaktu);
                    TextView tvTempat = itemView.findViewById(R.id.tvTempat);

                    tvJudul.setText("Kegiatan: " + (kegiatan != null ? kegiatan : "-"));
                    tvTanggal.setText("Tanggal: " + (tgl != null ? tgl : "-"));
                    tvWaktu.setText("Waktu: " + (wkt != null ? wkt : "-"));
                    tvTempat.setText("Tempat: " + (tmp != null ? tmp : "-"));

                    container.addView(itemView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(home.this, "Gagal mengambil data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String name = "Notifikasi Jadwal";
            String description = "Pemberitahuan jadwal kegiatan terbaru";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void showNotification(String title, String message) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        Intent intent = new Intent(this, home.class);
        intent.putExtra("email", emailLogin);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                        PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(2, builder.build());
    }
}
