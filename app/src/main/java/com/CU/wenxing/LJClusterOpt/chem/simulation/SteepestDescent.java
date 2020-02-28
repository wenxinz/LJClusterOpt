package com.CU.wenxing.LJClusterOpt.chem.simulation;

import android.util.Log;

import com.CU.wenxing.LJClusterOpt.Parameters;
import com.CU.wenxing.LJClusterOpt.chem.MyVector;
import com.CU.wenxing.LJClusterOpt.chem.MySystem;

public class SteepestDescent extends Simulation {
    public static String TAG = "SteepestDescent";

    // to do a geometry optimization using steepest descent:
    // forces on each particle(negtive gradient) need to be known
    // only potentail energy is relevant

    public SteepestDescent(MySystem ljSystem, int startingStep){
        super(startingStep, "SteepestDescent");
        step = Parameters.LAMBADA;
        ljSystem.calcForce();
        if(Parameters.LoggerConfigOn){
            Log.w(TAG, "simulation created");
        }
    }

    public boolean update(MySystem ljSystem){
        boolean reject = true;
        boolean converged = false;
        boolean changeStep = false;

        // store the energy of the system before changing
        double energyBefore = ljSystem.getPotentialE();
        // store the position before changing
        MyVector[] positionBefore = new MyVector[ljSystem.getNumOfParticles()];
        for(int i = 0; i< ljSystem.getNumOfParticles(); i++){
            positionBefore[i] = ljSystem.getParticle(i).getPosition();
        }

        // do energy optimization
        while(reject) {
            // test 1: particle shouldn't change by distance larger than 2 boxlength
            for (int i = 0; i < ljSystem.getNumOfParticles(); i++) {
                MyVector test = positionBefore[i].add(ljSystem.getParticle(i).getForce().scale(step));
                if (ljSystem.boundCond(test) == null) {
                    if(Parameters.LoggerConfigOn) {
                        Log.w(TAG, "particle move too fase! reduce lambda!");
                    }
                    changeStep = true;
                    step = 0.5* step;
                    break;
                }
                ljSystem.getParticle(i).setPosition(ljSystem.boundCond(test));
            }

            if(changeStep){
                changeStep = false;
                continue;
            }

            // test 2: energy of the system shouldn't increase
            ljSystem.calcPotentialE();
            double energyAfter = ljSystem.getPotentialE();
            if(Parameters.LoggerConfigOn) {
                Log.i(TAG, "energy: " + ljSystem.getPotentialE());
            }

            if(energyBefore < energyAfter){
                if(Parameters.LoggerConfigOn) {
                    Log.w(TAG, "energy increase! reduce lambada!");
                }
                step = 0.5* step;
                continue;
            }else if((energyBefore-energyAfter) < 0.001) {
                // if the energy change is small, system could be at a minimum
                // ---> need to be further confirmed by force test
                converged = true;
            }

            reject = false;
        }

        // update force
        ljSystem.calcForce();

        // always increase lambada unless it's rejected
        step = 1.1* step;

        // test3: is system actually at a minimum
        if(converged) {
            for (int i = 0; i < ljSystem.getNumOfParticles(); i++) {
                if (ljSystem.getParticle(i).getForce().module() > 0.01) {
                    converged = false;
                    break;
                }
            }
        }

        // if the system is truely at a minimum, stop the optimization process
        if(converged){
            if(Parameters.LoggerConfigOn) {
                Log.i(TAG, " system is at minimum");
            }
            return false;
        }

        numOfSteps++;
        return true;
    }

    public void stop(MySystem ljSystem){
        ljSystem.setNullForce();
    }
}
