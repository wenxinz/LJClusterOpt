package com.CU.wenxing.LJClusterOpt.chem;

import android.util.Log;

import com.CU.wenxing.LJClusterOpt.chem.potential.LJPotential;

import java.util.Random;

public class MySystem {
    public static String TAG = "MySystem";

    private int numOfParticles;
    private double boundaryX, boundaryY, boundaryZ;
    public double getBoundaryX(){return boundaryX;}
    public double getBoundaryY(){return boundaryY;}
    public double getBoundaryZ(){return boundaryZ;}
    private boolean PBC = true;
    private double cutoff;

    public int getNumOfParticles(){ return numOfParticles;}
    public boolean isPBC(){return PBC;}

    private Particle[] particleList;
    public void copyParticleList(Particle[] particles){
        numOfParticles = particles.length;
        particleList = new Particle[numOfParticles];
        for(int i=0;i<numOfParticles;i++){
            particleList[i] = new Particle(particles[i]);
        }
    }
    public Particle getParticle(int i){return particleList[i];}

    private double potentialE = 0.0;
    private double kineticE = 0.0;

    public double getPotentialE(){return potentialE;}
    public double getKineticE(){return kineticE;}

    private boolean velSetting = false;
    private boolean forceSetting = false;
    public void setVelState(boolean isVelSet){
        velSetting = isVelSet;
    }
    public void setForceState(boolean isForceSet){
        forceSetting = isForceSet;
    }
    public boolean isVelSet(){
        return velSetting;
    }
    public boolean isForceSet(){
        return forceSetting;
    }

    public MySystem(double x, double y, double z){
        boundaryX = x;
        boundaryY = y;
        boundaryZ = z;
        if(((x>y? y:x)>z?(x>y? y:x):z) < 6){
            Log.w(TAG, "box size too small! The smallest side should be at least 6 sigma!");
        }
        cutoff = 0.5*((x>y? y:x)>z?(x>y? y:x):z);
    }

    public MySystem(int numOfParticles, double x, double y, double z){
        this(x,y,z);
        this.numOfParticles = numOfParticles;
        particleList = new Particle[numOfParticles];
        initClusterPos();
        //initRandPos();
        calcPotentialE();
    }

    // *********************************************************************************************
    private void initRandPos(){
        boolean reject = false;
        for (int i = 0; i < numOfParticles; i++) {
            particleList[i] = new Particle();

            particleList[i].setPosition(
                    boundaryX * Math.random(),
                    boundaryY * Math.random(),
                    boundaryZ * Math.random());
            for (int j = 0; j < i; j++) {
                double distance = particleList[i].getPosition().subtract(particleList[j].getPosition()).module();
                if (distance < 0.8) {
                    reject = true;
                    break;
                }
            }
            if (reject) {
                i--;
                reject = false;
            }
        }
    }

    private void initClusterPos() {
        particleList[0] = new Particle();
        particleList[0].setPosition(
                boundaryX * Math.random(),
                boundaryY * Math.random(),
                boundaryZ * Math.random());

        for (int i = 1; i < numOfParticles; i++) {
            particleList[i] = new Particle();

            boolean reject = true;

            particleList[i].setPosition(
                    boundaryX * Math.random(),
                    boundaryY * Math.random(),
                    boundaryZ * Math.random());

            for (int j = 0; j < i; j++) {
                double distance = particleList[i].getPosition().subtract(particleList[j].getPosition()).module();
                if(distance < 1.5){
                    reject = false;
                    if (distance < 0.8) {
                        reject = true;
                        break;
                    }
                }
            }
            if (reject) {
                i--;
            }
        }

        // move the center of mass of the cluster to the middle of the box
        MyVector centerofmass = getCenterOfMass();
        for(int i=0;i<numOfParticles;i++){
            particleList[i].translate(new MyVector(boundaryX,boundaryY,boundaryZ).scale(0.5).subtract(centerofmass));
        }
    }

    public boolean isOneCluster(){
        for(int i=1;i<numOfParticles;i++){
            boolean sameCluster = false;
            for(int j=0;j<i;j++){
                if(particleList[i].getPosition().subtract(particleList[j].getPosition()).module() < 1.5){
                    sameCluster = true;
                    break;
                }
            }
            if(!sameCluster){
                return false;
            }
        }
        return true;
    }

    public void initVel(double temperature){
        Random rndGuassian = new Random(System.currentTimeMillis());
        for (int i = 0; i < numOfParticles; i++) {
            // initialize paritcle velocity
            double velScale = Math.sqrt(temperature);
            particleList[i].setVelocity(
                    velScale*rndGuassian.nextGaussian(),
                    velScale*rndGuassian.nextGaussian(),
                    velScale*rndGuassian.nextGaussian());
            //overalMomentum = overalMomentum.add(particleList[i].getLJVelocity().scale(particleList[i].getMass()));
        }

        // correct initial velocities so there's no overal momentum
        // need if particles in the system don't have the same mass
        velSetting = true;
    }

    public void setNullVel(){
        for (int i = 0; i < numOfParticles; i++) {
            particleList[i].setVelocity(null);
        }
        velSetting = false;
    }

    public void calcForce(){
        for(int i=0;i<numOfParticles;i++) {
            MyVector force = new MyVector();
            double r;
            if (PBC) {
                for (int j = 0; j < numOfParticles; j++) {
                    if (j != i) {
                        MyVector rji = particleList[i].getPosition().subtract(particleList[j].getPosition());
                        // minimum image criterion:
                        // among all images of a particle, consider only the closest and neglect the rest
                        rji = minImage(rji);
                        r = rji.module();
                        if (r < cutoff) {
                            force = force.add(rji.scale(1.0 / r).scale(-1.0 * LJPotential.gradient(r)));
                        }
                    }
                }
            }
            if(force == null){
                Log.i(TAG, "NULL FORCE");
            }
            particleList[i].setForce(force);
        }
    }

    public void initForce(){
        calcForce();
        forceSetting = true;
    }

    public void setNullForce(){
        for (int i = 0; i < numOfParticles; i++) {
            particleList[i].setForce(null);
        }
        forceSetting = false;
    }

    private MyVector minImage(MyVector v){
        double dx = v.getX();
        double dy = v.getY();
        double dz = v.getZ();

        if (Math.abs(dx) > 0.5 * boundaryX)
            dx = dx - boundaryX * Math.signum(dx);
        if (Math.abs(dy) > 0.5 * boundaryY)
            dy = dy - boundaryY * Math.signum(dy);
        if (Math.abs(dz) > 0.5 * boundaryZ)
            dz = dz - boundaryZ * Math.signum(dz);

        return new MyVector(dx, dy, dz);
    }

    public MyVector boundCond(MyVector v){
        double x = v.getX();
        double y = v.getY();
        double z = v.getZ();

        if(x > 2.0*boundaryX || x < -boundaryX || y > 2.0*boundaryY || y < -boundaryY || z > 2.0*boundaryZ || z < -boundaryZ){
            return null;
        }else{
            if(x > boundaryX){
                return v.add(new MyVector(-boundaryX, 0f, 0f));}
            else if(x < 0){
                return v.add(new MyVector(boundaryX, 0f, 0f));}

            if(y > boundaryY){
                return v.add(new MyVector(0f, -boundaryY, 0f));}
            else if(y < 0){
                return v.add(new MyVector(0f, boundaryY, 0f));}

            if(z > boundaryZ){
                return v.add(new MyVector(0f, 0f, -boundaryZ));}
            else if(z < 0){
                return v.add(new MyVector(0f, 0f, boundaryZ));}
        }

        return v;
    }

    public void calcPotentialE(){
        potentialE = 0.0;
        double r;
        if(PBC) {
            for (int i = 0; i < numOfParticles; i++) {
                for (int j = 0; j < i; j++) {
                    MyVector rji = particleList[i].getPosition().subtract(particleList[j].getPosition());
                    // minimum image criterion:
                    // among all images of a particle, consider only the closest and neglect the rest
                    rji = minImage(rji);
                    r = rji.module();
                    if (r < cutoff) {
                        potentialE += LJPotential.potentialE(r);
                    }
                }
            }
        }
    }

    public void calcKineticE(){
        kineticE = 0.0;
        if(isVelSet()) {
            for (int i = 0; i < numOfParticles; i++) {
                kineticE += 0.5 * particleList[i].getVelocity().dotProduct(particleList[i].getVelocity());
            }
        }
    }

    public MyVector getCenterOfMass(){
        MyVector centerOfMass = new MyVector();
        for(int i=0;i<numOfParticles;i++){
            centerOfMass = centerOfMass.add(particleList[i].getPosition());
        }
        return centerOfMass.scale(1.0/numOfParticles);
    }

    // clustering allgorithm
    // not 100% sure about it's validity
    // check before using
    public int clustering(){
        int[] particleClusterID = new int[numOfParticles];
        int num = 1;
        particleClusterID[0] = num;
        for(int i=0;i<numOfParticles;i++){
            if(particleClusterID[i] == 0) particleClusterID[i] = ++num;
            for(int j=i+1;j<numOfParticles;j++){
                if(particleClusterID[j] != particleClusterID[i]) {
                    //if particle i and j are already in same cluster, ignore
                    if (minImage(particleList[i].getPosition().subtract(particleList[j].getPosition())).module() < 1.5) {
                        // particle i and j should belong to the same cluster
                        if(particleClusterID[j] == 0){
                            // if particle j hasn't been assigned to any cluster,
                            // assign it to the cluster particle i belongs to
                            particleClusterID[i] = particleClusterID[j];
                        }else{
                            // if particle j has already been assigned to a cluster,
                            // and it's not the same cluster particle i belongs to,
                            // merge two clusters, the new id is the previous smaller id,
                            // and changes id of all clusters with id larger than the previous higher id
                            if(particleClusterID[i] > particleClusterID[j])
                                renameClusterID(particleClusterID, particleClusterID[i],particleClusterID[j]);
                            else
                                renameClusterID(particleClusterID, particleClusterID[j],particleClusterID[i]);
                            // change clusternumbers
                            num--;
                        }
                    }
                }
            }
        }
        return num;
    }

    private void renameClusterID(int id[], int largeid, int smallid){
        for(int i=0;i<id.length;i++){
            if(id[i] == largeid)
                id[i] = smallid;
            else if(id[i] > largeid)
                id[i]--;
        }
    }


    // *********************************************************************************************
    public MyVector[] getParticlePositions(){
        MyVector[] positions = new MyVector[numOfParticles];
        for(int i=0;i<numOfParticles;i++){
            positions[i] = new MyVector(particleList[i].getPosition());
        }
        return positions;
    }
}
