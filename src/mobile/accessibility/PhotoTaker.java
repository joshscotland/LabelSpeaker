/**
 * @author Abdelelah Salama(engobada@cs.washington.edu)
 * @author Chung Han(han@cs.washington.edu)
 * @author Nikhil Karkarey(nikhilkarkarey@gmail.com)
 * 
 * Talking Memories - MenuView
 * 
 * Designed to meet the requirements of the Winter 2012 UW course, 
 * CSE 481H: Accessibility Capstone
 * 
 * Displays camera preview on the screen
 */

package mobile.accessibility;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnClickListener;

public class PhotoTaker extends Activity implements SurfaceHolder.Callback, ShutterCallback,
														PictureCallback, OnClickListener{
	
	private static final String FILE_NUMBER = "fileNum";
	private static final String TAG = "PHOTO_TAKER";
	private SharedPreferences mPreferences;
	private static final String picFileName = "tm_file";
	private GestureDetector gestureDetector;
	public static final String PREF_NAME = "myPreferences";
	public static final String VERBOSE_INST = "Camera View. Single click screen to take photo, or two finger click to return to home screen.";
	public static final String INST_SHORT = "Camera View.";
	

	//DATABASE globals
	DbHelper mHelper;
	SQLiteDatabase mDb;
	Cursor mCursor;

    View.OnTouchListener gestureListener;
    Camera mCamera;
	SurfaceView mPreview;
	
	int requestCode;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Utility.getMediaPlayer() != null) {
			Utility.getMediaPlayer().reset();
		}
		
		setContentView(R.layout.clickpicture);
		
		//Initialize database
		mHelper = new DbHelper(this);
		//Open data base Connections
		mDb = mHelper.getWritableDatabase();
		String[] columns = new String[] {"_id", DbHelper.COL_IMG, DbHelper.COL_AUD};
		mCursor = mDb.query(DbHelper.TABLE_NAME, columns, null, null, null, null, null);
		
		// Gesture detection
        gestureDetector = new GestureDetector(new MyGestureDetector());
        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
            	int action = event.getAction();
				if ((action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) && event.getPointerCount() == 2) {
					finish();
					return true;
				}
				else if (gestureDetector.onTouchEvent(event))
                    return true;
                else
                    return false;
            }
        };
		        
		mPreview = (SurfaceView)findViewById(R.id.mPreview);
		mPreview.setOnClickListener(this);
		mPreview.setOnTouchListener(gestureListener);
        mPreview.getHolder().addCallback(this);
		mPreview.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
		mPreferences = getSharedPreferences(LabelSpeakerActivity.PREF_NAME, Activity.MODE_PRIVATE);
		mCamera = Camera.open();
		Utility.playInstructionsMP(this, R.raw.camlonginst, R.raw.camshortinst, mPreferences);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		mCamera.stopPreview();
	} 
	
	public void onRestart(){
		super.onRestart();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		mCamera.release();
		Log.d(TAG, "DESTROY");
		mDb.close();
		mCursor.close();
	}
	
	// Surface call back methods
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Camera.Parameters params = mCamera.getParameters();
		List<Camera.Size> sizes = params.getSupportedPreviewSizes();
		Camera.Size selectedSize = sizes.get(0);
		params.setPreviewSize(selectedSize.width, selectedSize.height);
		mCamera.setParameters(params);
		mCamera.startPreview();
		
	}

	public void surfaceCreated(SurfaceHolder holder) {
		try {
			
		Camera.Parameters parameters = mCamera.getParameters();
				
		if (this.getResources().getConfiguration().orientation !=
		Configuration.ORIENTATION_LANDSCAPE) {
		// This is an undocumented although widely known feature
		parameters.set("orientation", "portrait");
		// For Android 2.2 and above
		//mCamera.setDisplayOrientation(90);
		// Uncomment for Android 2.0 and above
		parameters.setRotation(90);
		} else {
		// This is an undocumented although widely known feature
		parameters.set("orientation", "landscape");
		// For Android 2.2 and above
		//mCamera.setDisplayOrientation(0);
		// Uncomment for Android 2.0 and above
		parameters.setRotation(0);
		}
		mCamera.setParameters(parameters);
		mCamera.setPreviewDisplay(mPreview.getHolder());
		} catch (IOException exception) {
		mCamera.release();
		Log.v(TAG,exception.getMessage());
		}
		mCamera.startPreview();
		
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
	}
	
	/*
	 * Action on clicking preview screen
	 * Snaps a picture with callback only when JPEG image ready
	 */
	public void onClick(View v) {
	}

	public void onPictureTaken(byte[] data, Camera camera) {		
		int fileNumber = getCurrentFileNumber();
					
		try {
			FileOutputStream fos = openFileOutput(picFileName + fileNumber + ".jpg", Context.MODE_PRIVATE);
			fos.write(data);
			fos.close();
			File s = getFilesDir();
			Log.d(TAG, "FILENAME: " + picFileName + fileNumber +".jpg");
			Log.d("TAG", s.getPath().toString());
			
			// add new image path to the data base.
			ContentValues cv = new ContentValues(2);
			cv.put(DbHelper.COL_IMG, s.getPath().toString() + "/"+picFileName + fileNumber + ".jpg");
			cv.put(DbHelper.COL_AUD, "NoTag");
			mDb.insert(DbHelper.TABLE_NAME, null, cv);
			//Refresh the list
			mCursor.requery();
			mCursor.moveToLast();
			
												
		} catch (FileNotFoundException e) {
			Log.d(TAG, e.getMessage());
		} catch (IOException e) {
			Log.d(TAG, e.getMessage());
		} 
		tagOrSkip();
	}
	
	private void tagOrSkip() {
		startActivityForResult(new Intent(PhotoTaker.this, TagSkip.class),requestCode);
	}

	/*
	 * get the current file number counter stored in the Shared Preferences
	 */
	private int getCurrentFileNumber() {
		return mPreferences.getInt(FILE_NUMBER, -1);
	}

	public void onShutter() {
		playSoundEffects(R.raw.camera1);
	}
	
	public void takePhoto(){
		mCamera.takePicture(this, null, null, this);
	}
	
	/*
	 * Inner GestureDetector class
	 */
	class MyGestureDetector extends SimpleOnGestureListener {
		
		/*
		 * single tap should play back instructions
		 */
        public boolean onSingleTapConfirmed(MotionEvent e) {
        	Utility.playInstructionsMP(PhotoTaker.this, R.raw.camlonginst, R.raw.camshortinst, mPreferences);
        	return true;
        }
        
        public boolean onSingleTapUp(MotionEvent e) {
           return false;
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
        	float velocityY) {
            return false;
        }
        
        /*
         *  double click takes a picture
         * 
         */
        public boolean onDoubleTap(MotionEvent e) {
            takePhoto();
            return true;
        }

    }
	
	@Override
	public void onBackPressed() {
	   return;
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 	   Log.d("CheckStartActivity","onActivityResult and resultCode = "+resultCode);
 	   super.onActivityResult(requestCode, resultCode, data);
 	   if(resultCode == 1) {
 		   Utility.getMediaPlayer().reset();
 		   Utility.setMediaPlayer(MediaPlayer.create(this, R.raw.skippedtagging));
 		   Utility.getMediaPlayer().start();
 	   }
 	   else {
 		  Utility.getMediaPlayer().reset();
 		  Utility.setMediaPlayer(MediaPlayer.create(this, R.raw.tagsaved));
 		  Utility.getMediaPlayer().start();
 	   }
    }
	
	private void playSoundEffects(int imageId)
	{	
		Utility.getMediaPlayer().reset();
    	Utility.setMediaPlayer(MediaPlayer.create(this, imageId));
    	Utility.getMediaPlayer().start();
    }
}
