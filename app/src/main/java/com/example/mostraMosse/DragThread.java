package com.example.mostraMosse;

import com.threeDBJ.MGraphicsLib.math.Quaternion;
import com.threeDBJ.MGraphicsLib.math.Vec2;
import com.threeDBJ.MGraphicsLib.math.Vec3;

import java.util.HashSet;

public class DragThread extends Thread{


    private float angle;
    private  boolean orario;
    private Layer curLayer;
    private CubeSide curSide;

    public DragThread(boolean orario, Layer curLayer, CubeSide curSide) {
        this.orario = orario;
        this.curLayer = curLayer;
        this.curSide = curSide;
    }

    @Override
    public void run(){

        Vec2 vel = new Vec2();
        //tieni x=y
        vel.x=0.05f; //se cambi il segno cambia verso di rotazione
        vel.y=0.05f;

        if(orario==false){ //se antiorario
            vel.x=-vel.x;
            vel.y=-vel.y;
        }

        angle=0;
        do {
            angle += curLayer.drag2(vel, curSide.frontFace);
            //System.out.println(Math.abs(angle) + "---" + Math.PI * 0.25);

            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }while(Math.abs(angle) <= Math.PI * 0.5);

        curLayer.dragEnd();

        CubeView.stopInput=false;
    }

}
