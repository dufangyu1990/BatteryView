package com.example.batteryview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private CustomView customView;
    private boolean testTheadFlag = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        customView = (CustomView) this.findViewById(R.id.batteryView);
        customView.setContentValue(100);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (testTheadFlag)
                {
                    int value = getRandomValue(0,100);
                    Log.d("dfy","value = "+value);
                    customView.setContentValue(value);
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }



            }
        }).start();
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        testTheadFlag  = false;
        customView.setThreadStop();

    }

    public  int getRandomValue(int m, int n)
    {
        int value = (int)(m+Math.random()*(n-m+1));
        return value;
    }

}
