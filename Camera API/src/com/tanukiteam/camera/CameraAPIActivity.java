package com.tanukiteam.camera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore.Images.Thumbnails;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.VideoView;
import com.tanukiteam.camera.utils.Utility;

/**
 * Main Activity where everything takes place.
 * @author ajuarez@tanukiteam.com
 */

public class CameraAPIActivity extends Activity {
	
	//Camera info
	private android.hardware.Camera.CameraInfo info;
	
	//Tag for debug logs
	protected static final String TAG = "CAMERA";
	
	//Picture
	private Camera mCamera;
    private CameraPreview mPreview;
    
    //Video
    private MediaRecorder mediaRecorder;
    private Bitmap thmbs = null;
    private Boolean reproduced = false;
	//private final int maxDurationInMs = 20000;
	//private final long maxFileSizeInBytes = 500000;
	//private final int videoFramesPerSecond = 20;

    //Audio
    private MediaPlayer mediaPlayer;
    
    //Buttons
    private static ImageButton captureButton;
    private ImageButton flashButton;
    private ImageButton rotateButton;
    private ImageButton closeButton;
    private ImageButton pictureButton;
    private ImageButton videoButton;
    private ImageButton audioButton;
    private ImageButton galleryButton;
    private ImageButton saveButton;
    private ImageButton playButton;
    private static ImageButton retakeButton;
    
    //Layout
    private LinearLayout masterLayout;
    private static FrameLayout previewLayout;
    private FrameLayout frameLayout;
    
    //ImageView
    private static ImageView pictureDisplay;
    private static ImageView videoPreview;
    
    //VideoView
    private VideoView videoDisplay;
    
    //Chronometer
    private Chronometer chrono;
    
    //Media contoller
    private MediaController mc;
    
    //Temporal vars    
    private Bitmap tempBM;
    private String tempPath;
    private String pic ;
    
    //Flags
    private boolean pictureShown;    
    private boolean videoShown;
    private boolean audioShown;
    private boolean isRecordingVideo;
    private boolean isRecordingAudio;
    
    //Activity modes
    public enum Mode {PICTURE,VIDEO,AUDIO};
    private Mode status;
    
    //Flash modes
    private final String[] flashModes = {Camera.Parameters.FLASH_MODE_AUTO, Camera.Parameters.FLASH_MODE_ON, Camera.Parameters.FLASH_MODE_OFF};
    private int fmi = 0; //flash mode index
    
    //Camera ID
    private int currentCamID;
	private final int backCam 	= Camera.CameraInfo.CAMERA_FACING_BACK;
	private final int frontCam 	= Camera.CameraInfo.CAMERA_FACING_FRONT;
	
        
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preview);        
        
        //Create an instance of Camera:
        mCamera = Utility.getCameraInstance();
        
        //Find the layouts --------------------------------------------------
        masterLayout	= (LinearLayout) findViewById(R.id.lyt_master);
        previewLayout	= (FrameLayout) findViewById(R.id.camera_preview);
        frameLayout		= (FrameLayout) findViewById(R.id.frame);
        //-------------------------------------------------------------------        
        
        //Find the buttons --------------------------------------------------
        captureButton 	= (ImageButton) findViewById(R.id.button_capture);
        flashButton 	= (ImageButton) findViewById(R.id.btn_flash);
        rotateButton 	= (ImageButton) findViewById(R.id.btn_rotate);
        closeButton 	= (ImageButton) findViewById(R.id.btn_close);
        pictureButton	= (ImageButton) findViewById(R.id.btn_picture);
        videoButton		= (ImageButton) findViewById(R.id.btn_video);
        audioButton		= (ImageButton) findViewById(R.id.btn_audio);
        galleryButton	= (ImageButton) findViewById(R.id.btn_roll);
        saveButton		= (ImageButton) findViewById(R.id.btn_save);  
        retakeButton	= (ImageButton) findViewById(R.id.button_retake);
        playButton		= (ImageButton) findViewById(R.id.button_play);
        //-------------------------------------------------------------------
        
        //Find the chronometer:
        chrono			= (Chronometer) findViewById(R.id.chronometer);        
        //Find the image view:
        pictureDisplay	= (ImageView) findViewById(R.id.image_preview);
        videoPreview	= (ImageView) findViewById(R.id.video_preview_img);
        //Find the video view:
        videoDisplay 	= (VideoView) findViewById(R.id.video_preview);
        
        //Initialize flags --------------------------------------------------
        pictureShown = false;
        isRecordingVideo = false;
        isRecordingAudio = false;
        
        try{
        	Intent intent = getIntent();
        	status = (Mode) intent.getExtras().get("status");
        	//remains in the state with which the CameraCustomGalleryActivity activity began
        	if(status.ordinal()==0) showPicture();
        	if(status.ordinal()==1) videoMode();
        	if(status.ordinal()==2) audioMode();
        }catch(Exception e){
        	//Default status
        	status = Mode.PICTURE;
        }
        //-------------------------------------------------------------------
        
        //Set auto-focus:
		Camera.Parameters params = mCamera.getParameters();
		params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);        
        
		//Set Display Orientation:
		setCameraDisplayOrientation(this, backCam, mCamera);
		
        //Create our Preview view and set it as the content of our activity:
        mPreview = new CameraPreview(this, mCamera);        
        previewLayout.addView(mPreview);                
        
        //Adjust Aspect Ratio:
        //adjustAspectRatio();        
        
        //If coming back from gallery mode:
        try{
	        Intent intent = getIntent();
	        pic = (String) intent.getExtras().get("picture"); //Get the picture from the intent dictionary 
	        tempBM = BitmapFactory.decodeFile(pic);
	        showPicture();
         }catch(Exception e){}        
        
        //ATTACH LISTENERS --------------------------------------------------

        //Capture picture/video/audio:
        captureButton.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                	capture();
                }
            }
        );
        
        //Set flash mode:
        flashButton.setOnClickListener(
        		new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {						
						setFlashMode();
					}
				}
        );
        
        //Switch camera (back/front):
        rotateButton.setOnClickListener(
        		new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						switchCamera();
					}
				}
        );
        
        //Close the camera:
        closeButton.setOnClickListener(
        		new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						//Close the app:
						finish();
					}
				}
        );
        
        //Change to picture taking:
        pictureButton.setOnClickListener(
        		new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						//call method to start picture mode
						pictureMode();
					}
				}
        );
        
        //Change to video recording:
        videoButton.setOnClickListener(
        		new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						//call method to start video mode
						videoMode();
					}
				}
        );
        
        //Change to audio recording:
        audioButton.setOnClickListener(
        		new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						//call method to start audio mode
						audioMode();
					}
				}
        );
        
        //Pick from gallery:
        galleryButton.setOnClickListener(
        		new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						openGalleryPageActivity();
					}
				}
        );
        
        //Save picture:
        saveButton.setOnClickListener(
        		new View.OnClickListener() {					
					@Override
					public void onClick(View v) {
						if(pictureShown)							
							savePicture();
						else if(videoShown)
							saveVideo();
						else if(audioShown)
							saveAudio();
					}
				}
        );
        
        //Retake picture:
        retakeButton.setOnClickListener(
        		new View.OnClickListener() {					
					@Override
					public void onClick(View v) {
				    	switch(status)
				    	{
				    		case PICTURE:
				    		{
				    			discardPicture();
				    			break;
				    		}
				    		case VIDEO:
				    		{
				    			discardVideo();
				    			break;
				    		}
				    		case AUDIO:
				    		{
				    			discardAudio();
				    			break;
				    		}
				    	}  												
					}
				}
        ); 
        
        playButton.setOnClickListener(
        		new View.OnClickListener() {					
					@Override
					public void onClick(View v) {
						if(status == Mode.VIDEO)
						{
							playVideo();
						}
						else if(status == Mode.AUDIO)
						{
							playAudio();
						}
					}
				}
        );
        
        videoDisplay.setOnCompletionListener(
        		new MediaPlayer.OnCompletionListener() {					
					@Override
					public void onCompletion(MediaPlayer mp) {
						reproduced = true;
						showVideo();						
					}
				}
        );
       
        //-------------------------------------------------------------------

    }    
    
    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();		  // release the media recorder immediately on pause event
        releaseCamera();              // release the camera immediately on pause event
    }
   
    /**
     * Releases the camera for other applications.
     * @param	void
     * @return 	void
     * @author 	ajuarez@tanukiteam.com
     */    
    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();
            mCamera = null;
        }
    }    

    /**
     * Releases the media recorder for other applications.
     * @param	void
     * @return 	void
     * @author 	ajuarez@tanukiteam.com
     */    
    private void releaseMediaRecorder(){
        if (mediaRecorder != null) {
            mediaRecorder.reset();   // clear recorder configuration
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }   
    
    //MODE SETTING ---------------------------------------------------------------------
    /**
     * Sets the layout and parameters for taking pictures.
     * @param	void
     * @return 	void
     * @author 	ajuarez@tanukiteam.com
     */
    private void pictureMode(){
    	//if you record a video and then click picture button the video will be deleted.
    	if(videoShown) resetVideoStatus(true);
    	
		//Set the appropriate image resources:
    	captureButton.setImageResource(R.drawable.camera_take_icon);
		pictureButton.setImageResource(R.drawable.camera_white_active);
		videoButton.setImageResource(R.drawable.video_icon);
		audioButton.setImageResource(R.drawable.audio_white);
		
		//Remove background image and show preview pane:		
		masterLayout.setBackgroundDrawable(null);		
		previewLayout.setVisibility(View.VISIBLE);
		
		//Show and hide buttons:
		if(currentCamID != frontCam) 
			flashButton.setVisibility(View.VISIBLE);
		else 
			flashButton.setVisibility(View.INVISIBLE);
		
		rotateButton.setVisibility(View.VISIBLE);
		pictureDisplay.setVisibility(View.GONE);
		
		//Set the needed parameters such as focus mode.
		Camera.Parameters params = mCamera.getParameters();
		params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);	    	
		
		//Disable mode button:
		pictureButton.setEnabled(false);
		//Enable mode buttons:
		videoButton.setEnabled(true);
		audioButton.setEnabled(true);
		
		status = Mode.PICTURE;
    }

    /**
     * Sets the layout and parameters for recording video.
     * @param	void
     * @return 	void
     * @author 	ajuarez@tanukiteam.com
     */    
    private void videoMode(){
    	//if you take a photo and then click video button the photo taken will be deleted.
    	if(pictureShown) resetPictureStatus();
			
		//Set the appropriate image resources:
    	captureButton.setImageResource(R.drawable.record_icon);
		pictureButton.setImageResource(R.drawable.camera_icon);
		videoButton.setImageResource(R.drawable.video_white_active);
		audioButton.setImageResource(R.drawable.audio_white);	
		
		//Remove background image and show preview pane:
		masterLayout.setBackgroundDrawable(null);
		previewLayout.setVisibility(View.VISIBLE);
		
		//Show or hide buttons:
		if(currentCamID != frontCam) 
			flashButton.setVisibility(View.VISIBLE);
		else
			flashButton.setVisibility(View.INVISIBLE);
		
		chrono.setVisibility(View.INVISIBLE); //Hide the chronometer
		rotateButton.setVisibility(View.VISIBLE);
		pictureDisplay.setVisibility(View.GONE);

		//Disable mode button:
		videoButton.setEnabled(false);
		//Enable mode buttons:		
		pictureButton.setEnabled(true);
		audioButton.setEnabled(true);		
		
		status = Mode.VIDEO;
    }

    /**
     * Sets the layout and parameters for recording audio.
     * @param	void
     * @return 	void
     * @author 	ajuarez@tanukiteam.com
     */    
    private void audioMode(){
    	
    	//if you take a photo and then click audio button the photo taken will be deleted.
    	if(pictureShown) resetPictureStatus();
    	//if you record a video and then click audio button the video will be deleted.
    	if(videoShown) resetVideoStatus(true);
    	
    	//Quit video thumb
    	playButton.setVisibility(View.INVISIBLE);
		videoPreview.setVisibility(View.INVISIBLE);
		
		//Set the appropriate image resources:
    	captureButton.setImageResource(R.drawable.record_icon);
		pictureButton.setImageResource(R.drawable.camera_icon);
		videoButton.setImageResource(R.drawable.video_icon);
		audioButton.setImageResource(R.drawable.sound_icon_active);
		
		//Change background and hide preview pane:
		masterLayout.setBackgroundResource(R.drawable.fullscreen_audio);
		previewLayout.setVisibility(View.INVISIBLE);
		chrono.setVisibility(View.INVISIBLE); //Hide the chronometer
		
		//Hide unused buttons:
		flashButton.setVisibility(View.INVISIBLE);
		rotateButton.setVisibility(View.INVISIBLE);   
		pictureDisplay.setVisibility(View.GONE);
		
		//Disable mode button:
		audioButton.setEnabled(false);
		//Enable mode buttons:		
		videoButton.setEnabled(true);
		pictureButton.setEnabled(true);			
		
		status = Mode.AUDIO;
    }    
    
    /**
     * Calls the gallery mode activity
     * @param	void
     * @return	void
     */
    private void openGalleryPageActivity(){
		Intent intent = new Intent(this, CameraCustomGalleryActivity.class);
		intent.putExtra("status", status);
		startActivity(intent);
		finish();
	}    
    //----------------------------------------------------------------------------------
    
    private PictureCallback mPicture = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
        	tempBM = BitmapFactory.decodeByteArray(data, 0, data.length);        	        	
        	showPicture();
        }
    };  
    
    /**
     * Capture picture, video, or audio - depending on the set mode.
     * @param	void
     * @return 	void
     * @author 	ajuarez@tanukiteam.com
     */    
    private void capture(){
    	//CHECK MODE (PICTURE/VIDEO/AUDIO)
    	switch(status){
    		case PICTURE:
    		{
                mCamera.takePicture(null, null, mPicture);
    			break;
    		}
    		case VIDEO:
    		{
    			recordVideo();    			
    			break;
    		}
    		case AUDIO:
    		{
    			recordAudio();
    			break;
    		}
    	}    	
    }
    
    /**
     * Switch between flash modes (ON/AUTO/OFF)
     * @param	void
     * @return 	void
     * @author 	ajuarez@tanukiteam.com
     */ 
    private void setFlashMode(){
		Camera.Parameters params = mCamera.getParameters();
		switch(fmi){
		case 0: //IF Flash AUTO
			{
				fmi = 1; //Flash ON
				flashButton.setImageResource(R.drawable.flash);
				break;
			}
		case 1: //IF Flash ON
			{
				fmi = 2; //Flash OFF
				flashButton.setImageResource(R.drawable.flash_disabled);
				break;
			}
		case 2: //IF Flash OFF
			{
				fmi = 0; //Flash AUTO
				flashButton.setImageResource(R.drawable.flash_auto);
				break;
			}
		default:
			{
				fmi = 0; //Flash AUTO
				flashButton.setImageResource(R.drawable.flash_auto);
				break;
			}
		}				
		
		if(status == Mode.VIDEO && fmi == 1) //If in video mode, flash on == torch
			params.setFlashMode(Parameters.FLASH_MODE_TORCH);
		else
			params.setFlashMode(flashModes[fmi]);						
		
		Log.d(TAG,params.getFlashMode());    	
		
		//Set the new parameters to the camera:
		mCamera.setParameters(params);
    }
    
    /**
     * Switch camera (back/front)
     * @param	void
     * @return 	void
     * @author 	ajuarez@tanukiteam.com
     */    
    private void switchCamera(){
		mCamera.stopPreview();
		mCamera.release();
		
		if(currentCamID == backCam){
		    currentCamID = frontCam;
		    flashButton.setVisibility(View.INVISIBLE);
		}
		else {
		    currentCamID = backCam;
		    flashButton.setVisibility(View.VISIBLE);
		}						
		
		mCamera = Camera.open(currentCamID);
		
		setCameraDisplayOrientation(CameraAPIActivity.this, currentCamID, mCamera);
								
	    try {
			mCamera.setPreviewDisplay(mPreview.getSurfaceHolder());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	    mCamera.startPreview();	    	
    }
    
    /**
     * Shows the picture that was just taken.
     * @param	byte[] (picture data)
     * @return 	void
     * @author 	ajuarez@tanukiteam.com
     */      
    private void showPicture(){
    	if(pic!=null) tempBM = BitmapFactory.decodeFile(pic);
    	Bitmap bMapRotate;
    	Matrix matrix = new Matrix();
    	if(info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
    		float[] mirrorY = { -1, 0, 0, 0, 1, 0, 0, 0, 1};
		    Matrix matrixMirrorY = new Matrix();
		    matrixMirrorY.setValues(mirrorY);
	        matrix.postConcat(matrixMirrorY);
	        bMapRotate = Bitmap.createBitmap(tempBM, 0, 0, tempBM.getWidth(),
            		tempBM.getHeight(), matrix, true);
        	
    	}
    	
    	if(Utility.isTablet(this)) {
    		 int orient = getResources().getConfiguration().orientation;
	    	
	        if(orient==1){
	        	matrix.postRotate(90);
	        	bMapRotate = Bitmap.createBitmap(tempBM, 0,0,tempBM.getWidth(),
	            		tempBM.getHeight(), matrix ,true);
	        	tempBM = bMapRotate;
	        }
	        
    	}else{
	    	//Get Orientation:
	    	int orientation;
	    	
	        if(tempBM.getHeight() < tempBM.getWidth()){
	            orientation = 90;
	        } else {
	            orientation = 0;
	        }
	        if (orientation != 0) {
	        	matrix.postRotate(orientation);
	        	bMapRotate = Bitmap.createBitmap(tempBM, 0, 0, tempBM.getWidth(),
	            		tempBM.getHeight(), matrix, true);
	        	
	        } else
	            bMapRotate = Bitmap.createScaledBitmap(tempBM, tempBM.getWidth(),
	            		tempBM.getHeight(), true);
	    	
	        tempBM = bMapRotate;
    	}
        //Shows picture by setting bitmap into the pictureDisplay view:
    	pictureDisplay.setImageBitmap(tempBM);        
    	//Shows the picture display:
        pictureDisplay.setVisibility(View.VISIBLE);
        //Hides the preview pane:
        previewLayout.setVisibility(View.GONE);
        //Hide unused buttons:
        captureButton.setVisibility(View.GONE);
        retakeButton.setVisibility(View.VISIBLE);
        //Set flag:
        pictureShown = true;
    } 
        
    /**
     * Saves the picture into the SD card.
     * @param	byte[] (picture data)
     * @return 	void
     * @author 	ajuarez@tanukiteam.com
     */     
    private void savePicture(){
        FileOutputStream out;
        try {
            out = new FileOutputStream(
                    String.format("/sdcard/%d.jpg", System.currentTimeMillis()));
            tempBM.compress(Bitmap.CompressFormat.JPEG, 90, out);
            if (tempBM != null) {
            	tempBM.recycle();
            	tempBM = null;
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    	//Calls reset method to reboot picture mode:
        resetPictureStatus();
    }      

    /**
     * Saves the video into the SD card.
     * @param	
     * @return 	void
     * @author 	ajuarez@tanukiteam.com
     */     
    private void saveVideo(){

    	//Video is already saved after capturing it, it is deleted from the SD card if 'retake' is pressed.
    	
    	//Calls reset method to reboot picture mode:
        resetVideoStatus(false);
    }       
    
    /**
     * Saves the video into the SD card.
     * @param	
     * @return 	void
     * @author 	ajuarez@tanukiteam.com
     */     
    private void saveAudio(){

    	//Video is already saved after capturing it, it is deleted from the SD card if 'retake' is pressed.
    	
    	//Calls reset method to reboot picture mode:
        resetAudioStatus(false);
    }       
        
    
    /**
     * Shows dialog with Y/N to discard picture.
     * @param	void
     * @return 	void
     * @author 	ajuarez@tanukiteam.com
     */     
    private void discardPicture(){
        //Ask the user if they want to quit
        new AlertDialog.Builder(this)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle("CONFIRM")
        .setMessage("Are you sure you want to discard this picture?")
        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
            	//Calls reset method to reboot picture mode:
            	resetPictureStatus();
            }

        })
        .setNegativeButton("No", null)
        .show();    	
        
    }

    /**
     * Shows dialog with Y/N to discard video.
     * @param	void
     * @return 	void
     * @author 	ajuarez@tanukiteam.com
     */     
    private void discardVideo(){
        //Ask the user if they want to quit
        new AlertDialog.Builder(this)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle("CONFIRM")
        .setMessage("Are you sure you want to discard this video?")
        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
            	//Calls reset method to reboot picture mode:
            	resetVideoStatus(true);
            }

        })
        .setNegativeButton("No", null)
        .show();    	
        
    }
    
    /**
     * Shows dialog with Y/N to discard video.
     * @param	void
     * @return 	void
     * @author 	ajuarez@tanukiteam.com
     */     
    private void discardAudio(){
        //Ask the user if they want to quit
        new AlertDialog.Builder(this)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle("CONFIRM")
        .setMessage("Are you sure you want to discard this recording?")
        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
            	//Calls reset method to reboot picture mode:
            	resetAudioStatus(true);
            }

        })
        .setNegativeButton("No", null)
        .show();    	
        
    }    

    /**
     * Empties the temp variable, tries calling garbage collector.
     * @param	void
     * @return 	void
     * @author 	ajuarez@tanukiteam.com
     */  
    private void resetPictureStatus(){
    	//Empty the temp variable:
    	tempBM = null;
    	//Removes picture from picturedisplay view:
    	pictureDisplay.setImageBitmap(null);
    	//Show/Hide appropriate layouts:                
        pictureDisplay.setVisibility(View.GONE);
        previewLayout.setVisibility(View.VISIBLE);        
        captureButton.setVisibility(View.VISIBLE);
        retakeButton.setVisibility(View.GONE);   
        //Set picture mode:
        pictureMode();
        galleryButton.setEnabled(true); //Enable gallery button back again
        //Set flag:
        pictureShown = false;           
    	//Try free some memory:
    	System.gc();        
        //Start preview again:
        mCamera.startPreview();     	
    }

    /**
     * Deletes the file just recorded.
     * @param	void
     * @return 	void
     * @author 	ajuarez@tanukiteam.com
     */  
    private void resetVideoStatus(Boolean delete){
    	
    	if(delete)
    	{
	    	//Delete the file
	    	File file = new File(tempPath);
	    	
	    	try
	    	{
	    		file.delete();
	    	}
	    	catch(Exception e)
	    	{
	    		//TODO warning
	    	}
    	}
    	
    	//Turn off flag:
    	videoShown = false;
    		
		//Add:
		frameLayout.addView(previewLayout);
		frameLayout.addView(pictureDisplay);
	
		//Show layouts:
		previewLayout.setVisibility(View.VISIBLE); 
		pictureDisplay.setVisibility(View.VISIBLE);		
		
		//Show video layout:
		videoDisplay.setVisibility(View.GONE);		
		
		//Hide buttons:
		captureButton.setVisibility(View.VISIBLE);
		
		//Hide buttons:
		playButton.setVisibility(View.GONE);
		retakeButton.setVisibility(View.GONE);
		
		videoMode();
    }    

    /**
     * Deletes the file just recorded.
     * @param	void
     * @return 	void
     * @author 	ajuarez@tanukiteam.com
     */  
    private void resetAudioStatus(Boolean delete){
    	if(delete)
    	{
	    	//Delete the file
	    	File file = new File(tempPath);
	    	
	    	try
	    	{
	    		file.delete();
	    	}
	    	catch(Exception e)
	    	{
	    		//TODO warning
	    	}
    	}
    	
    	//Turn off flag:
    	audioShown = false;	
		
		//Show buttons:
		captureButton.setVisibility(View.VISIBLE);
		
		//Hide buttons:
		playButton.setVisibility(View.GONE);
		retakeButton.setVisibility(View.GONE);
		
		audioMode();
    }        
    
    /**
     * Prepares the video recorder
     * @param	void
     * @return 	boolean
     * @author 	ajuarez@tanukiteam.com
     */     
	public boolean prepareVideoRecorder(){
		//Set File Name:
		String filename;
		String path; 
		Date date 	= new Date();
		path 		= Environment.getExternalStorageDirectory().getAbsolutePath().toString();			    			
		filename	= "/rec"+date.toString().replace(" ", "_").replace(":", "_")+".mp4";
		tempPath	= path+filename; //Save it in the temp var to access it later in video preview
		
		mediaRecorder = new MediaRecorder();
		
	    //Step 1: Unlock and set camera to MediaRecorder
		mCamera.stopPreview(); //Stop preview from picture mode.
		mCamera.unlock();
		
		if(info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
			mediaRecorder.setOrientationHint(270);
		}else if(Utility.isTablet(this)){
			int orient = getResources().getConfiguration().orientation;
	        if(orient==2){
	        	//nothing
	        }else{
	        	mediaRecorder.setOrientationHint(90);
	        }
		}else{
			mediaRecorder.setOrientationHint(90);
		}
		
		mediaRecorder.setCamera(mCamera);
		
		 //Step 2: Set sources
	    mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
	    mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
	    //Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
	    mediaRecorder.setProfile(CamcorderProfile.get(1,CamcorderProfile.QUALITY_HIGH));

			//Step 3b: (if API Level < 8)
			/*
			mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);			
			mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
			mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			*/		    
	    
        //Extra
	    //mediaRecorder.setMaxDuration(maxDurationInMs);			
		//mediaRecorder.setVideoFrameRate(videoFramesPerSecond);			
		//mediaRecorder.setMaxFileSize(maxFileSizeInBytes);
	    //mediaRecorder.setVideoSize(640,480);
		//mediaRecorder.setVideoSize(60,80); //surfaceView.getWidth(), surfaceView.getHeight()
		//-----	    
	    
	    //Step 4: Set output file
	    mediaRecorder.setOutputFile(path+filename);		    			
		
	    //Step 5: Set the preview output
		mediaRecorder.setPreviewDisplay(mPreview.getSurfaceHolder().getSurface());

	    //Step 6: Prepare configured MediaRecorder
	    try {
	        mediaRecorder.prepare();
	    } catch (IllegalStateException e) {
	        Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
	        releaseMediaRecorder();
	        return false;
	    } catch (IOException e) {
	        Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
	        releaseMediaRecorder();
	        return false;
	    }
	           		
		return true;	
	}
    
    /**
     * Starts/stops recording video.
     * @param	void
     * @return 	void
     * @author 	ajuarez@tanukiteam.com
     */     
	public void recordVideo(){		
		if (isRecordingVideo) {
			//Stop chronometer:
			chrono.stop();
			//chrono.setVisibility(View.INVISIBLE); //Hide the chronometer
			
			//show flash and camera switch buttons:
			flashButton.setVisibility(View.VISIBLE);
			rotateButton.setVisibility(View.VISIBLE);			
			
			//Stop recording and release camera:
            mediaRecorder.stop();	// Stop the recording
            releaseMediaRecorder(); // Release the MediaRecorder object
            mCamera.lock();        	// Take camera access back from MediaRecorder
            
            isRecordingVideo = false;
            
            showVideo();
            
        } else {
            //Initialize video camera
            if (prepareVideoRecorder()) {
                //Camera is available and unlocked, MediaRecorder is prepared,                
                mediaRecorder.start();
                
                //hide flash and camera switch buttons:
    			flashButton.setVisibility(View.INVISIBLE);
    			rotateButton.setVisibility(View.INVISIBLE);                
                
                //Start chronometer:
                chrono.setBase(SystemClock.elapsedRealtime()); //set chronometer to 0
                chrono.setVisibility(View.VISIBLE); //show the chronometer
                chrono.start();
                
                
                isRecordingVideo = true;

            } else {
                //Prepare didn't work, release the camera
                releaseMediaRecorder();
            }
        }		
	}    
	
	/**
	 * Shows video thumb and play button
	 * @param	void
	 * @return	void
	 * @author	
	 */
	private void showVideo(){
		//show video thumb
		videoPreview.setVisibility(View.VISIBLE);
		thmbs = ThumbnailUtils.createVideoThumbnail(tempPath , Thumbnails.MINI_KIND);
		if (thmbs == null) {
			videoPreview.setImageResource(R.drawable.unsupported_file_image);
		} else {
			videoPreview.setImageBitmap(thmbs);
		}
        
		//Turn on flag:
		videoShown = true;
		
		//Hide layouts:
		previewLayout.setVisibility(View.INVISIBLE); 
		pictureDisplay.setVisibility(View.INVISIBLE);
		
		//Remove: 
		frameLayout.removeView(previewLayout);
		frameLayout.removeView(pictureDisplay);
		frameLayout.removeView(videoPreview);
		frameLayout.addView(videoPreview);
		frameLayout.removeView(playButton);
		frameLayout.addView(playButton);
		
		//Show video layout:
		if(reproduced) videoDisplay.setVisibility(View.GONE);
		else videoDisplay.setVisibility(View.VISIBLE);
		
		//Hide buttons:
		captureButton.setVisibility(View.INVISIBLE);
		flashButton.setVisibility(View.INVISIBLE);
		rotateButton.setVisibility(View.INVISIBLE);
		
		//Show buttons:
		playButton.setVisibility(View.VISIBLE);
		retakeButton.setVisibility(View.VISIBLE);
		
		//Media controller for play/pause
		mc = new MediaController(this);
		videoDisplay.setMediaController(mc);		
		
		//Set the path:
		videoDisplay.setVideoPath(tempPath);
		//Request focus:
		videoDisplay.requestFocus();
	}
	
	/**
	 * Shows a preview of the video
	 * @param	void
	 * @return	void
	 * @author	ajuarez@tanukiteam.com
	 */
	private void playVideo(){			
		//Hide button:
		videoDisplay.setVisibility(View.VISIBLE);
		playButton.setVisibility(View.GONE);
		videoPreview.setVisibility(View.GONE);
		frameLayout.removeView(videoPreview);
		
		//Start the video:
		videoDisplay.start();
		
	}

    /**
     * Prepares the audio recorder
     * @param	void
     * @return 	boolean
     * @author 	ajuarez@tanukiteam.com
     */     
	public boolean prepareAudioRecorder(){
		//Set File Name:
		String filename;
		String path;
		Date date 	= new Date();
		path 		= Environment.getExternalStorageDirectory().getAbsolutePath().toString();			    			
		filename	= "/rec"+date.toString().replace(" ", "_").replace(":", "_")+".3gp";
		tempPath	= path+filename; //Save it in the temp var to access it later in video preview
		
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(path+filename);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

	    try {
	        mediaRecorder.prepare();
	    } catch (IllegalStateException e) {
	        Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
	        releaseMediaRecorder();
	        return false;
	    } catch (IOException e) {
	        Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
	        releaseMediaRecorder();
	        return false;
	    }
	           		
		return true;	
	}	
	
	/**
	 * Starts/stops recording audio.
	 * @param	void
	 * @return	void
	 * @author	ajuarez@tanukiteam.com
	 */
	private void recordAudio(){
		if (isRecordingAudio) {  
			//Stop chronometer:
			chrono.stop();			
			//chrono.setVisibility(View.INVISIBLE); //Hide the chronometer
			
			//Stop recording and release media recorder
            mediaRecorder.stop();	// Stop the recording
            releaseMediaRecorder(); // Release the MediaRecorder object            
            
            isRecordingAudio = false;
            
            showAudio();
            
        } else {
            //Initialize audio
            if (prepareAudioRecorder()) {
                //Start chronometer:                
            	chrono.setBase(SystemClock.elapsedRealtime()); //set chronometer to 0
                chrono.setVisibility(View.VISIBLE); //Show the chronometer
                chrono.start();
                                
            	//Camera is available and unlocked, MediaRecorder is prepared,                
                mediaRecorder.start();          
                isRecordingAudio = true;
            } else {
                //Prepare didn't work, release the camera
                releaseMediaRecorder();
            }
        }			
	}
	
	/**
	 * Shows the preview ready audio
	 * @param
	 * @return	void
	 * @author	ajuarez@tanukiteam.com
	 */
	private void showAudio(){
		
		audioShown = true;
		
		captureButton.setVisibility(View.GONE);
		playButton.setVisibility(View.VISIBLE);
		retakeButton.setVisibility(View.VISIBLE);
		
		Uri myUri =	Uri.parse(tempPath);		
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		try {
			mediaPlayer.setDataSource(getApplicationContext(), myUri);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			mediaPlayer.prepare();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
	}
	
	/**
	 * Plays a preview of the audio
	 * @param	void
	 * @return	void
	 * @author	ajuarez@tanukiteam.com
	 */
	private void playAudio(){
		mediaPlayer.start();
	}
	
	/**
	 * Adjusts the aspect ratio according to device's screen dimensions
	 * @return void
	 * @author ajuarez@tanukiteam.com
	 */
	private void adjustAspectRatio(){
		double 	ratioI, ratioS;
		int		widthI, heightI, widthS,heightS, widthF, heightF;

		Display display = getWindowManager().getDefaultDisplay(); 		
		
		//Preview pane W & H:
		widthI 	= mCamera.getParameters().getPreviewSize().width;
		heightI = mCamera.getParameters().getPreviewSize().height;
		//Screen W & H:
		widthS	= display.getWidth();
		heightS	= display.getHeight();
		
		ratioI	= widthI/heightI;
		ratioS	= widthS/heightS;
		
		if(ratioS>ratioI)
		{
			widthF	= widthI * heightS/heightI;
			heightF	= heightS;
		}
		else
		{
			widthF	= widthS;
			heightF	= heightI * widthS/widthI;
		}
		
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(widthF, heightF);
		previewLayout.setLayoutParams(lp);		
	}
	
	/**
	 * Sets the camera display orientation correctly according to device and camera.
	 * @params
	 * @return	void
	 */
    private void setCameraDisplayOrientation(Activity activity,
            int cameraId, android.hardware.Camera camera) {
        info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }    
            
}