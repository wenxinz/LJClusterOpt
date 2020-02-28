package com.CU.wenxing.LJClusterOpt;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.CU.wenxing.LJClusterOpt.chem.MySystem;
import com.CU.wenxing.LJClusterOpt.chem.simulation.ModifiedSD;
import com.CU.wenxing.LJClusterOpt.chem.simulation.MonteCarlo;
import com.CU.wenxing.LJClusterOpt.chem.simulation.Simulation;
import com.CU.wenxing.LJClusterOpt.chem.simulation.SteepestDescent;

/**
 * Created by wenxing on 6/19/2017.
 */

public class SimulationActivity extends HandlerThread {
    private static final String TAG = "SimulationActivity";

    private boolean mHasQuit = false;
    private MySystem mSystem;
    private Simulation mSimulation;
    private Handler mSimulationHandler;
    private Handler mUIHandler;

    static final int UPDATE_GLVIEW = 0;
    static final int UPDATE_TOOLBAR = 1;
    static final int UPDATE_DISPLAY = 2;
    static final int UPDATE_MINENERGYDIF = 3;

    class Data{
        MySystem simulsystem;
        Simulation simul;

        Data(MySystem sys, Simulation sim){
            simulsystem = sys;
            simul = sim;
        }
    }

    public SimulationActivity(Handler uiHandler, MySystem mSimulationSystem){
        super(TAG);

        mUIHandler = uiHandler;
        mSystem = mSimulationSystem;
    }

    private void createSimulation(){
        int startingStep;
        if(mSimulation == null){
            startingStep = 0;
        }else {
            startingStep = mSimulation.getNumOfSteps();
        }
        switch (Parameters.TYPE) {
            case "MonteCarlo":
                mSimulation = new MonteCarlo(mSystem, startingStep);
                break;
            case "SteepestDescent":
                mSimulation = new SteepestDescent(mSystem, startingStep);
                break;
            case "ModifiedSD":
                mSimulation = new ModifiedSD(mSystem, startingStep);
                break;
        }
    }

    public void runSimulation(){
        // Start with steepest decent
        Parameters.TYPE = "SteepestDescent";
        createSimulation();
        mUIHandler.obtainMessage(UPDATE_MINENERGYDIF).sendToTarget();

        mSimulationHandler.post(new Runnable() {
            @Override
            public void run() {
                // Do user want to restart?
                if(Parameters.RESTART){
                    //recreate system
                    mSystem = new MySystem(Parameters.NUM_OF_PARTICLES,
                            Parameters.BOUNDARY_X,Parameters.BOUNDARY_Y,Parameters.BOUNDARY_Z);
                    mUIHandler.obtainMessage(UPDATE_GLVIEW, mSystem.getParticlePositions()).sendToTarget();
                    if(mSystem.getParticlePositions() == null) Log.e(TAG, "NULL SYSTEM");
                    //recreate simulation
                    if(mSimulation != null) {
                        mSimulation = null;
                    }
                    Parameters.TYPE = "SteepestDescent";
                    createSimulation();
                    mUIHandler.obtainMessage(UPDATE_MINENERGYDIF).sendToTarget();
                    //unset restart state
                    Parameters.RESTART = false;
                }
                // Does the system find global minimum?
                if (Parameters.RUN) {
                    // check to see if simulation needed to be changed to another type
                    if(!Parameters.TYPE.equals(mSimulation.getSimulationType())){
                        mSimulation.stop(mSystem);
                        createSimulation();
                    }
                    // run simulation
                    if(!mSimulation.update(mSystem)){
                        switch (mSimulation.getSimulationType()){
                            case "SteepestDescent":
                                // system reach a local minima, check to see if it's global minima
                                if((mSystem.getPotentialE()-Parameters.GLOBAL_MINIMA) < 0.0001){
                                    // global minima is reached, stop simulation
                                    Parameters.RUN = false;
                                   mUIHandler.obtainMessage(UPDATE_TOOLBAR).sendToTarget();
                                }else{
                                    // it's a local minima, switch to MC and continue
                                    Parameters.TYPE = "MonteCarlo";
                                }
                                break;
                            case "MonteCarlo":
                                // system has run MC for Parameters.NUM_OF_MC_STEPS steps,
                                // switch to SD to find the minima
                                Parameters.TYPE = "SteepestDescent"; break;
                            case "ModifiedSD":
                                // ModifiedSD dosen't fit this situation, switch back to normal SD
                                Parameters.modifiedSDfail = true; Parameters.TYPE = "SteepestDescent"; break;
                        }
                    }else{
                        switch ((mSimulation.getSimulationType())){
                            case "SteepestDescent":
                                // if LAMBADA is too small, check to see if the cluster breaks down
                                if(mSimulation.getStepSize() < 0.0001){
                                    if((!Parameters.modifiedSDfail) && (mSystem.clustering() > 1)){
                                        // more than one cluster in the system, switch to ModifiedSD
                                        Parameters.TYPE = "ModifiedSD";
                                    }
                                }
                                break;
                            case "ModifiedSD":
                                // if multiclusters problem fixed, switch back to normal SD
                                if(mSystem.clustering() == 1){
                                    Parameters.TYPE = "SteepestDescent";
                                }
                                break;
                        }
                    }
                    // update display
                    if(mSimulation.getNumOfSteps()%100 == 0 || !Parameters.RUN) {
                        mUIHandler.obtainMessage(UPDATE_GLVIEW, mSystem.getParticlePositions()).sendToTarget();
                        mUIHandler.obtainMessage(UPDATE_DISPLAY, new Data(mSystem,mSimulation)).sendToTarget();
                    }
                }
                mSimulationHandler.post(this);
            }
        });
    }

    @Override
    protected void onLooperPrepared(){
        mSimulationHandler = new Handler();
    }

    @Override
    public boolean quit(){
        mHasQuit = true;
        return super.quit();
    }
}
