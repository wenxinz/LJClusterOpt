package com.CU.wenxing.LJClusterOpt.chem.simulation;

import android.util.Log;

import com.CU.wenxing.LJClusterOpt.Parameters;
import com.CU.wenxing.LJClusterOpt.chem.MySystem;

public class ModifiedSD extends Simulation {
    public static String TAG = "ModifiedSD";
    private int numOfMSDsteps;

    public ModifiedSD(MySystem ljSystem, int startingStep) {
        super(startingStep, "ModifiedSD");
        numOfMSDsteps = 0;
        step = 0.01;
        ljSystem.calcForce();
        if(Parameters.LoggerConfigOn){
            Log.w(TAG, "simulation created");
        }
    }

    public boolean update(MySystem ljSystem){
        for(int i=0;i<ljSystem.getNumOfParticles();i++){
            ljSystem.getParticle(i).translate(ljSystem.getParticle(i).getForce().unitVector().scale(step));
        }
        ljSystem.calcPotentialE();
        ljSystem.calcForce();

        numOfSteps++;

        numOfMSDsteps++;
        if(numOfMSDsteps == 100){
            return false;
        }
        return true;
    }

    public void stop(MySystem ljSystem){
        ljSystem.setNullForce();
    }
}

