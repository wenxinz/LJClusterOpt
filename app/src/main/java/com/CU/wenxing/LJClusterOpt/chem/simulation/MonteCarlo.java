package com.CU.wenxing.LJClusterOpt.chem.simulation;

import android.util.Log;

import com.CU.wenxing.LJClusterOpt.Parameters;
import com.CU.wenxing.LJClusterOpt.chem.MyVector;
import com.CU.wenxing.LJClusterOpt.chem.MySystem;

public class MonteCarlo extends Simulation {
    public static String TAG = "MonteCarlo";
    private int numOfMCsteps;

    public MonteCarlo(MySystem ljSystem, int startingStep){
        super(startingStep, "MonteCarlo");
        numOfMCsteps = 0;
        step = Parameters.MCSTEP;
        if(Parameters.LoggerConfigOn){
            Log.w(TAG, "simulation created");
        }
    }

    public boolean update(MySystem ljSystem){
        boolean reject = false;
        double probOfAccept = 0.0;

        MyVector[] positionBefore = new MyVector[ljSystem.getNumOfParticles()];
        for(int i=0;i<ljSystem.getNumOfParticles();i++){
            positionBefore[i] = new MyVector(ljSystem.getParticle(i).getPosition());
        }
        double potentialBefore = ljSystem.getPotentialE();

        for(int i=0;i<ljSystem.getNumOfParticles();i++){
            ljSystem.getParticle(i).translate(new MyVector(
                    (Math.random()-0.5)*step,
                    (Math.random()-0.5)*step,
                    (Math.random()-0.5)*step));
            if(ljSystem.boundCond(ljSystem.getParticle(i).getPosition()) == null){
                if(Parameters.LoggerConfigOn){
                    Log.e(TAG, "something crazy happens");
                }
            }else{
                ljSystem.getParticle(i).setPosition(ljSystem.boundCond(ljSystem.getParticle(i).getPosition()));
            }
        }
        ljSystem.calcPotentialE();

        if(ljSystem.getPotentialE() > potentialBefore){
            probOfAccept = Math.exp(-(ljSystem.getPotentialE()-potentialBefore)/Parameters.TEMPERATURE);
            if(Math.random() > probOfAccept){
                reject = true;
            }
        }

        if(reject){
            for(int i=0;i<ljSystem.getNumOfParticles();i++){
                ljSystem.getParticle(i).setPosition(positionBefore[i]);
            }
            ljSystem.calcPotentialE();
        }

        if(Parameters.LoggerConfigOn) {
            Log.i(TAG, "system potential energy: " + ljSystem.getPotentialE());
        }

        numOfSteps++;

        numOfMCsteps++;
        if(numOfMCsteps == Parameters.NUM_OF_MC_STEPS){
            return false;
        }
        return true;
    }
}
