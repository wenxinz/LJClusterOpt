package com.CU.wenxing.LJClusterOpt.display.data;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class VertexData {
    private static int BYTES_PER_FLOAT = 4;
    private final FloatBuffer floatBuffer;

    public VertexData(float[] vertexData){
        floatBuffer = ByteBuffer
                .allocateDirect(vertexData.length*BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData);
    }

    public void setVertexAttribPointer(int dataOffset, int attribLocation, int componentCount, int stride){
        floatBuffer.position(dataOffset);
        GLES20.glVertexAttribPointer(attribLocation, componentCount, GLES20.GL_FLOAT, false, stride, floatBuffer);
        GLES20.glEnableVertexAttribArray(attribLocation);
        floatBuffer.position(0);
    }

    public void updateBuffer(float[] vertexData){
        floatBuffer.position(0);
        floatBuffer.put(vertexData);
        floatBuffer.position(0);
    }
}
