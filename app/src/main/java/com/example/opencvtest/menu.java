package com.example.opencvtest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.mostraMosse.PuzzleDroidActivity;

public class menu extends AppCompatActivity {
    private Button scanButton, cubeButton;
    private Intent scanIntent, cubeIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        scanButton= findViewById(R.id.scan_button);
        cubeButton= findViewById(R.id.testCubo);

        scanIntent = new Intent(this, MainActivity.class);
        cubeIntent = new Intent(this, PuzzleDroidActivity.class);


        //listener del bottone per iniziare la scannerizzazione
        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(scanIntent);
            }
        });

        //listener del bottone per test cubo
        cubeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(cubeIntent);
            }
        });
    }

}