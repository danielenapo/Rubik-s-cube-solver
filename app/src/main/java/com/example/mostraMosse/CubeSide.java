package com.example.mostraMosse;

import com.threeDBJ.MGraphicsLib.math.Mat4;
import com.threeDBJ.MGraphicsLib.math.Vec2;
import com.threeDBJ.MGraphicsLib.math.Vec3;

public class CubeSide {

    private static final float EPS = 0.0001f; //machine epsilon

    private int dim;
    int frontFace;
    private Vec3 normal, aPoint;

    private Layer[] hLayers, vLayers;
    private float[] bounds = new float[6];

    public CubeSide(int dim, int frontFace, float xMin, float xMax,
                    float yMin, float yMax, float zMin, float zMax) {
        //
        this.frontFace = frontFace;
        this.dim = dim; //in genere 3
        this.normal = new Vec3(xMin + xMax, yMin + yMax, zMin + zMax);
        this.normal.nor(); //normalizza
        this.aPoint = new Vec3(normal);

        //setto il limite di presa della faccia
        bounds[0] = xMin - EPS;
        bounds[1] = xMax + EPS;
        bounds[2] = yMin - EPS;
        bounds[3] = yMax + EPS;
        bounds[4] = zMin - EPS;
        bounds[5] = zMax + EPS;
    }

    public void setHLayers(Layer[] l) {
        hLayers = l;
    }

    public void setVLayers(Layer[] l) {
        vLayers = l;
    }

    public Layer getHLayer(Vec2 ind) {
        if (frontFace == Cube.kTop) {
            return hLayers[(int) ind.y];
        }
        return hLayers[dim - (int) ind.y - 1];
    }

    public Layer getVLayer(Vec2 ind) {
        if (frontFace == Cube.kRight) {
            return vLayers[dim - (int) ind.x - 1];
        } else if (frontFace == Cube.kBack) {
            return vLayers[dim - (int) ind.x - 1];
        }
        return vLayers[(int) ind.x];
    }

}