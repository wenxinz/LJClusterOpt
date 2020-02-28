package com.CU.wenxing.LJClusterOpt.chem.potential;

public class LJPotential {
    public static String TAG = "LJPotential";

    public static double potentialE(double r){
        double r2, r6, temp;
        r2 = r*r;
        r6 = r2*r2*r2;
        temp = 1.0/r6;
        return 4*temp*(temp-1.0);
    }

    public static double gradient(double r){
        double r2, r6, temp;
        r2 = r*r;
        r6 = r2*r2*r2;
        temp = 1.0/r6;
        return -48.0 * temp * (temp - 0.5) / r;
    }
}
