package com.CU.wenxing.LJClusterOpt.display.objects;

import android.opengl.GLES20;

import com.CU.wenxing.LJClusterOpt.Parameters;
import com.CU.wenxing.LJClusterOpt.display.data.VertexData;
import com.CU.wenxing.LJClusterOpt.display.program.SphereProgram;

public class Sphere {
    private static int POSITION_COMPONENT_PER_VERTEX = 3;
    private static int NORM_COMPONENT_PER_VERTEX = 3;
    private static int BYTES_PER_FLOAT = 4;
    private static int numOfVerticesPerRing = 74;
    private static int numOfRings = 32;

    private VertexData mVertexData;

    public Sphere(float x, float y, float z){
        mVertexData = new VertexData(generateVertexData(x,y,z,Parameters.PARTICLE_SIZE));
    }

    private float[] generateVertexData(float centerX, float centerY, float centerZ, float radius){
        float[] vertexData = new float[numOfVerticesPerRing*numOfRings*(POSITION_COMPONENT_PER_VERTEX + NORM_COMPONENT_PER_VERTEX)];
        int numOfPoints = numOfVerticesPerRing / 2;
        int index = 0;
        for(int j=0;j<numOfRings;j++) {
            float radiusUpper = radius*(float)Math.cos((((float) (j+1) / (float) numOfRings)-0.5f) * (float) Math.PI);
            float radiusBottom = radius*(float)Math.cos((((float) j / (float) numOfRings)-0.5f) * (float) Math.PI);
            for (int i = 0; i < numOfPoints; i++) {
                float angleInRadius = ((float) i / (float)(numOfPoints-1)) * ((float) Math.PI * 2f);
                vertexData[index++] = centerX + radiusBottom * (float) Math.cos(angleInRadius);
                vertexData[index++] = centerY + radius*(float)Math.sin((((float) j / (float) numOfRings)-0.5f) * (float) Math.PI);
                vertexData[index++] = centerZ + radiusBottom * (float) Math.sin(angleInRadius);

                //norm
                vertexData[index++] = radiusBottom * (float) Math.cos(angleInRadius)/radius;
                vertexData[index++] = (float)Math.sin((((float) j / (float) numOfRings)-0.5f) * (float) Math.PI);
                vertexData[index++] = radiusBottom * (float) Math.sin(angleInRadius)/radius;

                vertexData[index++] = centerX + radiusUpper * (float) Math.cos(angleInRadius);
                vertexData[index++] = centerY + radius*(float)Math.sin((((float) (j+1) / (float) numOfRings)-0.5f) * (float) Math.PI);
                vertexData[index++] = centerZ + radiusUpper * (float) Math.sin(angleInRadius);

                //norm
                vertexData[index++] = radiusUpper * (float) Math.cos(angleInRadius)/radius;
                vertexData[index++] = (float)Math.sin((((float) (j+1) / (float) numOfRings)-0.5f) * (float) Math.PI);
                vertexData[index++] = radiusUpper * (float) Math.sin(angleInRadius)/radius;
            }
        }
        return vertexData;
    }

    public void update(float x, float y, float z){
        mVertexData.updateBuffer(generateVertexData(x,y,z,Parameters.PARTICLE_SIZE));
    }

    public void bindData(SphereProgram program){
        mVertexData.setVertexAttribPointer(0, program.getaPositionLocation(), POSITION_COMPONENT_PER_VERTEX, (POSITION_COMPONENT_PER_VERTEX + NORM_COMPONENT_PER_VERTEX)*BYTES_PER_FLOAT);
        mVertexData.setVertexAttribPointer(POSITION_COMPONENT_PER_VERTEX, program.getaNormalLocation(), NORM_COMPONENT_PER_VERTEX, (POSITION_COMPONENT_PER_VERTEX + NORM_COMPONENT_PER_VERTEX)*BYTES_PER_FLOAT);
    }

    public void draw(){
        for(int i = 0; i < numOfRings; i++) {
            // depth test is important!!!
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, i*numOfVerticesPerRing, numOfVerticesPerRing);
        }
    }
}
