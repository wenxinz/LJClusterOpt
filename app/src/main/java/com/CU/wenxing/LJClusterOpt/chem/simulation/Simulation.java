package com.CU.wenxing.LJClusterOpt.chem.simulation;

import com.CU.wenxing.LJClusterOpt.chem.MySystem;

public class Simulation {
    protected int numOfSteps;
    protected double step;
    protected String type;

    protected Simulation(int startingStep, String simType){
        numOfSteps = startingStep;
        type = simType;
    }

    public int getNumOfSteps(){
        return numOfSteps;
    }
    public String getSimulationType(){
        return type;
    }
    public double getStepSize() {return step;}

    public boolean update(MySystem ljSystem){return true;}
    public void stop(MySystem ljSystem){}
}
