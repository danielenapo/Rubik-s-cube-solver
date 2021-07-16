package com.example.menu;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.opencvtest.Scanner;
import com.example.opencvtest.R;

public class Menu extends AppCompatActivity {
    private Button scanButton, instructionsButton;
    private Intent scanIntent, instructionsIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        scanButton= findViewById(R.id.scan_button);
        instructionsButton= findViewById(R.id.instruction_button);

        scanIntent = new Intent(this, Scanner.class);
        instructionsIntent = new Intent(this, Instructions.class);


        //listener del bottone per iniziare la scannerizzazione
        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(scanIntent);
            }
        });

        //listener del bottone per test cubo
        instructionsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(instructionsIntent);
            }
        });
    }

}