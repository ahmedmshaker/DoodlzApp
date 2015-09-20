package com.example.shika.doodlz;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

/**
 * Created by shika on 9/10/2015.
 */

public class DoodleView extends View {

    private Paint paintScreen;
    private Paint paintLine;
    private HashMap<Integer , Path> pathMap;
    private HashMap<Integer , Point> previousMap;
    private Bitmap bitmap;
    private Canvas canvasBitmap;



    public DoodleView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paintScreen=new Paint();

        paintLine=new Paint();
        paintLine.setAntiAlias(true);
        paintLine.setColor(Color.BLACK);
        paintLine.setStyle(Paint.Style.STROKE);
        paintLine.setStrokeWidth(5);
        paintLine.setStrokeCap(Paint.Cap.ROUND);
        pathMap=new HashMap<>();
        previousMap=new HashMap<>();

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        bitmap=Bitmap.createBitmap(getWidth() , getHeight() , Bitmap.Config.ARGB_8888);
        canvasBitmap=new Canvas(bitmap);
        bitmap.eraseColor(Color.WHITE);

    }

    public void clear(){
        pathMap.clear();
        previousMap.clear();
        bitmap.eraseColor(Color.WHITE);
        invalidate();
    }

    public void setDrawingColor(int color){
        paintLine.setColor(color);
    }

    public int getDrawingColor(){
        return paintLine.getColor();
    }


    public void setLineWidth(int width){
        paintLine.setStrokeWidth(width);

    }

    public int getLineWidth(){
        return (int) paintLine.getStrokeWidth();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(bitmap , 0 , 0 , paintScreen);

        for (Integer key : pathMap.keySet()){
            canvas.drawPath(pathMap.get(key) , paintLine);
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action=event.getActionMasked();
        int actionIndex=event.getActionIndex();

        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN){


            touchStarted(event.getX(actionIndex) , event .getY(actionIndex) , event.getPointerId(actionIndex));

        }else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP){

            touchEnded(event.getPointerId(actionIndex));
        }else{

            touchMoved(event);
        }

        invalidate();


        return true;
    }


    private void touchStarted(float x ,float y , int LineID){

        Path path;
        Point point;
        if (pathMap.containsKey(LineID)){
            path=pathMap.get(LineID);
            path.reset();
            point=previousMap.get(LineID);
        }else {
            path=new Path();
            pathMap.put(LineID , path);
            point=new Point();
            previousMap.put(LineID , point);
        }
        path.moveTo(x,y);
        point.x= (int) x;
        point.y= (int) y;

    }

    private void touchMoved(MotionEvent event){

        for (int i = 0; i <event.getPointerCount() ; i++) {

            int pointerId=event.getPointerId(i);
            int pointerIndex=event.findPointerIndex(i);
            if (pathMap.containsKey(pointerId)){
                float newX=event.getX(pointerIndex);
                float newY=event.getY(pointerIndex);

                Path path=pathMap.get(pointerId);
                Point point=previousMap.get(pointerId);

                float deltaX=Math.abs(newX-point.x);
                float deltaY=Math.abs(newY-point.y);

                if (deltaX>=10||deltaY>10){
                    path.quadTo(point.x ,point.y , (newX+point.x)/2 , (newY+point.y)/2);

                    point.x= (int) newX;
                    point.y= (int) newY;
                }
            }
        }

    }
    private void touchEnded(int LineID){

        Path path=pathMap.get(LineID);
        canvasBitmap.drawPath(path , paintLine);
        path.reset();

    }


    public void saveImage(){
        String fileName="doodlz"+System.currentTimeMillis();
        ContentValues contentValues=new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE , fileName);
        contentValues.put(MediaStore.Images.Media.DATE_ADDED , System.currentTimeMillis());
        contentValues.put(MediaStore.Images.Media.MIME_TYPE , "image/jpg");

        Uri uri=getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI ,
                contentValues);

        try {
            OutputStream outputStream=getContext().getContentResolver().openOutputStream(uri);

            bitmap.compress(Bitmap.CompressFormat.JPEG , 100 ,outputStream);

            outputStream.flush();
            outputStream.close();

            Toast.makeText(getContext() , "image Saved = " + uri.toString(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
