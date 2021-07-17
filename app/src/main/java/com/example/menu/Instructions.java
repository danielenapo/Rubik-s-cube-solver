package com.example.menu;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.example.opencvtest.R;

public class Instructions extends AppCompatActivity {
    private TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);

        text=findViewById(R.id.textId);
        text.setText("SCANNER:\n" +
                "- First, be sure to be in a proper light condition, or else the colors cannot be viewed correctly.\n" +
                "- Start from any face, and when your ready press the scan button on the right.\n" +
                "- After each scan, turn the cube following the directions on the left side.\n" +
                "- An indicator on the top left will show you how many faces you've already scanned.\n" +
                "- You can see the last scanned face on the right. In case of wrong colors, you can press the " +
                "undo button (on the bottom left) to scan again that face.\n" +
                "-After 6 scans, if something went wrong, you will be asked to scan again the cube (from the start).\n" +
                "\n" +
                "MOVES SHOWER:\n" +
                "- If the scanning went succesfully, you will see a virtual representation of your cube. \n" +
                "- You can move the virtual cube by swiping your fingers on the screen.\n" +
                "- Be sure that the app is showing the same face you're looking on the real cube.\n" +
                "- Press \"NEXT MOVE\" to show the next move to follow.\n" +
                "- press \"PREV. MOVE\" to undo the last action. ");
    }

}