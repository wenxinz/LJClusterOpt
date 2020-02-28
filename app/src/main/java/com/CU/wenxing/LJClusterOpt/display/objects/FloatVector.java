package com.CU.wenxing.LJClusterOpt.display.objects;

public class FloatVector {
    private float x, y, z;

    public FloatVector(float x, float y, float z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float getX() {
        return x;
    }
    public float getY() {
        return y;
    }
    public float getZ() {
        return z;
    }

    public void set(float x, float y, float z){
        this.x = x;
        this.y = y;
        this.z = z;
    }
    public void add(FloatVector v){
        x = x+v.getX();
        y = y+v.getY();
        z = z+v.getZ();
    }
    public void scale(float factor){
        x = x*factor;
        y = y*factor;
        z = z*factor;
    }
}
