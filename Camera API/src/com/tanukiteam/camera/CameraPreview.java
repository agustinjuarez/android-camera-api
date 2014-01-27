package com.tanukiteam.camera;

import java.io.IOException;
import java.util.List;
import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Camera Preview Auxiliary Class
 * @author ajuarez@tanukiteam.com
 */

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private List<Camera.Size> supportedSizes;
        
    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;
        //Install a SurfaceHolder.Callback so we get notified when the
        //underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        //Deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        //The Surface has been created, now tell the camera where to draw the preview.
        try {
            //Set the display:
        	mCamera.setPreviewDisplay(holder);
        	 
            //Get the parameters:
            Camera.Parameters parameters = mCamera.getParameters();
            
            //Get the supported preview sizes and set them:
            supportedSizes = mCamera.getParameters().getSupportedPreviewSizes();
            parameters.setPreviewSize(supportedSizes.get(0).width,supportedSizes.get(0).height);            
            mCamera.setParameters(parameters);            
            //Start the preview:
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d("ERROR", "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        //Empty, the camera release takes place in the activity. 
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
          // preview surface does not exist
          return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
          // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (Exception e){
            Log.d("ERROR", "Error starting camera preview: " + e.getMessage());
        }
    }
    
    public SurfaceHolder getSurfaceHolder(){
    	return this.mHolder;
    }
    
    
    
}