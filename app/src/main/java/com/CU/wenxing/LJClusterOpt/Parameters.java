package com.CU.wenxing.LJClusterOpt;

public class Parameters {
    // debug
    public static boolean LoggerConfigOn = false;

    public static boolean RESTART = false;
    // simulation
    public static boolean RUN = false;
    public static String TYPE = "MonteCarlo";
    public static int NUM_OF_MC_STEPS;
    public static double LAMBADA = 0.001;
    public static double MCSTEP = 0.1;
    public static boolean modifiedSDfail = false;
    // particle
    public static float PARTICLE_SIZE = 0.5f;
    // system
    public static int NUM_OF_PARTICLES = 10;
    public static double GLOBAL_MINIMA;
    public static double BOUNDARY_X = 10.0;
    public static double BOUNDARY_Y = 10.0;
    public static double BOUNDARY_Z = 10.0;
    public static double TEMPERATURE = 0.7;
    public static boolean CLUSTER = false;

}
