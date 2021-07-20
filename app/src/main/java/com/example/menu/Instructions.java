package com.example.menu;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;

import com.example.scanner.R;

public class Instructions extends AppCompatActivity {
    private TextView text;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        text=findViewById(R.id.textId);
        text.setText("SCANNER:\n" +
                "- Be sure to be in proper light conditions, or else the colors cannot be viewed correctly. The light should not " +
                "be too much bright or dimmed.\n"+
                "- If your cube does not have the standard colors (white, yellow, orange, red, green, blue), the app won't work.\n" +
                "- Start from any face, and if every color on screen matches the ones on your cube, press the scan button on the bottom right.\n" +
                "- After each scan, turn the cube following the directions on the left side.\n" +
                "- An indicator on the top left will show you how many faces you have already scanned.\n" +
                "- You can see the last scanned face on the right. In case of wrong colors, you can press the " +
                "undo button (on the bottom left) to scan again that face.\n" +
                "- When you have finished scanning every face, if something went wrong you will be asked to scan again the cube (from the start).\n" +
                "\n" +
                "MOVES SHOWER:\n" +
                "- If the scanning went successfully, you will see a virtual representation of the cube. \n" +
                "- You can move the virtual cube by swiping your fingers on the screen.\n" +
                "- Be sure that the app is showing the same face you are looking on the real cube before pressing any button.\n" +
                "- Press \"NEXT MOVE\" to show the next move to follow.\n" +
                "- press \"PREV. MOVE\" to undo the last action. ");
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }


}