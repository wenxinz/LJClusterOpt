package com.CU.wenxing.LJClusterOpt;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.CU.wenxing.LJClusterOpt.chem.MyVector;
import com.CU.wenxing.LJClusterOpt.display.objects.FloatVector;
import com.CU.wenxing.LJClusterOpt.display.objects.Sphere;
import com.CU.wenxing.LJClusterOpt.display.program.SphereProgram;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyGLRenderer implements GLSurfaceView.Renderer{
    private final Context context;

    private int numParticles;
    private FloatVector[] positions;
    private void initPosition(MyVector[] mv){
        numParticles = mv.length;
        positions = new FloatVector[numParticles];
        for(int i=0;i<numParticles;i++){
            positions[i] = new FloatVector((float)mv[i].getX(),
                    (float)mv[i].getY(),
                    (float)mv[i].getZ());
        }
    }
    private FloatVector center;
    private void calcCenter(){
        center.set(0.0f,0.0f,0.0f);
        for(int i=0;i<numParticles;i++){
            center.add(positions[i]);
        }
        center.scale(1.0f/numParticles);
    }

    public MyGLRenderer(Context context, MyVector[] initPositions){
        this.context = context;
        initPosition(initPositions);
        center = new FloatVector(0.0f, 0.0f, 0.0f);
        calcCenter();
    }

    private Sphere[] spheres;
    private SphereProgram sphereProgram;

    // mvpMatrix ---> M: ModelMatrix  V: ViewMatrix  P: ProjectionMatrix
    private float[] mMVPMatrix = new float[16];
    private float[] mMVMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mModelMatrix = new float[16];
    private float[] mTranslateMatrix = new float[16];
    private float[] mRotateMatrix = new float[16];
    private float[] mNormalMatrix = new float[9];

    private float rAngleX = 0.0f;
    private float rAngleY = 0.0f;

    public void handleOnTouchRotation(float dx, float dy){
        rAngleX += dy;
        rAngleY += -dx;
    }

    private void createSpheres(){
        spheres = new Sphere[numParticles];
        for(int i=0;i<positions.length;i++){
            spheres[i] = new Sphere(positions[i].getX(),positions[i].getY(),positions[i].getZ());
        }
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config){
        GLES20.glClearColor(1.0f,1.0f,1.0f,1.0f);

        createSpheres();
        sphereProgram = new SphereProgram(context);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        // Projection matrix
        // Matrix.frustumM(float[] m, int offset, float left, float right, float bottom, float top, float near, float far);
        float ratio = (float)height/width;
        Matrix.frustumM(mProjectionMatrix, 0,
                -(0.5f*(float)Parameters.BOUNDARY_X + Parameters.PARTICLE_SIZE),
                0.5f*(float)Parameters.BOUNDARY_X + Parameters.PARTICLE_SIZE,
                -ratio*(0.5f*(float)Parameters.BOUNDARY_Y + Parameters.PARTICLE_SIZE),
                ratio*(0.5f*(float)Parameters.BOUNDARY_Y + Parameters.PARTICLE_SIZE),
                2.0f*(float)Parameters.BOUNDARY_Z - Parameters.PARTICLE_SIZE,
                3.0f*(float)Parameters.BOUNDARY_Z + Parameters.PARTICLE_SIZE);

        // View matrix
        //Matrix.setLookAtM(float[] m, int offset, float eyeX, float eyeY, float eyeZ,
        //                  float centerX, float centerY, float centerZ, float upX, float upY, float upZ)
        Matrix.setLookAtM(mViewMatrix, 0,
                0.0f, 0.0f, 0.0f,
                0.0f, 0.0f, -2.5f*(float)Parameters.BOUNDARY_Z,
                0.0f, 1.0f, 0.0f);
    }

    @Override
    public void onDrawFrame(GL10 unused){
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if(Parameters.CLUSTER) {
            // Model matrix
            // 1. translate center of mass to (0,0,0)
            Matrix.setIdentityM(mTranslateMatrix, 0);
            Matrix.translateM(mTranslateMatrix, 0,
                    -center.getX(),
                    -center.getY(),
                    -center.getZ());
            // 2. rotate
            Matrix.setIdentityM(mRotateMatrix, 0);
            Matrix.rotateM(mRotateMatrix, 0, rAngleX, -1.0f, 0.0f, 0.0f);
            Matrix.rotateM(mRotateMatrix, 0, rAngleY, 0.0f, -1.0f, 0.0f);
            Matrix.multiplyMM(mModelMatrix, 0, mRotateMatrix, 0, mTranslateMatrix, 0);
            // 3. translate box to (-L/2, -L/2, -3L) <-> (L/2,L/2,-2L),
            Matrix.setIdentityM(mTranslateMatrix, 0);
            Matrix.translateM(mTranslateMatrix, 0,
                    center.getX() - 0.5f * (float) Parameters.BOUNDARY_X,
                    center.getY() - 0.5f * (float) Parameters.BOUNDARY_Y,
                    center.getZ() - 3f * (float) Parameters.BOUNDARY_Z);
            Matrix.multiplyMM(mModelMatrix, 0, mTranslateMatrix, 0, mModelMatrix, 0);
        }else{
            // Model matrix
            // 1. translate center of the box to (0,0,0)
            Matrix.setIdentityM(mTranslateMatrix, 0);
            Matrix.translateM(mTranslateMatrix, 0,
                    -0.5f * (float) Parameters.BOUNDARY_X,
                    -0.5f * (float) Parameters.BOUNDARY_Y,
                    -0.5f *  (float) Parameters.BOUNDARY_Z);
            // 2. rotate
            Matrix.setIdentityM(mRotateMatrix, 0);
            Matrix.rotateM(mRotateMatrix, 0, rAngleX, -1.0f, 0.0f, 0.0f);
            Matrix.rotateM(mRotateMatrix, 0, rAngleY, 0.0f, -1.0f, 0.0f);
            Matrix.multiplyMM(mModelMatrix, 0, mRotateMatrix, 0, mTranslateMatrix, 0);
            // 3. translate box to (-L/2, -L/2, -3L) <-> (L/2,L/2,-2L),
            Matrix.setIdentityM(mTranslateMatrix, 0);
            Matrix.translateM(mTranslateMatrix, 0, 0.0f, 0.0f, -2.5f * (float) Parameters.BOUNDARY_Z);
            Matrix.multiplyMM(mModelMatrix, 0, mTranslateMatrix, 0, mModelMatrix, 0);
        }

        //  Mprojection * Mview * Mmodel
        Matrix.multiplyMM(mMVMatrix, 0,  mViewMatrix, 0, mModelMatrix, 0);
        mNormalMatrix[0] = mMVMatrix[0];
        mNormalMatrix[1] = mMVMatrix[1];
        mNormalMatrix[2] = mMVMatrix[2];
        mNormalMatrix[3] = mMVMatrix[4];
        mNormalMatrix[4] = mMVMatrix[5];
        mNormalMatrix[5] = mMVMatrix[6];
        mNormalMatrix[6] = mMVMatrix[8];
        mNormalMatrix[7] = mMVMatrix[9];
        mNormalMatrix[8] = mMVMatrix[10];
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVMatrix, 0);

        sphereProgram.useProgram();
        GLES20.glUniformMatrix4fv(sphereProgram.getuMVPMatrixLocation(), 1, false, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(sphereProgram.getuMVMatrixLocation(), 1, false, mMVMatrix, 0);
        GLES20.glUniformMatrix4fv(sphereProgram.getuMVLightMatrixLocation(), 1, false, mViewMatrix , 0);
        GLES20.glUniformMatrix3fv(sphereProgram.getuNormalMatrixLocation(), 1, false, mNormalMatrix, 0);
        for(int i=0;i<spheres.length;i++){
            spheres[i].bindData(sphereProgram);
            spheres[i].draw();
        }
    }

    public void updateView(MyVector[] newPostions){
        if(newPostions.length != numParticles){
            initPosition(newPostions);
            createSpheres();
        }else {
            for (int i = 0; i < numParticles; i++) {
                float x = (float) newPostions[i].getX();
                float y = (float) newPostions[i].getY();
                float z = (float) newPostions[i].getZ();
                positions[i].set(x, y, z);
                spheres[i].update(x, y, z);
            }
        }
        calcCenter();
    }
}
