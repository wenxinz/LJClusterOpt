package com.CU.wenxing.LJClusterOpt.chem;

public class Particle {
    private MyVector position;
    private MyVector velocity;
    private MyVector force;

    public Particle(Particle p){
        setPosition(p.getPosition());
        setVelocity(p.getVelocity());
        setForce((p.getForce()));
    }

    public Particle(){}

    public MyVector getPosition() {
        return position;
    }

    public MyVector getForce() {
        return force;
    }

    public MyVector getVelocity() {
        return velocity;
    }

    public void setPosition(MyVector position) {
        this.position = new MyVector(position);
    }
    public void setPosition(double posX, double posY, double posZ){
        position = new MyVector(posX, posY, posZ);
    }

    public void setVelocity(MyVector velocity) {
        if(velocity == null){
            this.velocity = null;
        }else {
            this.velocity = new MyVector(velocity);
        }
    }
    public void setVelocity(double velX, double velY, double velZ){
        velocity = new MyVector(velX, velY, velZ);
    }

    public void setForce(MyVector force) {
        if(force == null){
            this.force = null;
        }else {
            this.force = new MyVector(force);
        }
    }

    public void translate(MyVector positionChange){
       position = position.add(positionChange);
    }

    public void accelerate(MyVector velocityChange){
        velocity = velocity.add(velocityChange);
    }
}
