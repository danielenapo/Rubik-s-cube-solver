package com.example.mostraMosse;

import com.threeDBJ.MGraphicsLib.GLFace;
import com.threeDBJ.MGraphicsLib.GLShape;
import com.threeDBJ.MGraphicsLib.GLVertex;
import com.threeDBJ.MGraphicsLib.math.Vec3;

public class Cube extends GLShape {
    //numerazione facce
    public static final int kBottom = 0;
    public static final int kFront = 1;
    public static final int kLeft = 2;
    public static final int kRight = 3;
    public static final int kBack = 4;
    public static final int kTop = 5;

    public int id;
    public Vec3 normal; //vettore tridimensionale di float (da OpenGL)

    public Cube(GLWorld world, float left, float bottom,
                float back, float right, float top, float front) {
        super(world);

        //istanzio i vertici del cubo
        GLVertex lbBack = new GLVertex(left, bottom, back);
        GLVertex rbBack = new GLVertex(right, bottom, back);
        GLVertex ltBack = new GLVertex(left, top, back);
        GLVertex rtBack = new GLVertex(right, top, back);
        GLVertex lbFront = new GLVertex(left, bottom, front);
        GLVertex rbFront = new GLVertex(right, bottom, front);
        GLVertex ltFront = new GLVertex(left, top, front);
        GLVertex rtFront = new GLVertex(right, top, front);

        //creo le facce del cubo, dati i vertici
        // Bottom
        addCubeSide(rbBack, rbFront, lbBack, lbFront);
        // Front
        addCubeSide(rbFront, rtFront, lbFront, ltFront);
        // Left
        addCubeSide(lbFront, ltFront, lbBack, ltBack);
        // Right
        addCubeSide(rbBack, rtBack, rbFront, rtFront);
        // Back
        addCubeSide(lbBack, ltBack, rbBack, rtBack);
        // Top
        addCubeSide(rtFront, rtBack, ltFront, ltBack);
        for (GLFace f : getFaceList()) {
            f.setTexture(getEnv().texture);
        }
    }

    private void addCubeSide(GLVertex rb, GLVertex rt, GLVertex lb, GLVertex lt) {
        addFace(new GLFace(addVertex(rb), addVertex(rt), addVertex(lb), addVertex(lt))); //metodo di MGraphicsLib (OpenGL)
    }

}