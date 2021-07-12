package com.example.mostraMosse;

import com.threeDBJ.MGraphicsLib.math.Quaternion;
import com.threeDBJ.MGraphicsLib.math.Vec2;
import com.threeDBJ.MGraphicsLib.math.Vec3;

import java.util.HashSet;

public class Layer {

    RubeCube cube;
    HashSet<Cube> cubes = new HashSet<Cube>();
    //Cube[] cubes;
    // which axis do we rotate around?
    // 0 for X, 1 for Y, 2 for Z
    int axis;
    static public final int XAxis = 0;
    static public final int YAxis = 1;
    static public final int ZAxis = 2;
    static public final float PI = (float) Math.PI;
    static public final float PI2 = 2f * PI;
    static public final float HALFPI = PI / 2f;
    static final int H = 0, V = 1;
    private int type, fixInd = 21;
    int index;
    float angle = 0f;
    private float fixAngle;
    private Vec3 axisVec;

    public Layer(RubeCube cube, Vec3 zero, int axis, int index) {
        this.axis = axis;
        this.axisVec = new Vec3(zero);
        this.cube = cube;
        this.index = index;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void clear() {
        cubes.clear();
    }

    public void add(Cube c) {
        cubes.add(c);
    }

    public void replaceCube(Cube oldCube, Cube newCube) {
        cubes.remove(oldCube);
        cubes.add(newCube);
    }

    public void startAnimation() {
        for (Cube cube : cubes) {
            if (cube != null) {
                cube.startAnimation();
            }
        }
    }

    public void endAnimation() {
        for (Cube cube : cubes) {
            if (cube != null) {
                cube.endAnimation();
            }
        }
    }

    /* Rotates the layer to a stable position. Calls cube.endLayerAnimation
       when finished to update layers and sides. */
    public void animate() {
        if (fixInd < 10) {
            setAngle(fixAngle);
            fixInd += 1;
            if (fixInd == 10) {
                cube.endLayerAnimation(axis, angle, index);
                angle = 0f;
            }
        }
    }

    public void drag(Vec2 dir, int face) { //dir è (1.3f, 1.3f)
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
    }

    public float drag2(Vec2 dir, int face) { //dir.x= dir.y
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
    }



    public void dragEnd() {
        float a = angle % HALFPI;
        if (a > HALFPI / 2f) {
            fixAngle = (HALFPI - a) / 10f;
        } else if (a < -1f * (HALFPI / 2f)) {
            fixAngle = -1f * (HALFPI + a) / 10f;
        } else {
            fixAngle = -1f * a / 10f;
        }
        fixInd = 0;
        cube.spinEnabled(false);
    }

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
    }
}
