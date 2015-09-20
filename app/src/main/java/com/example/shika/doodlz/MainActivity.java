package com.example.shika.doodlz;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;

import java.util.concurrent.atomic.AtomicBoolean;


public class MainActivity extends ActionBarActivity {

    DoodleView doodleView;
    Dialog currentDialog;
    SensorManager sensorManager;
    private float acceleration;
    private float Currentacceleration;
    private float Lastacceleration;

    AtomicBoolean atomicBoolean=new AtomicBoolean();
    private static final int colorID=Menu.FIRST;
    private static final int WidthID=Menu.FIRST+1;
    private static final int EraseID=Menu.FIRST+2;
    private static final int ClearID=Menu.FIRST+3;
    private static final int SaveID=Menu.FIRST+4;
    private static final int AccelertionThreshold=15000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        doodleView= (DoodleView) findViewById(R.id.doodleView);

        acceleration=0.0f;

        Currentacceleration=SensorManager.GRAVITY_EARTH;
        Lastacceleration=SensorManager.GRAVITY_EARTH;

        enableAccelerationLister();
    }

    @Override
    protected void onPause() {
        super.onPause();
        disableAccelerationListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE , colorID , Menu.NONE , "Choose Color");
        menu.add(Menu.NONE , WidthID , Menu.NONE , "Line Width");
        menu.add(Menu.NONE , EraseID , Menu.NONE , "Erase");
        menu.add(Menu.NONE , ClearID , Menu.NONE , "Clear Window");
        menu.add(Menu.NONE , SaveID , Menu.NONE , "Save Image");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){
            case colorID :
                ColorDialog();
                return true;
            case WidthID :
                LineDialog();
                return true;
            case EraseID :
                doodleView.setDrawingColor(Color.WHITE);
                return true;
            case ClearID :
                doodleView.clear();
                return true;
            case SaveID :
                doodleView.saveImage();
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    private void ColorDialog() {
        currentDialog=new Dialog(this);
        currentDialog.setContentView(R.layout.color_dialog);
        currentDialog.setTitle("Set Color");
        currentDialog.setCancelable(true);

        final SeekBar alpha= (SeekBar) currentDialog.findViewById(R.id.alpha);
        final SeekBar red= (SeekBar) currentDialog.findViewById(R.id.red);
        final SeekBar green= (SeekBar) currentDialog.findViewById(R.id.green);
        final SeekBar blue= (SeekBar) currentDialog.findViewById(R.id.blue);

        alpha.setOnSeekBarChangeListener(colorSeekBar);
        red.setOnSeekBarChangeListener(colorSeekBar);
        green.setOnSeekBarChangeListener(colorSeekBar);
        blue.setOnSeekBarChangeListener(colorSeekBar);
        final int color = doodleView.getDrawingColor();
        alpha.setProgress(Color.alpha(color));

        red.setProgress(Color.red(color));
        green.setProgress(Color.green(color));
        blue.setProgress(Color.blue(color));
        final Button setButton= (Button) currentDialog.findViewById(R.id.set_color);
        setButton.setOnClickListener(buttonListener);
        atomicBoolean.set(true);
        currentDialog.show();


    }

    private void LineDialog(){

        currentDialog=new Dialog(this);
        currentDialog.setContentView(R.layout.line_width);
        currentDialog.setTitle("Set Line Width");
        currentDialog.setCancelable(true);

        SeekBar lineSeekBar= (SeekBar) currentDialog.findViewById(R.id.seekLine);
        lineSeekBar.setOnSeekBarChangeListener(WidthListener);
        lineSeekBar.setProgress(doodleView.getLineWidth());


        Button setButton= (Button) currentDialog.findViewById(R.id.lineButton);
        setButton.setOnClickListener(ButtonLine);

        atomicBoolean.set(true);
        currentDialog.show();


    }


    SeekBar.OnSeekBarChangeListener WidthListener = new SeekBar.OnSeekBarChangeListener() {

        Bitmap bitmap=Bitmap.createBitmap(400 , 100 , Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);


        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {


            ImageView imageView= (ImageView) currentDialog.findViewById(R.id.line);

            Paint paint=new Paint();
            paint.setColor(doodleView.getDrawingColor());
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeWidth(progress);

            bitmap.eraseColor(Color.WHITE);
            canvas.drawLine(30 ,50 ,370 ,50 ,paint);
            imageView.setImageBitmap(bitmap);

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    View.OnClickListener ButtonLine = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            SeekBar lineSeekBar= (SeekBar) currentDialog.findViewById(R.id.seekLine);
            doodleView.setLineWidth(lineSeekBar.getProgress());
            atomicBoolean.set(false);
            currentDialog.dismiss();
            currentDialog = null;

        }
    };
    SeekBar.OnSeekBarChangeListener colorSeekBar = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {


            SeekBar alpha= (SeekBar) currentDialog.findViewById(R.id.alpha);
            SeekBar red= (SeekBar) currentDialog.findViewById(R.id.red);
            SeekBar green= (SeekBar) currentDialog.findViewById(R.id.green);
            SeekBar blue= (SeekBar) currentDialog.findViewById(R.id.blue);
            View colorView = (View)currentDialog.findViewById(R.id.colorView);

            colorView.setBackgroundColor(Color.argb(alpha.getProgress() , red.getProgress() , green.getProgress() , blue.getProgress()));

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };



    View.OnClickListener buttonListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            SeekBar alpha= (SeekBar) currentDialog.findViewById(R.id.alpha);
            SeekBar red= (SeekBar) currentDialog.findViewById(R.id.red);
            SeekBar green= (SeekBar) currentDialog.findViewById(R.id.green);
            SeekBar blue= (SeekBar) currentDialog.findViewById(R.id.blue);


            doodleView.setDrawingColor(Color.argb(alpha.getProgress(), red.getProgress(), green.getProgress(), blue.getProgress()));

            atomicBoolean.set(false);
            currentDialog.dismiss();
            currentDialog=null;
        }
    };

    private void enableAccelerationLister(){
        sensorManager= (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(sensorEventListener , sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) , SensorManager.SENSOR_DELAY_NORMAL);

    }

    private void disableAccelerationListener(){
        if (sensorManager != null){
            sensorManager.unregisterListener(sensorEventListener , sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
            sensorManager = null;
        }
    }

    SensorEventListener sensorEventListener=new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {

            if (!atomicBoolean.get()){
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                Lastacceleration = Currentacceleration;
                Currentacceleration = x*x + y*y + z*z ;
                acceleration = Currentacceleration * (Currentacceleration - Lastacceleration);
                if (Currentacceleration > AccelertionThreshold){

                    AlertDialog.Builder builder =new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage( "Are You Sure You Want Erase" );
                    builder.setCancelable(true);
                    builder.setPositiveButton("Erase" , new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            atomicBoolean.set(false);
                            doodleView.clear();
                        }
                    });


                    builder.setNegativeButton("Cancel" , new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            atomicBoolean.set(false);
                            dialog.cancel();
                        }
                    });
                    atomicBoolean.set(true);
                    builder.show();
                }
            }


        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
}

