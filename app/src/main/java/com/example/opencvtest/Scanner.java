package com.example.opencvtest;

import cs.min2phase.Search;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;


import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.menu.Instructions;
import com.example.mostraMosse.PuzzleDroidActivity;

import java.util.ArrayList;

import static org.opencv.core.Core.mean;
import static org.opencv.imgproc.Imgproc.cornerHarris;
import static org.opencv.imgproc.Imgproc.putText;
import static org.opencv.imgproc.Imgproc.rectangle;


public class Scanner extends Activity implements CvCameraViewListener2 {
    JavaCameraView camera;//view della fotocamera
    BaseLoaderCallback baseLoaderCallback;//bo
    Mat mRgba;//matrice dei pixel
    //COORDINATE QUADRATO
    int thicknessRect=13, sizeRect=125;
    ArrayList<Square> squares; //lista dei 9 quadrati da stampare su schermo
    private int squareLayoutDistance = 200; //distanza tra l'origine di un quadrato e un altro
    //array che indica come sono disposti i 9 quadrati
    private Point[] squareLocations = {
            new Point(-1,-1),new Point(0,-1),new Point(1,-1),
            new Point(-1,0),new Point(0,0),new Point(1,0),
            new Point(-1,1),new Point(0,1),new Point(1,1)};
    Point textDrawPoint, arrowDrawPoint, textFaceIndex; //coordinate del punto di origine del testo e della freccia direzionale
    Scalar colorText = new Scalar(153,50,204,255); //colore dei testi
    private String[] faces; //array delle facce
    private ImageButton saveFaceButton; //bottone per salvare la faccia
    private ImageButton undoButton; //bottone per annullare l'ultima faccia scannerizzata
    private ImageButton instructionsButton; //bottone per aprire le istruzioni
    private Intent instructionsIntent; //intent per andare all'activity delle istruzioni
    private int index=-1; //indice che tiene traccia delle facce scansionate
    private char[] sides =new char[6];
    private Intent cubeIntent;
    long timeOffset;




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        saveFaceButton =findViewById(R.id.saveFaceButton);
        undoButton=findViewById(R.id.undoButton);
        instructionsButton=findViewById(R.id.instructions_button);
        instructionsIntent=new Intent(this, Instructions.class);
        faces = new String[6]; //ci sono 6 facce
        cubeIntent = new Intent(this, PuzzleDroidActivity.class);

        if(permission()) { //dopo aver chiesto i permessi per la fotocamera
            //inizializzazione fotocamera
            camera = findViewById(R.id.javaCameraView);
            camera.setVisibility(SurfaceView.VISIBLE);
            camera.setCameraPermissionGranted(); //permessi camera
            camera.setCvCameraViewListener(this);

            baseLoaderCallback = new BaseLoaderCallback(this) { //FA PARTIRE LA VIDEOCAMERA SE OPENCV E' PARTITO BENE
                @Override
                public void onManagerConnected(int status) {
                    switch (status) {
                        case LoaderCallbackInterface.SUCCESS: {
                            Log.d("OPENCV","SIIIII");
                            camera.enableView(); //attivo la fotocamera se tutto è andato bene
                        }
                        break;
                        default: {
                            super.onManagerConnected(status);
                            Log.d("OPENCV","NOOOO");
                        }
                        break;
                    }
                }
            };
        }

        //INIZIALIZZA OGGETTO PER SOLUZIONE CUBO
        if (!Search.isInited())
            Search.init();

        //listener bottone che salva faccia
        saveFaceButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveFace();
            }
        });

        //listener bottone che salva faccia
        undoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(index>=0)
                    index--;
            }
        });

        //listener del bottone per istruzioni
        instructionsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(instructionsIntent);
            }
        });
    }

    //METODI DA IMPLEMENTARE DELL'INTERFACCIA (però non so cosa facciano e non le implemento lolz)
    @Override
    public void onPause() {
        super.onPause();
        if (camera != null)
            camera.disableView();
    }

    public void onDestroy() {
        super.onDestroy();
        if (camera != null)
            camera.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }


    //+++++++++++++++++++FUNZIONE SU CUI SI FANNO OPERAZIONI SULL'IMMAGINE (per ogni frame da come parametro l'immagine inputFrame presa dalla telecamera)++++++++++++++++
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame)
    {
        mRgba=inputFrame.rgba(); //prendo la matrice dei valori rgb

        //se non sono ancora stati istanziati i rettangoli, lo faccio
        if(squares==null) {
            Point tempCenter = new Point(mRgba.width() / 2, mRgba.height() / 2); //centro dello schermo
            squares=new ArrayList<>();
            //creo i 9 quadrati, che hanno come coordinate la posizione di squareLocations, moltiplicata per la distanza, rispetto al centro dello schermo
            for(Point squareLocation: squareLocations){
                squares.add(new Square(new Point(squareLocation.x*squareLayoutDistance+tempCenter.x,squareLocation.y*squareLayoutDistance+tempCenter.y),sizeRect));
            }
        }
        if(System.currentTimeMillis()-timeOffset>=200)
            processColor(); //controllo i colori
        drawSquares(); //disegno i rettangoli sullo schermo
        return mRgba;
    }

    //++++++++++++++++++funzione che controlla il colore+++++++++++++++++++
    private void processColor() {
        Scalar tmpColor;
        for(Square s : squares) {
            Mat rectRgba = mRgba.submat(s.getRect());  //considero solo i pixel nel quadrato
            tmpColor = mean(rectRgba); //faccio una media dei colori (rgb)
            s.setColorRgb(tmpColor);
        }
        timeOffset=System.currentTimeMillis();

    }

    //funzione che disegna i quadrati e le scritte
    private void drawSquares(){
        for (int i = 0; i < squares.size(); i++) {
            Square s = squares.get(i);
            textDrawPoint = new Point(s.getCenter().x-100,s.getCenter().y);
            textFaceIndex= new Point(100, 100); //stampa il contatore della faccia in alto a sx;
            Scalar showColor=charToRGB(s.getColor());
            rectangle(mRgba,  s.getTopLeftPoint(), s.getBottomRightPoint(), showColor, thicknessRect);
            putText(mRgba,s.getColor(),textDrawPoint,1,6,colorText,8);
            putText(mRgba, "Face "+(index+2), textFaceIndex, 1, 6, colorText, 8 );

            //stampo la freccia solop se non è la prima scannerizzazione
            if(index>=0) {
                arrowDrawPoint = new Point(100, mRgba.height() - 300);
                if (index<3)
                    putText(mRgba, "right", arrowDrawPoint, 1, 4, colorText, 7);
                else if(index==3)
                    putText(mRgba, "right + up", arrowDrawPoint, 1, 4, colorText, 7);
                else if(index==4)
                    putText(mRgba, "down + down", arrowDrawPoint, 1, 4, colorText, 7);
            }
        }

        if(index>=0) //disegno l'ultima faccia scannerizzata (dalla seconda mossa in poi)
            drawLastFace();
    }

    //++++++++++++++++++++++DISEGNA L'ULTIMA FACCIA SCANNERIZZATA SULLO SCHERMO++++++++++++++++++++++
    void drawLastFace(){
        Point tmpPoint;
        int layoutDistance=55;
        int width=50;
        Point tempCenter = new Point(mRgba.width() -300, (mRgba.height() / 2)-100); //centro della faccia cche disegno (in centro a dx)

        for(int i=0; i<9; i++) {
            tmpPoint = new Point(squareLocations[i].x * layoutDistance + tempCenter.x, squareLocations[i].y * layoutDistance + tempCenter.y);
            Scalar showColor=charToRGB(faces[index].substring(i,i+1));
            rectangle(mRgba,new Rect((int)tmpPoint.x, (int)tmpPoint.y, width, width), showColor, -1);
        }
    }

    //++++++++++++++++++++++funzione che salva nel vettore faces la faccia corrente, quando viene cliccato il pulsante++++++++++++++++++
    public void saveFace(){
        //creo la stringa dei colori dei quadrati della faccia, chiamando il metodo getColor() per ogni Square della faccia
        String tempString = "";
        for (int i = 0; i < squares.size(); i++) {
            Square s = squares.get(i);
            tempString += s.getColor();
        }


        //se si è riuscito a leggere correttamente il colore del quadrato centrale
        if( tempString.charAt(4)!='n') {
            index++;
        }
        sides[index]=tempString.charAt(4);


        faces[index] = tempString;
        Log.i("faccia", faces[index].toString());

        //ULTIMA SCANSIONE
        if(index==5) {  //vedo se ci sono errori e cambio activity
            String sol = solve(faces, sides);
            Log.d("faccia", sol);

            // SE CI SONO ERRORI STAMPO L'ERRORE E FACCIO RIPARTIRE LA SCANNERIZZAZIONE
            if(sol.length()==0) {
                sol = "Cube already solved!";
                new AlertDialog.Builder(this)
                        .setTitle("ERROR")
                        .setMessage(sol+"\nScan correctly to continue.")
                        .setPositiveButton(android.R.string.yes, null)
                        .show();
                index=-1;
            }
            else if (sol.contains("Error")) {
                switch (sol.charAt(sol.length() - 1)) {
                    case '1':
                        sol = "There are not exactly nine squares of each color!";
                        break;
                    case '2':
                        sol = "Not all 12 edges exist exactly once!";
                        break;
                    case '3':
                        sol = "Flip error: One edge has to be flipped!";
                        break;
                    case '4':
                        sol = "Not all 8 corners exist exactly once!";
                        break;
                    case '5':
                        sol = "Twist error: One corner has to be twisted!";
                        break;
                    case '6':
                        sol = "Parity error: Two corners or two edges have to be exchanged!";
                        break;
                    case '7':
                        sol = "No solution exists for the given maximum move number!";
                        break;
                    case '8':
                        sol = "Timeout, no solution found within given maximum time!";
                        break;
                }
                new AlertDialog.Builder(this)
                        .setTitle("ERROR")
                        .setMessage(sol+"\nScan correctly to continue.")
                        .setPositiveButton(android.R.string.yes, null)
                        .show();
                index=-1;
            }
            //SE NON CI SONO ERRORI PASSO LA SOLUZIONE ALL'ACTIVITY DEL CUBO
            else {
                cubeIntent.putExtra("configurazione", faces); //mando configurazione all'activity del cubo
                cubeIntent.putExtra("colori facce", sides); //mando il vettore dei colori delle facce. ordine:[0:F, 1:R, 2:B, 3:L, 4:U, 5:D]
                startActivity(cubeIntent); //passa all'activity del cubo
            }
        }

    }


    //++++++++++++++++++++++++++++++FUNZIONE CHE RISOLVE IL CUBO++++++++++++++++++++++++++++++++++++++++++
    public static String solve(String[] faces, char [] sides){
        Search search = new Search(); //fai import cs.min2phase.Search; (libreria per soluzione cubo)

        //DEVO CAMBIARE L'ORDINE DELLE FACCE (perchè la libreria ne richiede uno diverso):
        //dalla scannerizzazione è così:
        //[0:F, 1:R, 2:B, 3:L, 4:U, 5:D]
        //deve diventare:
        //[0:U, 1:R, 2:F, 3:D, 4:L, 5:B]
        String faceSwap[]=new String[6]; //vettore di stringhe su cui salvo le nuove facce cambiate di ordine
        faceSwap[0]=faces[4];
        faceSwap[1]=faces[1];
        faceSwap[2]=faces[0];
        faceSwap[3]=faces[5];
        faceSwap[4]=faces[3];
        faceSwap[5]=faces[2];
        StringBuffer tempString = new StringBuffer(54);
        String result;
        for (int i = 0; i < 54; i++)
            tempString.insert(i, 'B');// default initialization

        //traduco i colori per la libreria (devo passare dalle iniziali dei colori alle iniziali della faccia -> se dal lato davanti avevo il blu, cambia tutti i 'B' in 'F', e così via)
        for(int i=0; i<faces.length; i++){ //scorre le facce (6 in tuttio)
            for(int j=0; j<9; j++){ //scorre i quadrati di ogni faccia (9 per faccia)
                if(faceSwap[i].charAt(j)==sides[0])
                    tempString.setCharAt(9 * i + j, 'F'); //Front
                if(faceSwap[i].charAt(j)==sides[1])
                    tempString.setCharAt(9 * i + j, 'R'); //Right
                if(faceSwap[i].charAt(j)==sides[2])
                    tempString.setCharAt(9 * i + j, 'B'); //Back
                if(faceSwap[i].charAt(j)==sides[3])
                    tempString.setCharAt(9 * i + j, 'L'); //Left
                if(faceSwap[i].charAt(j)==sides[4])
                    tempString.setCharAt(9 * i + j, 'U'); //Up
                if(faceSwap[i].charAt(j)==sides[5])
                    tempString.setCharAt(9 * i + j, 'D'); //Down
            }
        }
        String cubeString=tempString.toString();
        //debug
        Log.d("faccia", cubeString);
        //chiama funzione della libreria kociemba che risolve il cubo
        result= search.solution(cubeString, 24, 100,0, 0);
        Log.d("faccia","RESULT: "+result);

        return result;
    }

    Scalar charToRGB(String color){
        Scalar showColor;
        switch (color){
            case "R":
                showColor=new Scalar(255,0,0);
                break;
            case "G":
                showColor=new Scalar(0,255,0);
                break;
            case "B":
                showColor=new Scalar(0,0,255);
                break;
            case "Y":
                showColor=new Scalar(255,255,0);
                break;
            case "O":
                showColor=new Scalar(255,165,0);
                break;
            case "W":
                showColor=new Scalar(255,255,255);
                break;
            default: //case "n"
                showColor=new Scalar(0,0,0);
                break;
        }
        return showColor;
    }
    //++++++++++++++++++++++++++++++INIZIALIZZAZIONE OPENCV++++++++++++++++++++++++++
    @Override
    public void onResume() {
        super.onResume();
        if(OpenCVLoader.initDebug()) {
            Log.d("OPENCV","OpenCV caricato correttamente");
            baseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS); //chiama baseLoaderCallback (definito in alto, fa attivare la videocamera se tutto va bene)
        }
        else{
            Log.d("OPENCV","Errore OpenCV non caricato");
        }

    }

    //++++++++++++++++++CONTROLLA CHE CI SIA IL PERMESSO DELLA FOTOCAMERA, ALTRIMENTI LO CHIEDE ALL'UTENTE+++++++++++++++++
    private boolean permission() {
        if (ContextCompat.checkSelfPermission(Scanner.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 50);
        } else {
            return true;
        }
        return false;
    }

}