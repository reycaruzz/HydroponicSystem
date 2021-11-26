package com.hidroponik.farm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private TextView SensorPpm, SensorPh, SensorSuhu, SetPt, PomABMix, PomAir, PomPhUp, PomPhD;
    private SeekBar SeekTds;

    private final FirebaseDatabase db = FirebaseDatabase.getInstance();
    private final DatabaseReference Sensor_Ppm = db.getReference("ppm_value");
    private final DatabaseReference Sensor_Ph = db.getReference("ph_value");
    private final DatabaseReference Sensor_Suhu = db.getReference("suhu_value");
    private final DatabaseReference Set_Pt = db.getReference("setpointTDS");
    private final DatabaseReference Pom_ABMix = db.getReference("pom_abmix");
    private final DatabaseReference Pom_Air = db.getReference("pom_air");
    private final DatabaseReference Pom_PhUp = db.getReference("pom_phup");
    private final DatabaseReference Pom_PhD = db.getReference("pom_phdn");

    int cMax = 1400, cStep = 50, cProg, pProg, nProg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SensorPpm = (TextView) findViewById(R.id.ppm);
        SensorPh = (TextView) findViewById(R.id.ph);
        SensorSuhu = (TextView) findViewById(R.id.suhu);
        SetPt = (TextView) findViewById(R.id.setpoint);
        PomABMix = (TextView) findViewById(R.id.p_abmix);
        PomAir = (TextView) findViewById(R.id.p_air);
        PomPhUp = (TextView) findViewById(R.id.p_phup);
        PomPhD = (TextView) findViewById(R.id.p_phdn);
        SeekTds = (SeekBar) findViewById(R.id.seek_ppm);

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
                    pProg = cProg;
                    Toast.makeText(MainActivity.this, "Setpoint berhasil di perbaharui", Toast.LENGTH_SHORT).show();
                });
                alert.setNegativeButton("Batal", (dialog, which) -> {
                    SetPt.setText("" + pProg);
                    // TODO help for get the previous value and set the progress for seekbar
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Gagal membaca data PPM!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}