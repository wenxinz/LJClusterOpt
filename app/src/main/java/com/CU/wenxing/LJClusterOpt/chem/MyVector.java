package com.CU.wenxing.LJClusterOpt.chem;

public class MyVector {
    private double x, y, z;

    public MyVector(double x, double y, double z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public MyVector(MyVector v){
        x = v.getX();
        y = v.getY();
        z = v.getZ();
    }

    public MyVector(){
        this(0.0,0.0,0.0);
    }

    public double getX(){
        return x;
    }
    public double getY(){
        return y;
    }
    public double getZ(){
        return z;
    }

    public double module(){
        return Math.sqrt(x*x + y*y + z*z);
    }

    public MyVector unitVector(){
        return this.scale(1.0/this.module());
    }

    public MyVector add(MyVector v){
        return new MyVector(x+v.getX(), y+v.getY(), z+v.getZ());
    }

    public MyVector subtract(MyVector v){
        return new MyVector(x-v.getX(), y-v.getY(), z-v.getZ());
    }

    public MyVector scale(double f){
        return new MyVector(x*f, y*f, z*f);
    }

    public MyVector crossProduct(MyVector v){
        return new MyVector(
                y*v.getZ() - z*v.getY(),
                z*v.getX() - x*v.getZ(),
                x*v.getY() - y*v.getX()
        );
    }

    public double dotProduct(MyVector v){
        return x*v.getX() + y*v.getY() + z*v.getZ();
    }
}
