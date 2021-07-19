package com.example.mostraMosse;

import android.content.Context;
import android.content.SharedPreferences;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;

//import com.example.mostraMosse.util.Util;
import com.threeDBJ.MGraphicsLib.math.Vec3;
import com.threeDBJ.MGraphicsLib.texture.TextureFont;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import timber.log.Timber;

public class CubeRenderer implements GLSurfaceView.Renderer {

    private float _width = 320f;
    private float _height = 480f;

    private static int[] viewport = new int[16];
    private static float[] modelViewMatrix = new float[16];
    private static float[] projectionMatrix = new float[16];
    private static float[] pointInPlane = new float[16];

    private float xMin, xMax, yMin, yMax;
    public boolean worldBoundsSet = false, GLDataLoaded = false;

    private GLWorld world;
    private CubeMenu menu;
    private TextureFont textureFont;
    private Context context;
    private SharedPreferences prefs;
    private RubeCube cube;
    private char[] configurazione;

    public CubeRenderer(Context context, TextureFont font, GLWorld world,
                        RubeCube rCube, CubeMenu menu, SharedPreferences prefs) {
        this.world = world;
        this.menu = menu;
        this.cube = rCube;
        this.context = context;
        textureFont = font;
        this.prefs = prefs;
        this.configurazione=rCube.getConfigurazione();
    }
/*
    public CubeRenderer(Context context, TextureFont font, GLWorld world,
                        RubeCube rCube, CubeMenu menu) {
        this.world = world;
        this.menu = menu;
        this.cube = rCube;
        this.context = context;
        textureFont = font;
        this.configurazione=rCube.getConfigurazione();
    }
*/


    @Override
    public void onSurfaceCreated(GL10 g, EGLConfig config) {
        GL11 gl = (GL11) g;

        gl.glClearColor(0.0f, 0.0f, 0.0f, 1f);
        gl.glClearDepthf(1.0f);
        gl.glEnable(GL11.GL_DEPTH_TEST);
        gl.glDepthFunc(GL11.GL_LEQUAL);
        gl.glShadeModel(GL11.GL_SMOOTH);
        gl.glEnable(GL11.GL_CULL_FACE);
        gl.glEnable(GL11.GL_BLEND);
        gl.glFrontFace(GL11.GL_CW);
        gl.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        //gl.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);


        gl.glHint(GL11.GL_POLYGON_SMOOTH_HINT, GL11.GL_NICEST);
        menu.init(gl, context);
        textureFont.init(gl, context);

        /*if (Util.dimensionSaved(prefs)) {
            cube.init();
            //menu.setRestore(prefs);
            Timber.d("Restoring sides");
            cube.restore(prefs);
        } else {*/

        cube.init();
        //char[] prova={'w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w','w'};
        //cube.setupSides();
        //cube.configura(prova);
        //Timber.d("Prefs saved? "+prefs.getBoolean("isConfigSaved", false));
        if(prefs.getBoolean("isConfigSaved", false)){ //se ho memorizzato la configurazione
            //Timber.d("Restoring sides");
            System.out.println("Restoring sides e iterator su listaMosse");
            cube.restore(prefs);
            menu.restore(prefs);
        }
        else{
            //Timber.d("Initializing sides");
            System.out.println("Initializing sides");
            cube.configura(configurazione);
        }

            //System.out.println("GATTO"+Arrays.toString(configurazione));
            //cube.configura(prova);
        //}
        cube.world.generate();
        world.setRubeCube(cube);
        GLDataLoaded = true;
    }

    @Override
    public void onDrawFrame(GL10 g) {
        GL11 gl = (GL11) g;

        surfaceSetup(gl);
        // Clears the screen and depth buffer.
        gl.glClearColor(0.5f, 0.5f, 0.5f, 1);
        gl.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        gl.glMatrixMode(GL11.GL_MODELVIEW);
        gl.glLoadIdentity();


        GLU.gluLookAt(gl, 0f, 0f, 7f,
                0f, 0f, 0f,
                0f, 1f, 0f);
        gl.glGetFloatv(GL11.GL_MODELVIEW_MATRIX, modelViewMatrix, 0);
        if (!worldBoundsSet) {
            getWorldBounds();
        }

        gl.glEnable(GL11.GL_DEPTH_TEST);
        gl.glEnableClientState(GL11.GL_COLOR_ARRAY);
        gl.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        world.draw(gl);
        menu.draw(gl);
        gl.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        gl.glDisableClientState(GL11.GL_COLOR_ARRAY);
        gl.glDisableClientState(GL11.GL_VERTEX_ARRAY);
        //gl.glFlush();
    }

    private void getWorldBounds() {
        GLU.gluUnProject(0, viewport[3], 7f, modelViewMatrix, 0,
                projectionMatrix, 0, viewport, 0, pointInPlane, 0);
        xMax = pointInPlane[0] * -6f;
        yMin = pointInPlane[1] * -6f;
        GLU.gluUnProject(_width, 0, 7f, modelViewMatrix, 0,
                projectionMatrix, 0, viewport, 0, pointInPlane, 0);
        xMin = pointInPlane[0] * -6f;
        yMax = pointInPlane[1] * -6f;
        worldBoundsSet = true;
        menu.setBounds(xMin, xMax, yMin, yMax);


    }

    public Vec3 screenToWorld(Vec3 p) {
        p.x = p.x * (xMax - xMin) + xMin;
        p.y = p.y * (yMax - yMin) + yMin;
        return p;
    }

    @Override
    public void onSurfaceChanged(GL10 g, int w, int h) {
        GL11 gl = (GL11) g;
        if (h == 0) {
            h = 1;
        }
        gl.glViewport(0, 0, w, h);
        _width = w;
        _height = h;
        surfaceSetup(gl);
    }

    private void surfaceSetup(GL11 gl) {
        // Reset projection matrix
        float ratio = _width / _height;
        gl.glMatrixMode(GL11.GL_PROJECTION);
        gl.glLoadIdentity();

        GLU.gluPerspective(gl, 45f, ratio, 5f, 10f);
        gl.glGetIntegerv(GL11.GL_VIEWPORT, viewport, 0);
        gl.glGetFloatv(GL11.GL_PROJECTION_MATRIX, projectionMatrix, 0);
        // OpenGL goes for quality over performance by default
        gl.glDisable(GL11.GL_DITHER);

    }

}