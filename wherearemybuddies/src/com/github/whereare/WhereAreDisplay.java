package com.github.whereare;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import java.io.IOException;

public class WhereAreDisplay extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Preview mPreview = new Preview(this);
        FriendsView mDraw = new FriendsView(this);
        setContentView(mPreview);
        addContentView(mDraw, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d("whereare", "Ignoring config change " + newConfig);
        super.onConfigurationChanged(newConfig);
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
}
