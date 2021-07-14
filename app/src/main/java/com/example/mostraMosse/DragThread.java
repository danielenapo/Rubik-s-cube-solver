package com.example.mostraMosse;

import com.threeDBJ.MGraphicsLib.math.Quaternion;
import com.threeDBJ.MGraphicsLib.math.Vec2;
import com.threeDBJ.MGraphicsLib.math.Vec3;

import java.util.HashSet;

public class DragThread extends Thread{
/*
    static final int H = 0, V = 1;
    private int type, fixInd = 21;
    int axis;
    static public final int XAxis = 0;
    static public final int YAxis = 1;
    static public final int ZAxis = 2;
    static public final float PI = (float) Math.PI;
    static public final float PI2 = 2f * PI;
    static public final float HALFPI = PI / 2f;
    float angle = 0f;
    private Vec3 axisVec;
    HashSet<Cube> cubes = new HashSet<Cube>();

    private boolean running =true;
    private Vec2 dir;
    private int face;

    public DragThread(boolean running, Vec2 dir, int face) {
        this.running = running;
        this.dir = dir;
        this.face = face;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
*/

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

        /*float angle;
        if (face == Cube.kRight) {
            angle = ((type == H) ? dir.x : dir.y);
        } else if (face == Cube.kBack) {
            angle = ((type == H) ? dir.x : dir.y);
        } else if (face == Cube.kBottom) {
            angle = ((type == H) ? -1f * dir.x : -1f * dir.y);
        } else {
            angle = ((type == H) ? dir.x : -1f * dir.y);
        }
        if (axis == YAxis && type == H) angle *= -1f;
        setAngle(angle);*/

        CubeView.stopInput=false;
    }

/*
    public void setAngle(float angle) {
        // normalize the angle
        while (angle >= PI) angle -= PI2;
        while (angle < -1f * PI) angle += PI2;
        this.angle += angle;

        Quaternion localRot = new Quaternion(axisVec, angle, true);
        for (Cube cube : cubes) {
            if (cube != null) {
                cube.animateTransform(localRot);
            }
        }
    }*/
    /*public float drag2(Vec2 dir, int face) { //dir.x= dir.y
        //float angle= (float) (Math.PI*0.25);
        float angle;
        if (face == Cube.kRight) {
            angle = ((type == H) ? dir.x : dir.y);
        } else if (face == Cube.kBack) {
            angle = ((type == H) ? dir.x : dir.y);
        } else if (face == Cube.kBottom) {
            angle = ((type == H) ? -1f * dir.x : -1f * dir.y);
        } else {
            angle = ((type == H) ? dir.x : -1f * dir.y);
        }
        if (axis == YAxis && type == H) angle *= -1f;
        setAngle(angle);
        return angle;
    }*/

}
