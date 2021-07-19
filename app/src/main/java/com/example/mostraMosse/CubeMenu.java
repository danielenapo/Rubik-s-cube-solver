package com.example.mostraMosse;

import android.content.Context;
import android.view.MotionEvent;

import com.example.scanner.Scanner;
import com.example.scanner.R;
import com.threeDBJ.MGraphicsLib.GLColor;
import com.threeDBJ.MGraphicsLib.GLEnvironment;
import com.threeDBJ.MGraphicsLib.math.Vec2;
import com.threeDBJ.MGraphicsLib.texture.TextureButton;
import com.threeDBJ.MGraphicsLib.texture.TextureView.TextureClickListener;
import com.threeDBJ.MGraphicsLib.texture.TextureFont;

import com.threeDBJ.MGraphicsLib.texture.TextureView;
//import com.threeDBJ.puzzleDroidFree.util.Util;

//import org.kociemba.twophase.Search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ListIterator;

import javax.microedition.khronos.opengles.GL11;

import timber.log.Timber;

public class CubeMenu extends GLEnvironment {

    private static final int NONE = 0, SINGLE_TOUCH = 1, MULTI_TOUCH = 2;

    private float MENU_HEIGHT, MENU_WIDTH;

    private RubeCube cube;

    private TextureFont font;

    private TextureView menuView;

    private TextureButton prevMove;
    private TextureButton nextMove;

    private char[] configurazione;
    private ArrayList<String> listaMosse;
    private ListIterator<String> iteratorMosse;

    private boolean showing = false, showingTimer = false;
    private float xMin, xMax, yMin, yMax;
    private float x1, y1;
    private int activePtrId = -1, touchMode = NONE;

    private boolean restoreStartTimer = false, restoreOnSetBounds = false;
    private int restoreTime = 0, restoreCubeDim = 3;

    public CubeMenu(RubeCube cube, TextureFont font) {
        this.cube = cube;
        this.font = font;
        this.configurazione=cube.getConfigurazione();
        setListaMosse(configurazione);

        menuView = new TextureView();

        nextMove= new TextureButton(this.font);
        prevMove = new TextureButton(this.font);


        generate();
        enableTextures();
    }

    public void init(GL11 gl, Context context) {  //setto la texture e assegno una funzione handler per ogni bottone

        showing = true;
        //menuView.animate(new TranslateAnimation(10, 0f, MENU_HEIGHT, 0f));
        menuView.setTexture(gl, context, R.drawable.menu_background);

        prevMove.setTexture(gl, context, R.drawable.btn_transparent_normal);
        prevMove.setPressedTexture(gl, context, R.drawable.btn_transparent_pressed);
        prevMove.setClickListener(new TextureClickListener() {
            public void onClick() {
                if(iteratorMosse.hasPrevious()){
                    CubeView.stopInput=true;
                    try {
                        cube.tradMossa(iteratorMosse.previous(), false);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    /*try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }*/
                }


            }
        });

        nextMove.setTexture(gl, context, R.drawable.btn_transparent_normal);
        nextMove.setPressedTexture(gl, context, R.drawable.btn_transparent_pressed);
        nextMove.setClickListener(new TextureClickListener() {

            public void onClick() {
                if(iteratorMosse.hasNext()){
                    CubeView.stopInput=true;
                    try {
                        cube.tradMossa(iteratorMosse.next(),true);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    /*try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }*/
                }

            }

        });

    }

    public void setListaMosse(char[] configurazione) {//setto la lista di mosse partendo dalla configurazione del cubo

        //String faces = "FBLDUFLLRDUURRLDLUFBFDFBDULRRBFDUULRLRDDLFFRBBUUDBBBFR";

        /*
         *   U bianco
         *   R rosso
         *   F verde
         *   D giallo
         *   L arancione
         *   B blu
         */
        char []tmp=configurazione.clone();
        for(int i=0;i<tmp.length;i++){
            switch (tmp[i]){
                case 'w':
                    tmp[i]='U';
                    break;
                case 'r':
                    tmp[i]='R';
                    break;
                case 'g':
                    tmp[i]='F';
                    break;
                case 'y':
                    tmp[i]='D';
                    break;
                case 'o':
                    tmp[i]='L';
                    break;
                case 'b':
                    tmp[i]='B';
                    break;
            }
        }

        //String configStr=new String(tmp);

        //System.out.println("DEBUG "+Arrays.toString(tmp)+" --- "+configStr);
        long xtime= System.nanoTime();
        String solution = Scanner.solve(MostraMosseActivity.configurazione, MostraMosseActivity.coloriFacce);
        solution = solution.replaceAll("  "," ");
        solution = RubeCube.replaceDoubleMove(solution);
        //String solution = Search.solution(configStr,21,6,false);
        //String solution = "R L F2 B' U2 R' B2 D L' D' F' U2 B2 L2 U L2 U2 F2 L2 U'";
        System.out.println("|||||| TEMPO calcolo soluzione: "+ (System.nanoTime()-xtime));
        System.out.println("|||||| soluzione: "+ solution);

        this.listaMosse = new ArrayList<String>(Arrays.asList(solution.split(" ")));
        this.iteratorMosse = listaMosse.listIterator();
    }

    public void setBounds(float xMin, float xMax, float yMin, float yMax) { //posiziono ivari elementi sullo schermo
        //System.out.println("|||||BOUNDS "+xMin+" "+xMax+" "+yMin+" "+yMin);
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
        Timber.d("Cube %f %f %f %f", xMin, xMax, yMin, yMax);
        float z = 1f;
        float rat = (xMax - xMin) / (yMax - yMin);
        float fRat = (rat + 1f) / 2f;

        float w, h, xl, xr, yb, yt;
        float l = xMin + 0.05f;
        //float r = l + (0.5f * 0.8f) + (0.4f * rat);
        float r = xMax - 0.05f;
        float b = yMin + 0.05f;
        float t = b + 0.6f + (0.4f * rat);
        GLColor white = new GLColor(1, 1, 1);

        float xPadding = (r - l) / 16f;
        float yPadding = (t - b) / 10f;


        //MENU_HEIGHT = (t - b) * 1.5f;
        //MENU_WIDTH = (r - l) * 2.8f;
        MENU_HEIGHT = (t - b);
        MENU_WIDTH = (r - l);


        //menuView.setFace(l, l + MENU_WIDTH, yMin - (0.05f + MENU_HEIGHT), b - 0.02f, z, white);
        menuView.setFace(l, r, b, t, z, white); //prende i vertici left, right, bottom, top

        menuView.setTextureBounds(1f, 1f);

        Vec2 tSize;
        GLColor textColor = new GLColor(0.5f, 0.5f, 0.5f);
        h = ((t - b) - (2*yPadding));

        w = (MENU_WIDTH * 0.5f) - (2*xPadding); //ci sono due bottoni


        prevMove.setFace( l + xPadding, l + xPadding + w, b + yPadding, t - yPadding, z + 0.02f, white);
        prevMove.setTextureBounds(1f, 1f);
        prevMove.setTextColor(textColor);
        prevMove.setTextSize(12f * fRat);
        tSize = font.measureText("Prev. Move", prevMove.textSize);
        //prevMove.setPadding(w / 2f - (tSize.x / 2f + xPadding), 0, 0, h / 2f - tSize.y / 2f);
        prevMove.setPadding(w / 2f - ((tSize.x + 0.02f) / 2f ), 0, 0, h / 2f - tSize.y / 2f);

        nextMove.setFace( l + w + (3*xPadding), l + (3*xPadding) + (2*w) , b + yPadding, t - yPadding, z + 0.02f, white);
        nextMove.setTextureBounds(1f, 1f);
        nextMove.setTextColor(textColor);
        nextMove.setTextSize(12f * fRat);
        tSize = font.measureText("Next Move", nextMove.textSize);
        nextMove.setPadding(w / 2f - ((tSize.x + 0.02f) / 2f ), 0, 0, h / 2f - tSize.y / 2f);

        menuView.addChild(prevMove);
        menuView.addChild(nextMove);

        generate();

        prevMove.setText("Prev. Move");
        nextMove.setText("Next Move");

        restoreTime = 0;
        restoreStartTimer = false;
        restoreOnSetBounds = false;
    }

    public Vec2 screenToWorld(float x, float y) {
        Vec2 p = new Vec2(x * adjustWidth, 1f - y * adjustHeight);
        p.x = p.x * (xMax - xMin) + xMin;
        p.y = p.y * (yMax - yMin) + yMin;
        return p;
    }

    public void draw(GL11 gl) {
        super.draw(gl);
        gl.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        gl.glPushMatrix();

        gl.glShadeModel(GL11.GL_FLAT);

        menuView.animate();
        menuView.draw(gl);


        gl.glPopMatrix();
    }


    private void resetTouch() {
        touchMode = NONE;
        activePtrId = -1;
    }

    public boolean handleTouch(MotionEvent e) {

        // Eventually detect cube hit here
        Vec2 worldCoords;

        final int action = e.getAction();
        switch (action & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:       //schermo premuto
                touchMode = SINGLE_TOUCH;
                activePtrId = e.getPointerId(0);
                x1 = e.getX();
                y1 = e.getY();
                worldCoords = screenToWorld(x1, y1);

                return menuView.handleActionDown(worldCoords);

            case MotionEvent.ACTION_UP:         //schermo rilasciato
                resetTouch();
                worldCoords = screenToWorld(x1, y1);

                return menuView.handleActionUp(worldCoords);

            case MotionEvent.ACTION_CANCEL:
                resetTouch();
                break;

            case MotionEvent.ACTION_MOVE:           //movimento tra ACTION_DOWN e ACTION_UP
                if (touchMode == SINGLE_TOUCH) {
                    final int ptrInd = e.findPointerIndex(activePtrId);
                    float x = e.getX(ptrInd);
                    float y = e.getY(ptrInd);
                    if (touchMode == SINGLE_TOUCH) {
                        worldCoords = screenToWorld(x, y);
                        return menuView.handleActionMove(worldCoords);
                    }
                }
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                touchMode = MULTI_TOUCH;
                break;

            case MotionEvent.ACTION_POINTER_UP:
                if (e.getPointerCount() == 1) {
                    touchMode = SINGLE_TOUCH;
                }
                break;
        }
        return false;
    }

}