package com.example.accelometer;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    private TextView xTextView, yTextView, zTextView;
    private SensorManager sensorManager;
    private Sensor accelometerSensor;
    private boolean isAccelometerAvailable, itIsNotFirstTime = false, isTorchModeOn = false;
    private float currentX, currentY, currentZ, lastX, lastY, lastZ;
    private float xDifference, yDifference, zDifference;
    private float treshHold = 5f;

    private CameraManager cameraManager;
    private String cameraID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        xTextView = findViewById(R.id.xTextView);
        yTextView = findViewById(R.id.yTextView);
        zTextView = findViewById(R.id.zTextView);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        cameraManager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);

        try {
            cameraID = cameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
            accelometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            isAccelometerAvailable = true;
        }else{
            xTextView.setText("No accelerometer sensor");
            isAccelometerAvailable = false;
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onSensorChanged(SensorEvent event) {

        xTextView.setText(event.values[0] + "m/s2");
        yTextView.setText(event.values[1] + "m/s2");
        zTextView.setText(event.values[2] + "m/s2");


        currentX = event.values[0];
        currentY = event.values[1];
        currentZ = event.values[2];

        if (itIsNotFirstTime){

             xDifference = Math.abs(lastX - currentX);
             yDifference = Math.abs(lastY - currentY);
             //zDifference = Math.abs(lastZ - currentZ);


            if (yDifference > treshHold){
                try {
                    cameraManager.setTorchMode(cameraID, true);
                    isTorchModeOn = true;
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
            if (xDifference > treshHold && isTorchModeOn){
                try {
                    cameraManager.setTorchMode(cameraID, false);
                    isTorchModeOn = false;
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

        }

        lastX = currentX;
        lastY = currentY;
        //lastZ = currentZ;
        itIsNotFirstTime = true;

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isAccelometerAvailable){
            sensorManager.registerListener(this, accelometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isAccelometerAvailable){
            sensorManager.unregisterListener(this);
        }
    }
}