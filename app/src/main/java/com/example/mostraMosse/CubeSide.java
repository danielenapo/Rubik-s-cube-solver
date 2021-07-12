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

    private Vec3 getNormal(Mat4 rotation) {
        Vec3 temp = new Vec3(normal);
        return temp.rot(rotation);
    }

    private Vec3 getPointOnPlane(Mat4 rotation) {
        Vec3 temp = new Vec3(aPoint);
        return temp.rot(rotation);
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

    private Vec2 getPlaneValues(Vec3 v) {
        switch (frontFace) {
            case Cube.kFront:
                return new Vec2(v.x, v.y);
            case Cube.kBack:
                return new Vec2(1f - v.x, v.y);
            case Cube.kLeft:
                return new Vec2(v.z, v.y);
            case Cube.kRight:
                return new Vec2(1f - v.z, v.y);
            case Cube.kTop:
                return new Vec2(v.x, 1f - v.z);
            case Cube.kBottom:
                return new Vec2(v.x, v.z);
        }
        return null;
    }

    /* Returns the hit point on the plane containing this side, regardless of
       whether the side was hit */
    public Vec2 getPlaneHitLoc(Vec3 start, Vec3 dir, Mat4 rotation) {
        Vec3 norm = getNormal(rotation);
        float denom = dir.dot(norm);
        Vec3 p = getPointOnPlane(rotation);
        p.sub(start);
        float d = p.dot(norm) / denom;
        Vec3 hp = dir.mul(d).add(start);
        Mat4 rotInv = new Mat4(rotation);
        rotInv.inv();
        hp.rot(rotInv);
        hp.x = (hp.x + 1f) / 2f;
        hp.y = (hp.y + 1f) / 2f;
        hp.z = (hp.z + 1f) / 2f;
        Vec2 v = getPlaneValues(hp);
        if(v != null) {
            v.x = (v.x * (float) dim);
            v.y = ((1f - v.y) * (float) dim);
        }
        return v;
    }

    /* Gets the point on this side pointed to by a vector begining at start
       in the direction of dir. Returns null if the side is not hit. */
    private Vec3 hitPoint(Vec3 start, Vec3 dir, Mat4 rotation) {
        Vec3 norm = getNormal(rotation);
        float denom = dir.dot(norm);
        if (denom <= 0f) return null;
        Vec3 p = getPointOnPlane(rotation);
        p.sub(start);
        float d = p.dot(norm) / denom;

        return dir.mul(d).add(start);
    }
/*
    public Vec2 getHitLoc(Vec3 start, Vec3 dir, Mat4 rotation) { //questa funzione è inutile perchè l'utente non deve essere in grado di girare le facce al tocco
        Vec3 hp = hitPoint(start, dir, rotation);
        if (hp == null) return null;
        Mat4 rotInv = new Mat4(rotation);
        rotInv.inv();
        hp.rot(rotInv);
        if (hp.x >= bounds[0] && hp.x <= bounds[1] && hp.y >= bounds[2] &&
                hp.y <= bounds[3] && hp.z >= bounds[4] && hp.z <= bounds[5]) {
            hp.x = (hp.x + 1f) / 2f;
            hp.y = (hp.y + 1f) / 2f;
            hp.z = (hp.z + 1f) / 2f;
            Vec2 v = getPlaneValues(hp);
            v.x = (v.x * (float) dim);
            v.y = ((1f - v.y) * (float) dim);
            return v;
        }
        return null;
    }
*/
}