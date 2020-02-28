package com.CU.wenxing.LJClusterOpt.display.program;

import android.content.Context;
import android.opengl.GLES20;

import com.CU.wenxing.LJClusterOpt.R;

public class SphereProgram extends Program {

    // attribute variable location
    private int aPositionLocation;
    private int aNormalLocation;
    // uniform variable location
    private int uMVPMatrixLocation;
    private int uMVMatrixLocation;
    private int uNormalMatrixLocation;
    private int uMVLightMatrixLocation;

    public SphereProgram(Context context){
        super(context, R.raw.sphere_vertex_shader, R.raw.sphere_fragment_shader);

        aPositionLocation = GLES20.glGetAttribLocation(program, "aPosition");
        aNormalLocation = GLES20.glGetAttribLocation(program, "aNormal");
        uMVPMatrixLocation = GLES20.glGetUniformLocation(program, "uMVPMatrix");
        uMVMatrixLocation = GLES20.glGetUniformLocation(program, "uMVMatrix");
        uNormalMatrixLocation = GLES20.glGetUniformLocation(program, "uNormalMatrix");
        uMVLightMatrixLocation = GLES20.glGetUniformLocation(program, "uMVLightMatrix");
    }

    public SphereProgram(String vetexShaderSource, String fragmentShaderSource){
        super(vetexShaderSource, fragmentShaderSource);

        aPositionLocation = GLES20.glGetAttribLocation(program, "aPosition");
        aNormalLocation = GLES20.glGetAttribLocation(program, "aNormal");
        uMVPMatrixLocation = GLES20.glGetUniformLocation(program, "uMVPMatrix");
        uMVMatrixLocation = GLES20.glGetUniformLocation(program, "uMVMatrix");
        uNormalMatrixLocation = GLES20.glGetUniformLocation(program, "uNormalMatrix");
        uMVLightMatrixLocation = GLES20.glGetUniformLocation(program, "uMVLightMatrix");
    }

    public int getaPositionLocation(){
        return aPositionLocation;
    }
    public int getaNormalLocation() {return aNormalLocation;}
    public int getuMVPMatrixLocation(){
        return uMVPMatrixLocation;
    }
    public int getuMVMatrixLocation() {return uMVMatrixLocation;}
    public int getuNormalMatrixLocation() {return uNormalMatrixLocation;}
    public int getuMVLightMatrixLocation() {return uMVLightMatrixLocation;}

}
