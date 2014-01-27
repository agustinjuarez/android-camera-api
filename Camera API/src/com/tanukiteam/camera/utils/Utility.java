package com.tanukiteam.camera.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;

/**
 * Utility class
 * @author ajuarez@tanukiteam.com
 */

public class Utility {

	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance(){
	    Camera c = null;
	    try {
	        c = Camera.open(); // attempt to get a Camera instance
	    }
	    catch (Exception e){
	        // Camera is not available (in use or does not exist)
	    }
	    return c; // returns null if camera is unavailable
	}
	
	/**
	 * Check if device is Tablet
	 * @param Context
	 * @return boolean
	 */
	public static boolean isTablet(Context context) {
	    return (context.getResources().getConfiguration().screenLayout
	            & Configuration.SCREENLAYOUT_SIZE_MASK)
	            >= Configuration.SCREENLAYOUT_SIZE_LARGE;
	}	
	
}
