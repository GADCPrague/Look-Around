package com.github.whereare;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import java.io.IOException;
import java.util.ArrayList;

public class WhereAreDisplay extends Activity implements SensorEventListener {

    public static final String FRIENDS_DATA = "friendsData";
    public static final String FRIENDS_LOCATIONS = "friendsLocations";
    public static final String MY_LOCATION = "myLocations";
    
    /** half of the visible angle. */
    private static float VISIBLE_AZIMUTH_ANGLE = 30f;
    
    private SensorManager mSensorManager;
    private Sensor mOrientation;

    private float azimuth_angle = 0f;
    private float pitch_angle = 0f;
    private float roll_angle = 0f;
    private Location myLocation;
    private ArrayList<Location> friendsLocations;
    private FriendsView mDraw;
  
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Intent intent = getIntent();
        friendsLocations = 
                intent.getParcelableArrayListExtra(FRIENDS_LOCATIONS);
        myLocation = 
                intent.getParcelableExtra(MY_LOCATION);
        
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mOrientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        Preview mPreview = new Preview(this);
        mDraw = new FriendsView(this);
        setContentView(mPreview);
        addContentView(mDraw, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d("whereare", "Ignoring config change " + newConfig);
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mOrientation, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        azimuth_angle = event.values[0];
        pitch_angle = event.values[1];
        roll_angle = event.values[2];
        Log.d("whereare", "sensor changed " + azimuth_angle + ", " + pitch_angle + ", " + roll_angle);

        mDraw.invalidate();
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // no-op
    }
    
    class Preview extends SurfaceView implements SurfaceHolder.Callback {

        SurfaceHolder mHolder;
        Camera mCamera;

        Preview(Context context) {
            super(context);
            // Install a SurfaceHolder.Callback so we get notified when the 
            // underlying surface is created and destroyed. 
            mHolder = getHolder();
            mHolder.addCallback(this);
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        public void surfaceCreated(SurfaceHolder holder) {
            // The Surface has been created, acquire the camera and tell 
            // it where to draw. 
            mCamera = Camera.open();
            try {
                mCamera.setPreviewDisplay(holder);
            } catch (IOException ex) {
                Log.d("whereare", "cannot set preview display", ex);
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // Surface will be destroyed when we return, so stop the preview. 
            // Because the CameraDevice object is not a shared resource, it's very 
            // important to release it when the activity is paused. 
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.setPreviewCallback(null);
                mCamera.release();
                mCamera = null;
            }
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            // Now that the size is known, set up the camera parameters and begin 
            // the preview. 
            Camera.Parameters parameters = mCamera.getParameters();
//            parameters.setPreviewSize(w, h);
            parameters.setRotation(90);

//            Camera.Size s = p.getSupportedPreviewSizes().get(0);
//            p.setPreviewSize( s.width, s.height );
            
            mCamera.setParameters(parameters);
            mCamera.startPreview();
        }
    }
    
    class FriendsView extends View {

        public FriendsView(Context context) {
            super(context);
            // TODO Auto-generated constructor stub 
        }

        @Override
        protected void onDraw(Canvas canvas) {
            Log.d("whereare", "friends view drawing " + friendsLocations.size());
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.BLACK);
            canvas.drawText("Test Text", 10, 10, paint);
            
            for (Location l : friendsLocations) {
                PositionData position = new PositionData(myLocation, l);
                float angle = position.getBearing() - azimuth_angle;
                if (Math.abs(angle) < VISIBLE_AZIMUTH_ANGLE) {
                    float x = canvas.getWidth() * (angle + VISIBLE_AZIMUTH_ANGLE);
                    canvas.drawLine(x, 0, x, canvas.getHeight(), paint);
                }
            }
            super.onDraw(canvas);
        }
    }
}
