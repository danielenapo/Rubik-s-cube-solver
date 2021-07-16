package com.example.mostraMosse;

import android.content.SharedPreferences;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.VelocityTracker;


import com.example.mostraMosse.util.Util;
import com.threeDBJ.MGraphicsLib.GLColor;
import com.threeDBJ.MGraphicsLib.math.Quaternion;
import com.threeDBJ.MGraphicsLib.math.Vec2;
import com.threeDBJ.MGraphicsLib.math.Vec3;

import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

import timber.log.Timber;



public class RubeCube {

    public static float MAX_SPIN_RATE = 0.08f; //velocità con cui si girano le facce

    private final Handler handler = new Handler();
    GLWorld world;
    private CubeRenderer mRenderer;
    private Cube[][][] cubes;
    private CubeSide[] cubeSides = new CubeSide[6];
    private int[][][] faceColors;
    private CubeSide front, back, left, right, top, bottom, curSide;
    private Layer[] lx, ly, lz;
    private Layer curLayer;
    private Vec2 hitVec, dragVec, dir = new Vec2();
    private boolean spinEnabled = false; //disabilito la capacità di girare le facce per l'utente
    private GLColor[] colors = new GLColor[6];
    private float angle=0;
    private char[] configurazione;
    //private boolean doDrag2=true;

    // for random cube movements
    private final Random random = new Random(System.currentTimeMillis());

    private float x1 = 0, y1 = 0, cubeSize, space;

    private static int NONE = 0, DRAG = 1, ZOOM = 2, SPIN = 3;
    private final float TOUCH_SCALE_FACTOR = (float) Math.PI / 180;

    private int mode = NONE, activePtrId = -1, dim;
    private VelocityTracker velocityTracker;

    public RubeCube(GLWorld world, int dim) {
        this.dim = dim;
        this.world = world;
        colors[Cube.kTop] = new GLColor(1f, 1f, 1f); //bianco
        colors[Cube.kFront] = new GLColor(0, 1f, 0); //verde
        colors[Cube.kBack] = new GLColor(0, 0, 1f); //blu
        colors[Cube.kLeft] = new GLColor(1f, 0.5f, 0); //arancione
        colors[Cube.kBottom] = new GLColor(1f, 1f, 0); //giallo
        colors[Cube.kRight] = new GLColor(1f,0 , 0); //rosso
        setup();
    }

    public RubeCube(GLWorld world, int dim, char [] configurazione) {
        this.dim = dim;
        this.world = world;
        this.configurazione=configurazione;
        colors[Cube.kTop] = new GLColor(1f, 1f, 1f);    //bianco
        colors[Cube.kFront] = new GLColor(0, 1f, 0);    //verde
        colors[Cube.kBack] = new GLColor(0, 0, 1f);     //blu
        colors[Cube.kLeft] = new GLColor(1f, 0.5f, 0);  //arancione
        colors[Cube.kBottom] = new GLColor(1f, 1f, 0);  //giallo
        colors[Cube.kRight] = new GLColor(1f,0 , 0);    //rosso
        setup();
    }


    public void setup() {
        faceColors = new int[6][dim][dim];
        lx = new Layer[dim];
        ly = new Layer[dim];
        lz = new Layer[dim];
        cubes = new Cube[dim][dim][dim];
        back = new CubeSide(dim, Cube.kBack, -1f, 1f, -1f, 1f, -1f, -1f);
        front = new CubeSide(dim, Cube.kFront, -1f, 1f, -1f, 1f, 1f, 1f);
        left = new CubeSide(dim, Cube.kLeft, -1f, -1f, -1f, 1f, -1f, 1f);
        right = new CubeSide(dim, Cube.kRight, 1f, 1f, -1f, 1f, -1f, 1f);
        bottom = new CubeSide(dim, Cube.kBottom, -1f, 1f, -1f, -1f, -1f, 1f);
        top = new CubeSide(dim, Cube.kTop, -1f, 1f, 1f, 1f, -1f, 1f);
        cubeSides[Cube.kFront] = front;
        cubeSides[Cube.kBack] = back;
        cubeSides[Cube.kLeft] = left;
        cubeSides[Cube.kRight] = right;
        cubeSides[Cube.kBottom] = bottom;
        cubeSides[Cube.kTop] = top;
    }

    public char[] getConfigurazione() {
        return configurazione;
    }

    public void setRenderer(CubeRenderer mRenderer) {
        this.mRenderer = mRenderer;
    }

    public void init() {
        addShapes(world);
        initSideColors();
        setupLayers();
    }

    private void addShapes(GLWorld world) {
        float curX, curY, curZ;
        curX = curY = curZ = -1f;
        // TODO -- scale with dim
        space = 1f / (dim * 15f);
        cubeSize = (2f - ((float) dim - 1f) * space) / (float) dim;
        // Add cubes and layers
        int i, j, k, n = 0;
        float xleft, ybot, zback, x, y, z;
        x = y = z = 0f;
        z = curZ;
        for (k = 0; k < dim; k += 1) {
            y = curY;
            for (j = 0; j < dim; j += 1) {
                x = curX;
                for (i = 0; i < dim; i += 1) {
                    xleft = x;
                    ybot = y;
                    zback = z;
                    Cube c = new Cube(world, xleft, ybot, zback,
                            xleft + cubeSize, ybot + cubeSize, zback + cubeSize);
                    cubes[k][j][i] = c;
                    n += 1;
                    x += cubeSize + space;
                    world.addShape(c);
                }
                y += cubeSize + space;
            }
            z += cubeSize + space;
        }

        // Paint all sides black by default
        GLColor black = new GLColor(0, 0, 0, 1f);
        for (i = 0; i < dim; i += 1) {
            for (j = 0; j < dim; j += 1) {
                for (k = 0; k < dim; k += 1) {
                    Cube cube = cubes[i][j][k];
                    for (int w = 0; w < 6; w += 1) {
                        cube.setFaceColorAll(w, black);
                    }
                }
            }
        }
        for (i = 0; i < dim; i += 1) {
            lz[i] = new Layer(this, new Vec3(0f, 0f, curZ), Layer.ZAxis, i);
            lx[i] = new Layer(this, new Vec3(curX, 0f, 0f), Layer.XAxis, i);
            ly[i] = new Layer(this, new Vec3(0f, curY, 0f), Layer.YAxis, i);
        }
    }

    void printFaceColors(String prepend) {
        StringBuilder colors = new StringBuilder();
        for (int i = 0; i < faceColors.length; i += 1) {
            for (int j = 0; j < dim; j += 1) {
                for (int k = 0; k < dim; k += 1) {
                    colors.append(i).append(' ');
                }
            }
        }
        Timber.d("%s: %s", prepend, colors);
    }

    private void initSideColors() {
        int i, j, k;
        for (i = 0; i < faceColors.length; i += 1) {
            for (j = 0; j < dim; j += 1) {
                for (k = 0; k < dim; k += 1) {
                    faceColors[i][j][k] = i;
                }
            }
        }
    }

    void setupSides() {         //disegna facce con colori default (cubo completato)
        int i, j, k;
        // Paint back
        i = 0;
        for (j = 0; j < dim; j += 1) {
            for (k = 0; k < dim; k += 1) {
                cubes[i][j][k].setFaceColor(Cube.kBack, colors[faceColors[Cube.kBack][j][k]]);
            }
        }

        // Paint front
        i = dim - 1;
        for (j = 0; j < dim; j += 1) {
            for (k = 0; k < dim; k += 1) {
                cubes[i][j][k].setFaceColor(Cube.kFront, colors[faceColors[Cube.kFront][j][k]]);
            }
        }

        // Paint right .
        k = dim - 1;
        for (i = 0; i < dim; i += 1) {
            for (j = 0; j < dim; j += 1) {
                cubes[i][j][k].setFaceColor(Cube.kRight, colors[faceColors[Cube.kRight][i][j]]);
            }
        }

        // Paint bottom
        j = 0;
        for (i = 0; i < dim; i += 1) {
            for (k = 0; k < dim; k += 1) {
                cubes[i][j][k].setFaceColor(Cube.kBottom, colors[faceColors[Cube.kBottom][i][k]]);
            }
        }

        // Paint top
        j = dim - 1;
        for (i = 0; i < dim; i += 1) {
            for (k = 0; k < dim; k += 1) {
                cubes[i][j][k].setFaceColor(Cube.kTop, colors[faceColors[Cube.kTop][i][k]]);
            }
        }
        // Paint left
        k = 0;
        for (i = 0; i < dim; i += 1) {
            for (j = 0; j < dim; j += 1) {
                cubes[i][j][k].setFaceColor(Cube.kLeft, colors[faceColors[Cube.kLeft][i][j]]);
            }
        }
    }


    GLColor traslColor(char c){
        switch (c){
            case 'w':
                return new GLColor(1f,1f,1f);

            case 'b':
                return new GLColor(0,0,1f);

            case 'r':
                return new GLColor(1f,0,0);

            case 'g':
                return new GLColor(0,1f,0);

            case 'y':
                return new GLColor(1f,1f,0);

            case 'o':
                return new GLColor(1f, 0.5f, 0);

            default:
                return new GLColor(0,0,0);
        }
    }

    void configura(char [] configurazione ) {
        int i, j, k, c;

        //long xtime= System.nanoTime();
        c=0;
        GLColor[] configGL = new GLColor[54];
        for(int x=0; x<configurazione.length; x++){
            configGL[x]=traslColor(configurazione[x]);
        }

        //System.out.println("DEBUG "+ (System.nanoTime()-xtime));

        // Paint front
        i = dim - 1;
        for (j = dim -1; j>=0 ; j -= 1) {
            for (k = 0; k <dim; k += 1) {
                cubes[i][j][k].setFaceColor(Cube.kFront, configGL[c]);
                c++;
            }
        }

        // Paint right .
        k = dim - 1;
        for (j = dim-1; j >=0; j -= 1) {
            for (i = dim-1 ; i >=0; i -= 1) {
                cubes[i][j][k].setFaceColor(Cube.kRight, configGL[c]);
                c++;
            }
        }


        // Paint back
        i = 0;
        for (j = dim-1; j >=0; j -= 1) {
            for (k = dim-1; k >=0; k -= 1) {
                cubes[i][j][k].setFaceColor(Cube.kBack, configGL[c]);
                c++;
            }
        }

        // Paint left
        k = 0;
        for (j = dim-1; j >=0; j -= 1) {
            for (i = 0; i < dim; i += 1) {
                cubes[i][j][k].setFaceColor(Cube.kLeft, configGL[c]);
                c++;
            }
        }

        // Paint top
        j = dim - 1;
        for (i = 0; i < dim; i += 1) {
            for (k = 0; k < dim; k += 1) {
                cubes[i][j][k].setFaceColor(Cube.kTop, configGL[c]);
                c++;
            }
        }

        // Paint bottom
        j = 0;
        for (i = dim-1; i >=0; i -= 1) {
            for (k = 0; k < dim; k += 1) {
                cubes[i][j][k].setFaceColor(Cube.kBottom, configGL[c]);
                c++;
            }
        }



    }

    private void setupLayers() {
        float curX, curY, curZ;
        curX = curY = curZ = -1f;
        // Record z layer
        curZ += (cubeSize) / 2f;
        int i, j, k;
        for (i = 0; i < dim; i += 1) {
            lz[i].clear();
            curZ += cubeSize + space;
            for (j = 0; j < dim; j += 1) {
                for (k = 0; k < dim; k += 1) {
                    lz[i].add(cubes[i][j][k]);
                }
            }
        }
        top.setHLayers(lz);
        bottom.setHLayers(lz);
        left.setVLayers(lz);
        right.setVLayers(lz);
        // Record x layer
        curX += (cubeSize) / 2f;
        for (k = 0; k < dim; k += 1) {
            lx[k].clear();
            curX += cubeSize + space;
            for (j = 0; j < dim; j += 1) {
                for (i = 0; i < dim; i += 1) {
                    lx[k].add(cubes[i][j][k]);
                }
            }
        }
        front.setVLayers(lx);
        back.setVLayers(lx);
        top.setVLayers(lx);
        bottom.setVLayers(lx);
        // Record y layer and register the cubes with the world
        curY += (cubeSize) / 2f;
        for (j = 0; j < dim; j += 1) {
            ly[j].clear();
            curY += cubeSize + space;
            for (i = 0; i < dim; i += 1) {
                for (k = 0; k < dim; k += 1) {
                    ly[j].add(cubes[i][j][k]);
                }
            }
        }
        front.setHLayers(ly);
        back.setHLayers(ly);
        left.setHLayers(ly);
        right.setHLayers(ly);
    }

    private Vec3 getRatio(float x, float y) {
        Vec3 w = new Vec3();
        w.x = x * world.adjustWidth;
        w.y = 1f - y * world.adjustHeight;
        w.z = 2f;
        return w;
    }

    public void animate() {
        for (int i = 0; i < dim; i += 1) {
            lx[i].animate();
            ly[i].animate();
            lz[i].animate();
        }
    }





    private void transposeColorSidePos(int side) {
        int[][] newSide = new int[dim][dim];
        for (int i = 0; i < dim; i += 1) {
            for (int j = 0; j < dim; j += 1) {
                newSide[i][j] = faceColors[side][j][dim - i - 1];
            }
        }
        faceColors[side] = newSide;
    }

    private void transposeColorSideNeg(int side) {
        int[][] newSide = new int[dim][dim];
        for (int i = 0; i < dim; i += 1) {
            for (int j = 0; j < dim; j += 1) {
                newSide[i][j] = faceColors[side][dim - j - 1][i];
            }
        }
        faceColors[side] = newSide;
    }

    private void transposeColorsXPos(int index) {
        int temp1, temp2;
        for (int i = 0; i < dim; i += 1) {
            temp1 = faceColors[Cube.kFront][dim - i - 1][index];
            faceColors[Cube.kFront][dim - i - 1][index] = faceColors[Cube.kBottom][dim - i - 1][index];
            temp2 = faceColors[Cube.kTop][i][index];
            faceColors[Cube.kTop][i][index] = temp1;
            temp1 = faceColors[Cube.kBack][i][index];
            faceColors[Cube.kBack][i][index] = temp2;
            faceColors[Cube.kBottom][dim - i - 1][index] = temp1;
        }
        if (index == 0) {
            transposeColorSidePos(Cube.kLeft);
        } else if (index == dim - 1) {
            transposeColorSidePos(Cube.kRight);
        }
    }

    private void transposeColorsXNeg(int index) {
        int temp1, temp2;
        for (int i = 0; i < dim; i += 1) {
            temp1 = faceColors[Cube.kFront][dim - i - 1][index];
            faceColors[Cube.kFront][dim - i - 1][index] = faceColors[Cube.kTop][i][index];
            temp2 = faceColors[Cube.kBottom][dim - i - 1][index];
            faceColors[Cube.kBottom][dim - i - 1][index] = temp1;
            temp1 = faceColors[Cube.kBack][i][index];
            faceColors[Cube.kBack][i][index] = temp2;
            faceColors[Cube.kTop][i][index] = temp1;
        }
        if (index == 0) {
            transposeColorSideNeg(Cube.kLeft);
        } else if (index == dim - 1) {
            transposeColorSideNeg(Cube.kRight);
        }
    }

    private void transposeColorsYPos(int index) {
        int temp1, temp2;
        for (int i = 0; i < dim; i += 1) {
            temp1 = faceColors[Cube.kFront][index][i];
            faceColors[Cube.kFront][index][i] = faceColors[Cube.kRight][dim - (i + 1)][index];
            temp2 = faceColors[Cube.kLeft][i][index];
            faceColors[Cube.kLeft][i][index] = temp1;
            temp1 = faceColors[Cube.kBack][index][dim - i - 1];
            faceColors[Cube.kBack][index][dim - i - 1] = temp2;
            faceColors[Cube.kRight][dim - (i + 1)][index] = temp1;
        }
        if (index == 0) {
            transposeColorSideNeg(Cube.kBottom);
        } else if (index == dim - 1) {
            transposeColorSideNeg(Cube.kTop);
        }
    }

    private void transposeColorsYNeg(int index) {
        int temp1, temp2;
        // Stopped here, figure out the side transposition
        for (int i = 0; i < dim; i += 1) {
            temp1 = faceColors[Cube.kFront][index][i];
            faceColors[Cube.kFront][index][i] = faceColors[Cube.kLeft][i][index];
            temp2 = faceColors[Cube.kRight][dim - (i + 1)][index];
            faceColors[Cube.kRight][dim - (i + 1)][index] = temp1;
            temp1 = faceColors[Cube.kBack][index][dim - i - 1];
            faceColors[Cube.kBack][index][dim - i - 1] = temp2;
            faceColors[Cube.kLeft][i][index] = temp1;
        }
        if (index == 0) {
            transposeColorSidePos(Cube.kBottom);
        } else if (index == dim - 1) {
            transposeColorSidePos(Cube.kTop);
        }
    }

    private void transposeColorsZNeg(int index) {
        int temp1, temp2;
        for (int i = 0; i < dim; i += 1) {
            temp1 = faceColors[Cube.kTop][index][i];
            faceColors[Cube.kTop][index][i] = faceColors[Cube.kRight][index][dim - i - 1];
            temp2 = faceColors[Cube.kLeft][index][i];
            faceColors[Cube.kLeft][index][i] = temp1;
            temp1 = faceColors[Cube.kBottom][index][dim - i - 1];
            faceColors[Cube.kBottom][index][dim - i - 1] = temp2;
            faceColors[Cube.kRight][index][dim - i - 1] = temp1;
        }
        if (index == 0) {
            transposeColorSideNeg(Cube.kBack);
        } else if (index == dim - 1) {
            transposeColorSideNeg(Cube.kFront);
        }
    }

    private void transposeColorsZPos(int index) {
        int temp1, temp2;
        for (int i = 0; i < dim; i += 1) {
            temp1 = faceColors[Cube.kTop][index][i];
            faceColors[Cube.kTop][index][i] = faceColors[Cube.kLeft][index][i];
            temp2 = faceColors[Cube.kRight][index][dim - i - 1];
            faceColors[Cube.kRight][index][dim - i - 1] = temp1;
            temp1 = faceColors[Cube.kBottom][index][dim - i - 1];
            faceColors[Cube.kBottom][index][dim - i - 1] = temp2;
            faceColors[Cube.kLeft][index][i] = temp1;
        }
        if (index == 0) {
            transposeColorSidePos(Cube.kBack);
        } else if (index == dim - 1) {
            transposeColorSidePos(Cube.kFront);
        }
    }

    private void transposeCubes(int nTurns, int axis, int index) {
        Cube[][] t = new Cube[dim][dim];
        int n;
        if (nTurns > 0) {
            n = -1;
        } else {
            n = 1;
        }
        while (nTurns != 0) {
            switch (axis) {
                case Layer.XAxis:
                    if (nTurns < 0) {
                        transposeColorsXNeg(index);
                    } else {
                        transposeColorsXPos(index);
                    }
                    break;
                case Layer.YAxis:
                    if (nTurns < 0) {
                        transposeColorsYNeg(index);
                    } else {
                        transposeColorsYPos(index);
                    }
                    break;
                case Layer.ZAxis:
                    if (nTurns < 0) {
                        transposeColorsZNeg(index);
                    } else {
                        transposeColorsZPos(index);
                    }
                    break;
            }
            for (int i = 0; i < dim; i += 1) {
                for (int j = 0; j < dim; j += 1) {
                    switch (axis) {
                        case Layer.XAxis:
                            if (nTurns < 0) {
                                t[i][j] = cubes[dim - j - 1][i][index];
                            } else {
                                t[i][j] = cubes[j][dim - i - 1][index];
                            }
                            break;
                        case Layer.YAxis:
                            if (nTurns < 0) {
                                t[i][j] = cubes[j][index][dim - i - 1];
                            } else {
                                t[i][j] = cubes[dim - j - 1][index][i];
                            }
                            break;
                        case Layer.ZAxis:
                            if (nTurns < 0) {
                                t[i][j] = cubes[index][dim - j - 1][i];
                            } else {
                                t[i][j] = cubes[index][j][dim - i - 1];
                            }
                            break;
                    }
                }
            }
            for (int i = 0; i < dim; i += 1) {
                for (int j = 0; j < dim; j += 1) {
                    switch (axis) {
                        case Layer.XAxis:
                            cubes[i][j][index] = t[i][j];
                            break;
                        case Layer.YAxis:
                            cubes[i][index][j] = t[i][j];
                            break;
                        case Layer.ZAxis:
                            cubes[index][i][j] = t[i][j];
                    }
                }
            }
            nTurns += n;
        }
    }

    /* Update layers and sides after a rotation */
    public void endLayerAnimation(int axis, float angle, int index) {
        int nTurns = (int) (angle / (Layer.HALFPI - 0.01f));
        transposeCubes(nTurns, axis, index);


        // world.pauseCube(true);
        // world.clear();
        // addShapes(world);
        // setupSides();
        // world.generate();
        // world.pauseCube(false);


        setupLayers();
        //setupSides();
        curSide = null;
        curLayer = null;
        dir = new Vec2();
        spinEnabled(true);
    }

    public void spinEnabled(boolean spin) {
        this.spinEnabled = spin;
    }

    public void handleTouch(MotionEvent e) {

        // Eventually detect cube hit here
        final int action = e.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {                 //tocco schermo
                if(velocityTracker == null) {
                    velocityTracker = VelocityTracker.obtain();
                } else {
                    velocityTracker.clear();
                }
                velocityTracker.addMovement(e);
                x1 = e.getX();
                y1 = e.getY();
                mode = DRAG;
                activePtrId = e.getPointerId(0);
                world.dragStart(x1, y1);
                Vec3 coords = mRenderer.screenToWorld(getRatio(x1, y1));

                break;
            }
            case MotionEvent.ACTION_MOVE: {                     //trascino dito sullo schermo
                velocityTracker.addMovement(e);
                if (activePtrId < 0 || activePtrId >= e.getPointerCount()) break;
                final int ptrInd = e.findPointerIndex(activePtrId);
                float x2 = e.getX(ptrInd);
                float y2 = e.getY(ptrInd);
                float dx = x2 - x1;
                float dy = y2 - y1;
                if (mode == DRAG) {

                    world.drag(x2, y2);

                    x1 = x2;
                    y1 = y2;
                }

                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                if (curLayer != null && mode == SPIN) {
                    curLayer.dragEnd();
                }
                float x1 = e.getX(0);
                float x2 = e.getX(1);
                float y1 = e.getY(0);
                float y2 = e.getY(1);
                float xdist1 = Math.abs(x1 - x2);
                float ydist1 = Math.abs(y1 - y2);
                if (xdist1 > 5f && ydist1 > 5f) {
                    //zdist = (float) Math.sqrt(x1 * x2 + y1 * y2);
                    mode = ZOOM;
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                dragEnd(e);
                break;

            case MotionEvent.ACTION_POINTER_UP: {
                // Back to translate
                final int ptrInd = (action & MotionEvent.ACTION_POINTER_INDEX_MASK)
                        >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int ptrId = e.getPointerId(ptrInd);
                final int nPtrInd = ptrInd == 0 ? 1 : 0;
                x1 = e.getX(nPtrInd);
                y1 = e.getY(nPtrInd);
                mode = DRAG;
                world.dragStart(x1, y1);
                activePtrId = e.getPointerId(nPtrInd);
                break;
            }
        }
    }

    private void dragEnd(MotionEvent e) {
        velocityTracker.addMovement(e);
        velocityTracker.computeCurrentVelocity(15, 120f);

        if (mode == DRAG) {
            world.dragEnd(e.getX(), e.getY(), velocityTracker.getXVelocity(), velocityTracker.getYVelocity());
        }
        activePtrId = -1;
        mode = NONE;
        if (curLayer != null) {
            curLayer.dragEnd();
        }
    }

    private final Runnable resetWorld = new Runnable() {
        public void run() {
            GLWorld temp = new GLWorld();
            addShapes(temp);
            initSideColors();
            setupLayers();
            //char[] prova={'w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w'};
            //configura(prova);
            //setupSides();
            //char[] config=CubeRenderer.configurazione;

            configura(configurazione);
            temp.generate();
            world.shapeList = temp.shapeList;
            world.vertexList = temp.vertexList;
            world.vertexBuffer = temp.vertexBuffer;
            world.indexBuffer = temp.indexBuffer;
            world.colorBuffer = temp.colorBuffer;
        }
    };



    public void save(SharedPreferences prefs) {
        SharedPreferences.Editor edit = prefs.edit();
        int i, j, k;
        for (i = 0; i < faceColors.length; i += 1) {
            for (j = 0; j < dim; j += 1) {
                for (k = 0; k < dim; k += 1) {
                    edit.putInt(i + "" + j + "" + k, faceColors[i][j][k]);
                }
            }
        }
        Quaternion rotation = world.getRotation();
        edit.putFloat("Rx", rotation.x);
        edit.putFloat("Ry", rotation.y);
        edit.putFloat("Rz", rotation.z);
        edit.putFloat("Rw", rotation.w);
        edit.apply();
        Util.saveDimension(prefs, dim);
    }

    public void restore(SharedPreferences prefs) {
        int i, j, k;
        int colorSum = 0;
        for (i = 0; i < faceColors.length; i += 1) {
            for (j = 0; j < dim; j += 1) {
                for (k = 0; k < dim; k += 1) {
                    int color = prefs.getInt(i + "" + j + "" + k, i);
                    faceColors[i][j][k] = color;
                    colorSum += color;
                }
            }
            Timber.d("Restore color %d00 %d", i, faceColors[i][0][0]);
        }
        if(colorSum == 0) {
            // TODO -- this is a hack, figure out why prefs get deleted sometimes
            initSideColors();
        }
        //char[] prova={'w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w'};
        //setupSides();
        configura(configurazione);
        Quaternion rotation = new Quaternion(prefs.getFloat("Rx", 0f), prefs.getFloat("Ry", 0f),
                prefs.getFloat("Rz", 0f), prefs.getFloat("Rw", 0f));
        world.rotateBy(rotation);
    }


    public void prova() {
        curSide = cubeSides[0];
        Vec2 nlinea= new Vec2();
        nlinea.x=0; //per layer verticali
        nlinea.y=0; //per layer orizzontali
/*
            //per mosse orizzontali
            Layer ayer = curSide.getHLayer(nlinea);
            ayer.setType(Layer.H);
*/

            //per mosse verticali
            Layer ayer = curSide.getVLayer(nlinea);
            ayer.setType(Layer.V);



        Vec2 vel = new Vec2();
        //tieni x=y
        vel.x=-0.05f; //se cambi il segno cambia verso di rotazione
        vel.y=-0.05f;

        angle=0;
        do {
            angle += ayer.drag2(vel, curSide.frontFace);
                //System.out.println(Math.abs(angle) + "---" + Math.PI * 0.25);

            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }while(Math.abs(angle) <= Math.PI * 0.5);

        ayer.dragEnd();

    }

    public void mossa(int side, int layer, int vh, boolean orario){


        curSide = cubeSides[side];

        Vec2 nlinea= new Vec2();
        if(vh==Layer.V){
            nlinea.x=layer; //per layer verticali
            nlinea.y=0; //per layer orizzontali
            curLayer= curSide.getVLayer(nlinea);
            curLayer.setType(Layer.V);
        }
        else if(vh==Layer.H){
            nlinea.x=0; //per layer verticali
            nlinea.y=layer; //per layer orizzontali
            curLayer= curSide.getHLayer(nlinea);
            curLayer.setType(Layer.H);
        }
///////////////////////////
        DragThread dt= new DragThread(orario, curLayer,curSide);
        dt.start();
        /*/////////////////////
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
/////////////////////////////////////////*/
    }

    public void tradMossa(String mossa, boolean forward) throws InterruptedException {


        switch (mossa){
            case "L":
                mossa(Cube.kFront,0,Layer.V, forward);
                break;
            case "L'":
                mossa(Cube.kFront,0,Layer.V,!forward);
                break;
            /*case "L2":
                mossa(Cube.kFront,0,Layer.V,forward);
                Thread.sleep(500);
                mossa(Cube.kFront,0,Layer.V,forward);
                break;
             */
            case "R":
                forward=!forward;
                mossa(Cube.kFront,2,Layer.V,forward);
                break;
            case "R'":
                forward=!forward;
                mossa(Cube.kFront,2,Layer.V,!forward);
                break;
            /*case "R2":
                forward=!forward;
                mossa(Cube.kFront,2,Layer.V,forward);
                Thread.sleep(500);
                mossa(Cube.kFront,2,Layer.V,forward);
                break;
             */

            case "U":
                forward=!forward;
                mossa(Cube.kFront,0,Layer.H,forward);
                break;
            case "U'":
                forward=!forward;
                mossa(Cube.kFront,0,Layer.H,!forward);
                break;
            /*case "U2":
                forward=!forward;
                mossa(Cube.kFront,0,Layer.H,forward);
                Thread.sleep(500);
                mossa(Cube.kFront,0,Layer.H,forward);
                break;
             */
            case "D":
                mossa(Cube.kFront,2,Layer.H,forward);
                break;
            case "D'":
                mossa(Cube.kFront,2,Layer.H,!forward);
                break;
            /*case "D2":
                mossa(Cube.kFront,2,Layer.H,forward);
                Thread.sleep(500);
                mossa(Cube.kFront,2,Layer.H,forward);
                break;
             */

            case "F":
                forward=!forward;
                mossa(Cube.kLeft,2,Layer.V,forward);
                break;
            case "F'":
                forward=!forward;
                mossa(Cube.kLeft,2,Layer.V,!forward);
                break;
            /*case "F2":
                forward=!forward;
                mossa(Cube.kLeft,2,Layer.V,forward);
                Thread.sleep(500);
                mossa(Cube.kLeft,2,Layer.V,forward);
                break;
             */
            case "B":
                mossa(Cube.kLeft,0,Layer.V,forward);
                break;
            case "B'":
                mossa(Cube.kLeft,0,Layer.V,!forward);
                break;
            /*case "B2":
                mossa(Cube.kLeft,0,Layer.V,forward);
                Thread.sleep(500);
                mossa(Cube.kLeft,0,Layer.V,forward);
                break;
            */
        }
    }

    public static String replaceDoubleMove(String solution){
        String strOut;
        char mossa;
        //System.out.println("|||DEBUG||| soluzione0: "+solution);
        //trovo gli indici dei "2"
        int lastIndex = 0;
        //ArrayList<Integer> result = new ArrayList<Integer>();
        while(lastIndex != -1) {

            lastIndex = solution.indexOf("2",lastIndex);

            if(lastIndex != -1){
                //sostituisco le mosse doppie con la stessa mossa singola ripetuta due volte
                //result.add(lastIndex);
                mossa=solution.charAt(lastIndex-1);
                //System.out.println("|||DEBUG||| indice 2: "+lastIndex);
                solution = solution.substring(0, lastIndex) +" "+ mossa+ solution.substring(lastIndex + 1);
                //System.out.println("|||DEBUG||| soluzioneCur: "+solution);
                lastIndex += 2;
            }
        }





        return solution;
    }

}