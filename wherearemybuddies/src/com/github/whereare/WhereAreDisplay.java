package com.github.whereare;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class WhereAreDisplay extends Activity implements SensorEventListener {

    public static final String FRIENDS_DATA = "friendsData";
    public static final String FRIENDS_LOCATIONS = "friendsLocations";
    public static final String MY_LOCATION = "myLocations";
    
    private static final int IMAGE_SIZE = 66;
    
    /** half of the visible angle. */
    private static float MIN_BEARING = -30f;
    private static float MAX_BEARING = 30f;
    
    private SensorManager mSensorManager;
    private Sensor mOrientation;

    private float azimuth_angle = 0f;
    private float pitch_angle = 0f;
    private float roll_angle = 0f;
    private Location myLocation;
    private ArrayList<PositionData> friendsLocations;
    private Map<Uri, Bitmap> imageCache;
    private FriendsView mDraw;
    private Bitmap backgroundBitmap;
  
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
        backgroundBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.image);
        imageCache = new HashMap<Uri, Bitmap>();
        for (PositionData data : friendsLocations) {
            if (data.getContactUri() != null) {
                Bitmap contactPhoto = Contacts.People.loadContactPhoto(
                        this, data.getContactUri(), R.drawable.avatar, null);
                if (contactPhoto != null) {
                    imageCache.put(data.getContactUri(), 
                            Utils.getResizedBitmap(contactPhoto, IMAGE_SIZE, IMAGE_SIZE));
                }
            }
        }
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
            paint.setTextSize(14);
            paint.setColor(Color.BLUE);
            canvas.drawText("azimuth " + azimuth_angle + ", pitch angle " + 
                    pitch_angle + ", roll angle " + roll_angle + " lat " + 
                    myLocation.getLatitude() + " lon " + myLocation.getLongitude(), 10, 10, paint);
            
            float y = 20;
            float markerYPosition = canvas.getHeight() - backgroundBitmap.getHeight();
            SortedSet<PositionData> paintedFriends = new TreeSet(new Comparator<PositionData>() {

                public int compare(PositionData object1, PositionData object2) {
                    int cmp = Float.compare(object2.getDistance(), object1.getDistance());
                    return cmp != 0 ? cmp : object2.getName().compareTo(object1.getName());
                }
            });
            for (PositionData position : friendsLocations) {
                int angle = (int) (position.getBearing() - azimuth_angle);
                while (angle < 0) {
                    angle += 360;
                }
                // this would be right formula if the azimuth would lead in camera diraction
                // (angle + 180) % 360 - 180;
                // skip '+180' to turn it around
                angle = angle % 360 - 180;
                paint.setTextSize(14);
                canvas.drawText(angle + ", lat " + position.getLocation().getLatitude() + 
                        " long " + position.getLocation().getLongitude(), 10, y, paint);
                y += 17;
                if (angle > MIN_BEARING && angle < MAX_BEARING) {
                    paintedFriends.add(position);
                }
            }
            for (PositionData position : paintedFriends) {
                int angle = (int) (position.getBearing() - azimuth_angle);
                while (angle < 0) {
                    angle += 360;
                }
                // this would be right formula if the azimuth would lead in camera diraction
                // (angle + 180) % 360 - 180;
                // skip '+180' to turn it around
                angle = angle % 360 - 180;
                paint.setTextSize(14);
                y += 17;
                float x = canvas.getWidth() * (angle - MIN_BEARING) / (MAX_BEARING - MIN_BEARING);
                canvas.drawLine(x, 0, x, canvas.getHeight(), paint);
                paint.setTextSize(25);
                canvas.drawBitmap(backgroundBitmap, x, markerYPosition, paint);
                Bitmap contactImg = imageCache.get(position.getContactUri());
                if (contactImg != null) {
                    canvas.drawBitmap(contactImg, x + 10, markerYPosition + 10, paint);
                }

                Rect clipBounds = canvas.getClipBounds();
                canvas.clipRect(new RectF(x + 100, markerYPosition, 
                        x + backgroundBitmap.getWidth(), markerYPosition + backgroundBitmap.getHeight()));
                paint.setColor(Color.WHITE);
                paint.setTextSize(30);
                canvas.drawText(position.getName(), x + 105, markerYPosition + 40, paint);
                paint.setTextSize(24);
                canvas.drawText(Utils.formatDistance(position.getDistance()), 
                        x + 105, markerYPosition + backgroundBitmap.getHeight() * 0.9f, paint);
                canvas.clipRect(clipBounds, Region.Op.REPLACE);
                markerYPosition -= backgroundBitmap.getHeight() * 0.7;
            }
            super.onDraw(canvas);
        }
    }
}
