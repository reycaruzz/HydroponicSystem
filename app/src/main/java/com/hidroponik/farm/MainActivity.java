package com.hidroponik.farm;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.ajts.androidmads.library.SQLiteToExcel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hidroponik.farm.databinding.ActivityMainBinding;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    ActivityMainBinding binding;
    public static final String BroadcastStringForAction = "checkinternet";
    private TextView SensorPpm, SensorPh, SensorSuhu, SetPt, PomABMix, PomAir, PomPhUp, PomPhD, Waktu;
    DatabaseHelper dbhelp;
    private SeekBar SeekTds;
    private IntentFilter mif;
    SQLiteToExcel sto;
    Handler mHandler;
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
    long id;
    private String text;
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String TEXT = "text";
    public static final String TEXT1 = "text1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        dbhelp = new DatabaseHelper(this);
        id = getIntent().getLongExtra(DatabaseHelper.COLUMN_ID, 0);
        File file = new File(this.getApplicationContext().getExternalFilesDir(null).toString());
        if (!file.exists()) {
            Log.v("File Created", String.valueOf(file.mkdirs()));
        }
        SensorPpm = findViewById(R.id.ppm);
        SensorPh = findViewById(R.id.ph);
        SensorSuhu = findViewById(R.id.suhu);
        SetPt = findViewById(R.id.setpoint);
        PomABMix = findViewById(R.id.p_abmix);
        PomAir = findViewById(R.id.p_air);
        PomPhUp = findViewById(R.id.p_phup);
        PomPhD = findViewById(R.id.p_phdn);
        SeekTds = findViewById(R.id.seek_ppm);
        Waktu = findViewById(R.id.waktu);
        Button pindahData = (Button) findViewById(R.id.btn1);
        SharedPreferences sP = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        text = sP.getString(TEXT, "0");
        SetPt.setText(text);
        SeekTds.setProgress((!text.equals("") ? Integer.parseInt(text) : 0) / cStep);
        mif = new IntentFilter();
        mif.addAction(BroadcastStringForAction);
        Intent sIntent = new Intent(this, netService.class);
        startService(sIntent);
        binding.lupdt.setVisibility(View.GONE);
        binding.waktu.setVisibility(View.GONE);
        if (isOnline(getApplicationContext())) {
            updateWaktu();
            String text1 = sP.getString(TEXT1, "0");
            Waktu.setText(text1);
            Set_Visibility_ON();
            Toast.makeText(MainActivity.this, "Internet tersambung!", Toast.LENGTH_LONG).show();
            Log.d(TAG, "Internet tersambung!");
        } else {
            Set_Visibility_OFF();
            Waktu.setText(R.string.not_connected);
            Toast.makeText(MainActivity.this, "Internet terputus!", Toast.LENGTH_LONG).show();
            Log.d(TAG, "Internet terputus");
        }
        SeekTds.setMax(cMax / cStep);
        SeekTds.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                cProg = progress * cStep;
                SetPt.setText("" + cProg);
                Log.d(TAG, "Seekbar berubah : " + cProg);
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
                    if (isOnline(getApplicationContext())) {
                        Set_Pt.setValue(SetPt.getText());
                        Log.e(TAG, "setpoint berubah :");

                        SharedPreferences sP = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                        SharedPreferences.Editor editor = sP.edit();
                        editor.putString(TEXT, SetPt.getText().toString());
                        editor.apply();
                        updateWaktu();
                        Toast.makeText(MainActivity.this, "Setpoint berhasil di perbaharui", Toast.LENGTH_SHORT).show();
                    } else {
                        SharedPreferences sP = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                        text = sP.getString(TEXT, "0");
                        SetPt.setText(text);
                        SeekTds.setProgress(Integer.parseInt(text) / cStep);
                        Toast.makeText(MainActivity.this, "Tidak ada koneksi internet!", Toast.LENGTH_SHORT).show();
                    }
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
        pindahData.setOnClickListener(v -> dataPindah());
        updateData();
        this.mHandler = new Handler();
        runnable.run();
    }

    public void updateData() {
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
                    PomABMix.setText(R.string.PompaON);
                    PomABMix.setTextColor(Color.rgb(0, 255, 0));
                    updateWaktu();
                } else if (tb_abmix.equals("L")) {
                    PomABMix.setText(R.string.PompaOFF);
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
                    PomAir.setText(R.string.PompaON);
                    PomAir.setTextColor(Color.rgb(0, 255, 0));
                    updateWaktu();
                } else if (tb_air.equals("L")) {
                    PomAir.setText(R.string.PompaOFF);
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
                    PomPhUp.setText(R.string.PompaON);
                    PomPhUp.setTextColor(Color.rgb(0, 255, 0));
                    updateWaktu();
                } else if (tb_phup.equals("L")) {
                    PomPhUp.setText(R.string.PompaOFF);
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
                    PomPhD.setText(R.string.PompaON);
                    PomPhD.setTextColor(Color.rgb(0, 255, 0));
                    updateWaktu();
                } else if (tb_phd.equals("L")) {
                    PomPhD.setText(R.string.PompaOFF);
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

    public void dataPindah() {
        sto = new SQLiteToExcel(getApplicationContext(), "data_sensor", this.getApplicationContext().getExternalFilesDir(null).toString());
        sto.exportAllTables("LogSensor.xls", new SQLiteToExcel.ExportListener() {
            @Override
            public void onStart() {
                Log.d(TAG, "onStart");
            }

            @Override
            public void onCompleted(String filePath) {
                Toast.makeText(MainActivity.this, "Sukses export!\nPath:" + filePath, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onCompleted " + filePath);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(MainActivity.this, "Gagal export!" + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onError " + e.getMessage());
            }
        });
    }

    private void updateWaktu() {
        Calendar calendar = Calendar.getInstance();
//        DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
        @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
        String cDate = df.format(calendar.getTime());
        Waktu.setText(cDate);

        SharedPreferences sP = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sP.edit();
        editor.putString(TEXT1, Waktu.getText().toString());
        editor.apply();
    }

    private final Runnable runnable = new Runnable() {
        public void run() {
            ContentValues values = new ContentValues();
            MainActivity.this.mHandler.postDelayed(runnable, 1000);
            values.put(DatabaseHelper.COLUMN_PPM, String.valueOf(SensorPpm.getText()));
            values.put(DatabaseHelper.COLUMN_PH, String.valueOf(SensorPh.getText()));
            values.put(DatabaseHelper.COLUMN_SUHU, String.valueOf(SensorPpm.getText()));
            values.put(DatabaseHelper.COLUMN_SETPOINT, String.valueOf(SetPt.getText()));
            values.put(DatabaseHelper.COLUMN_PABMIX, String.valueOf(PomABMix.getText()));
            values.put(DatabaseHelper.COLUMN_PAIR, String.valueOf(PomAir.getText()));
            values.put(DatabaseHelper.COLUMN_PUP, String.valueOf(PomPhUp.getText()));
            values.put(DatabaseHelper.COLUMN_PDN, String.valueOf(PomPhD.getText()));
            dbhelp.insertData(values);
//            Log.d(TAG, "postdb->" + values);
        }
    };
    public BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BroadcastStringForAction)) {
                if (intent.getStringExtra("online_status").equals("true")) {
                    Set_Visibility_ON();
                } else {
                    Waktu.setText(R.string.not_connected);
                    Toast.makeText(MainActivity.this, "Internet terputus!", Toast.LENGTH_LONG).show();
                    Set_Visibility_OFF();
                }
            }
        }
    };

    public boolean isOnline(Context c) {
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnectedOrConnecting();
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
        runnable.run();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
        mHandler.removeCallbacks(runnable);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mif);
        runnable.run();
    }
}