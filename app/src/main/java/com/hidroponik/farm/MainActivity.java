package com.hidroponik.farm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hidroponik.farm.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    public static final String BroadcastStringForAction = "checkinternet";

    private TextView SensorPpm, SensorPh, SensorSuhu, SetPt, PomABMix, PomAir, PomPhUp, PomPhD, Waktu, LUdpt;
    private SeekBar SeekTds;
    private IntentFilter mif;
    private final FirebaseDatabase db = FirebaseDatabase.getInstance();
    private final DatabaseReference Sensor_Ppm = db.getReference("ppm_value");
    private final DatabaseReference Sensor_Ph = db.getReference("ph_value");
    private final DatabaseReference Sensor_Suhu = db.getReference("suhu_value");
    private final DatabaseReference Set_Pt = db.getReference("setpointTDS");
    private final DatabaseReference Pom_ABMix = db.getReference("pom_abmix");
    private final DatabaseReference Pom_Air = db.getReference("pom_air");
    private final DatabaseReference Pom_PhUp = db.getReference("pom_phup");
    private final DatabaseReference Pom_PhD = db.getReference("pom_phd");

    int cMax = 1400;
    int cStep = 50;
    int cProg;
    private String text, text1;
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String TEXT = "text";
    public static final String TEXT1 = "text1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SensorPpm = (TextView) findViewById(R.id.ppm);
        SensorPh = (TextView) findViewById(R.id.ph);
        SensorSuhu = (TextView) findViewById(R.id.suhu);
        SetPt = (TextView) findViewById(R.id.setpoint);
        PomABMix = (TextView) findViewById(R.id.p_abmix);
        PomAir = (TextView) findViewById(R.id.p_air);
        PomPhUp = (TextView) findViewById(R.id.p_phup);
        PomPhD = (TextView) findViewById(R.id.p_phdn);
        SeekTds = (SeekBar) findViewById(R.id.seek_ppm);
        Waktu = (TextView) findViewById(R.id.waktu);
        LUdpt = (TextView) findViewById(R.id.lupdt);

        SharedPreferences sP = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        text = sP.getString(TEXT, "");
        SetPt.setText(text);
        SeekTds.setProgress((!text.equals("") ? Integer.parseInt(text) : 0) / cStep);

        mif = new IntentFilter();
        mif.addAction(BroadcastStringForAction);
        Intent sIntent = new Intent(this, netService.class);
        startService(sIntent);

        binding.lupdt.setVisibility(View.GONE);
        binding.waktu.setVisibility(View.GONE);
        if (isOnline(getApplicationContext())) {
            text1 = sP.getString(TEXT1, "");
            Waktu.setText(text1);
            Set_Visibility_ON();
            Toast.makeText(MainActivity.this, "Internet tersambung!", Toast.LENGTH_LONG).show();
        } else {
            Set_Visibility_OFF();
            Toast.makeText(MainActivity.this, "Internet terputus!", Toast.LENGTH_LONG).show();
        }

        SeekTds.setMax(cMax / cStep);
        SeekTds.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                cProg = progress * cStep;
                SetPt.setText("" + cProg);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                alert.setTitle("Konfirmasi");
                alert.setMessage("Ubah setpoint TDS menjadi " + SetPt.getText() + " PPM");
                alert.setCancelable(false);
                alert.setPositiveButton("Ya", (dialog, which) -> {
                    Set_Pt.setValue(SetPt.getText());

                    SharedPreferences sP = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sP.edit();
                    editor.putString(TEXT, SetPt.getText().toString());
                    editor.apply();

                    updateWaktu();
                    Toast.makeText(MainActivity.this, "Setpoint berhasil di perbaharui", Toast.LENGTH_SHORT).show();
                });
                alert.setNegativeButton("Batal", (dialog, which) -> {
                    SharedPreferences sP = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                    text = sP.getString(TEXT, "");
                    SetPt.setText(text);
                    SeekTds.setProgress(Integer.parseInt(text) / cStep);
                    Toast.makeText(MainActivity.this, "Setpoint batal di perbaharui", Toast.LENGTH_SHORT).show();
                });
                alert.show();
            }
        });

        Sensor_Ppm.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String tb_ppm = snapshot.getValue(String.class);
                SensorPpm.setText(tb_ppm);
                updateWaktu();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Gagal membaca data PPM!" + error.toException(), Toast.LENGTH_SHORT).show();
            }
        });
        Sensor_Ph.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String tb_ph = snapshot.getValue(String.class);
                SensorPh.setText(tb_ph);
                updateWaktu();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Gagal membaca data pH!" + error.toException(), Toast.LENGTH_SHORT).show();
            }
        });
        Sensor_Suhu.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String tb_suhu = snapshot.getValue(String.class);
                SensorSuhu.setText(tb_suhu);
                updateWaktu();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Gagal membaca data suhu!" + error.toException(), Toast.LENGTH_SHORT).show();
            }
        });
        Set_Pt.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String tb_pt = snapshot.getValue(String.class);
                SeekTds.setProgress((!text.equals("") ? Integer.parseInt(tb_pt) : 0) / cStep);
                updateWaktu();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Gagal membaca data setpoint TDS!" + error.toException(), Toast.LENGTH_SHORT).show();
            }
        });
        Pom_ABMix.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String tb_abmix = snapshot.getValue(String.class);
                if (tb_abmix.equals("H")) {
                    PomABMix.setText("ON");
                    PomABMix.setTextColor(Color.rgb(0, 255, 0));
                    updateWaktu();
                } else if (tb_abmix.equals("L")) {
                    PomABMix.setText("OFF");
                    PomABMix.setTextColor(Color.rgb(255, 0, 0));
                    updateWaktu();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Gagal membaca data pompa ABMix!" + error.toException(), Toast.LENGTH_SHORT).show();
            }
        });
        Pom_Air.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String tb_air = snapshot.getValue(String.class);
                if (tb_air.equals("H")) {
                    PomAir.setText("ON");
                    PomAir.setTextColor(Color.rgb(0, 255, 0));
                    updateWaktu();
                } else if (tb_air.equals("L")) {
                    PomAir.setText("OFF");
                    PomAir.setTextColor(Color.rgb(255, 0, 0));
                    updateWaktu();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Gagal membaca data pompa air!" + error.toException(), Toast.LENGTH_SHORT).show();
            }
        });
        Pom_PhUp.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String tb_phup = snapshot.getValue(String.class);
                if (tb_phup.equals("H")) {
                    PomPhUp.setText("ON");
                    PomPhUp.setTextColor(Color.rgb(0, 255, 0));
                    updateWaktu();
                } else if (tb_phup.equals("L")) {
                    PomPhUp.setText("OFF");
                    PomPhUp.setTextColor(Color.rgb(255, 0, 0));
                    updateWaktu();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Gagal membaca data pompa pH up!" + error.toException(), Toast.LENGTH_SHORT).show();
            }
        });
        Pom_PhD.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String tb_phd = snapshot.getValue(String.class);
                if (tb_phd.equals("H")) {
                    PomPhD.setText("ON");
                    PomPhD.setTextColor(Color.rgb(0, 255, 0));
                    updateWaktu();
                } else if (tb_phd.equals("L")) {
                    PomPhD.setText("OFF");
                    PomPhD.setTextColor(Color.rgb(255, 0, 0));
                    updateWaktu();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Gagal membaca data pompa pH down!" + error.toException(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateWaktu() {
        Calendar calendar = Calendar.getInstance();
        DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
        String cDate = df.format(calendar.getTime());
        Waktu.setText(cDate);

        SharedPreferences sP = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sP.edit();
        editor.putString(TEXT1, Waktu.getText().toString());
        editor.apply();
    }

    public BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BroadcastStringForAction)) {
                if (intent.getStringExtra("online_status").equals("true")) {
                    Set_Visibility_ON();
                } else {
                    Set_Visibility_OFF();
                }
            }
        }
    };

    public boolean isOnline(Context c) {
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni != null && ni.isConnectedOrConnecting())
            return true;
        else
            return false;
    }

    public void Set_Visibility_ON() {
        binding.waktu.setVisibility(View.VISIBLE);
        binding.lupdt.setVisibility(View.VISIBLE);
    }

    public void Set_Visibility_OFF() {
        binding.waktu.setVisibility(View.GONE);
        binding.lupdt.setVisibility(View.GONE);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        registerReceiver(mReceiver, mif);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mif);
    }
}