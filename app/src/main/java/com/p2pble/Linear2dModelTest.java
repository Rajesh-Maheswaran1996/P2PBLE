package com.p2pble;

/**
 * Created by rajeshmaheswaran on 27/02/18.
 */

public class Linear2dModelTest {
    public static void main(String args[]){

        Linear2dObservationModel obs = new Linear2dObservationModel();
        Linear2dProcessModel model = new Linear2dProcessModel();
        KalmanFilter filter = new KalmanFilter(model);
        int i,j;

        for (i = 0,j=0; i <= 10 && j<=10; ++i,++j) {
            double time = i;
            obs.setPosition(i,j);
            filter.update(i,obs);
        }

        double x = model.getState()[0][0];
        double v = model.getState()[1][0];


        System.out.println(""+x+" "+v+" "+v+"\n");




    }

}

