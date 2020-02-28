package com.CU.wenxing.LJClusterOpt.display.program;

import android.content.Context;
import android.opengl.GLES20;

import com.CU.wenxing.LJClusterOpt.display.util.ShaderHelper;
import com.CU.wenxing.LJClusterOpt.display.util.TextResourceReader;

public class Program {
    protected final int program;

    protected Program(Context context, int vertexShaderResourceId, int fragmentShaderResourceId){
        program = ShaderHelper.buildProgram(
                TextResourceReader.readTextFileFromResource(context, vertexShaderResourceId),
                TextResourceReader.readTextFileFromResource(context, fragmentShaderResourceId));
    }

    protected Program(String vetexShaderSource, String fragmentShaderSource){
        program = ShaderHelper.buildProgram(vetexShaderSource, fragmentShaderSource);
    }

    public void useProgram(){
        GLES20.glUseProgram(program);
    }
}
