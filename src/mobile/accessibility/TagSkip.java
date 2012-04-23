/*
 * Copyright (C) 2012 Josh Scotland
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mobile.accessibility;

/*
 * Allows user to either proceed with voice tagging
 * or skip tagging and go back to taking another picture.
 */

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class TagSkip extends Activity implements OnClickListener, OnCompletionListener {
	 View.OnTouchListener gestureListener;
	 private GestureDetector gestureDetector;
	 private SharedPreferences mPreferences;
	 private TextView tv;
	 
	 private static final String FILE_NUMBER_KEY = "fileNum";
	 private int currentFileNumber;
	 private AudioRecorder recorder;
	 private boolean isRecording;
	 private static final String audioFileName = "tm_file";
	 public static final String PREF_NAME = "myPreferences";
	 private static final String TAG = "TAG_RECORDER";

	//DataBase globals
	 DbHelper mHelper;
	 SQLiteDatabase mDb;
	 Cursor mCursor;
	 
	 @Override
	 public void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
		 setContentView(R.layout.tagskip);
		 tv = (TextView)findViewById(R.id.tagskipview);
		 tv.setText("Start Recording");
		  
		//Initialize database
		mHelper = new DbHelper(this);
		//Open data base Connections
		mDb = mHelper.getWritableDatabase();
		String[] columns = new String[] {"_id", DbHelper.COL_IMG, DbHelper.COL_AUD};
		mCursor = mDb.query(DbHelper.TABLE_NAME, columns, null, null, null, null, null);
		
		mPreferences = getSharedPreferences(LabelSpeakerActivity.PREF_NAME, Activity.MODE_WORLD_READABLE);
		currentFileNumber = getCurrentFileNumber();		
		isRecording = false;
		// Gesture detection
		 gestureDetector = new GestureDetector(new MyGestureDetector());
		 gestureListener = new View.OnTouchListener()
		 {
			 public boolean onTouch(View v, MotionEvent event) {
				 int action = event.getAction();
				 if ((action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) && event.getPointerCount() == 2) {
					 // if recording in progress, do nothing when two finger motion occurs
					 if(!isRecording){
						 skipRecording();
						 Intent in = new Intent();
						 setResult(1, in);
						 finish();
					 }
					 return true;
				 }
				 else if (gestureDetector.onTouchEvent(event))
					 return true;
				 else
					 return false;
			 }
		 };
		 tv.setOnClickListener(this);
		 tv.setOnTouchListener(gestureListener);
		 Utility.getMediaPlayer().reset();
		 Utility.playInstructionsMP(this,R.raw.taglonginstr, R.raw.tagshortinstr, mPreferences);
	 }
	 
	 /*
	 * Inner GestureDetector class
	 */
	class MyGestureDetector extends SimpleOnGestureListener {
		
		/*
		 * single tap should play back instructions only if no recording is taking place
		 */
        public boolean onSingleTapConfirmed(MotionEvent e) {
        	if(!isRecording){
        		Utility.playInstructionsMP(TagSkip.this, R.raw.taglonginstr, R.raw.tagshortinstr, mPreferences);
        	}
        	return true;
        }
        
        public boolean onSingleTapUp(MotionEvent e) {
           return false;
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
        	float velocityY) {
            return false;
        }
        
        public boolean onDoubleTap(MotionEvent e) {
        	if(!isRecording) {
        		tv.setText("Stop Recording");
        		tv.setBackgroundColor(getResources().getColor(R.color.red));
        		startRecording();
        	}
        	else {
        		tv.setText("Tag Saved");
        		stopRecording();
        	}
            return true;
        }

	    }
	 
	 @Override
	 public void onBackPressed() {
		 return;
	 }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
	
	private void startRecording() {
		Utility.getMediaPlayer().reset();
		String fileName = audioFileName + currentFileNumber;
		String internalStoragePath = getFilesDir().toString();
		recorder = new AudioRecorder(fileName, internalStoragePath);
								
		MediaPlayer mp = MediaPlayer.create(this, R.raw.recprompt);
		mp.setOnCompletionListener(this);
    	mp.start();
	}

	private void stopRecording() {
		try {
			// stop recording, update the file number counter in Shared Preferences and exit activity to return to camera
			recorder.stop();
			mCursor.moveToLast();
			ContentValues cv = new ContentValues(1);
			cv.put(DbHelper.COL_AUD, mCursor.getString(1).replace(".jpg", ".3gp"));
			mDb.update(DbHelper.TABLE_NAME, cv, "iFile = ?", new String[] { mCursor.getString(1) });
			mCursor.requery();
			mCursor.moveToLast();
			Log.d(TAG, mCursor.getString(0) + ", " + mCursor.getString(1) + ", " + mCursor.getString(2));
			updateCurrentFileNumber(currentFileNumber);
			
			Utility.getMediaPlayer().reset();
			isRecording = false;
			Intent in = new Intent();
			setResult(2, in);
			finish();
		} catch (IOException e) {
			Log.d(TAG, e.getMessage().toString());
			e.printStackTrace();
		}
	}
	
	private void skipRecording(){
		//Utility.getTextToSpeech().stop();
		// set the file name using the file counter and create path to save file
			Utility.getMediaPlayer().reset();
			String fileName = audioFileName + currentFileNumber;
			String internalStoragePath = getFilesDir().toString();
			
			mCursor.moveToLast();
			ContentValues cv = new ContentValues(1);
			cv.put(DbHelper.COL_AUD, mCursor.getString(1).replace(".jpg", ".3gp"));
			mDb.update(DbHelper.TABLE_NAME, cv, "iFile = ?", new String[] { mCursor.getString(1) });
			mCursor.requery();
			mCursor.moveToLast();
			Log.d(TAG, mCursor.getString(0) + ", " + mCursor.getString(1) + ", " + mCursor.getString(2));
			
			try {
				copyResourceToExternal(R.raw.notagrecorded, internalStoragePath+"/"+fileName+".3gp");
				Log.d(TAG, "PATH: " +internalStoragePath+"/"+fileName+".3gp");
			} catch (IOException e) {
				Log.d(TAG, e.getMessage().toString());
				e.printStackTrace();
			}
			
			updateCurrentFileNumber(currentFileNumber);
	}
	
	// This method copies a resource file to the given destination dest
	private void copyResourceToExternal(int resourceID, String dest) throws IOException{
		InputStream ins = getResources().openRawResource(resourceID);
		ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
		int size = 0;
		// Read the entire resource into a local byte buffer.
		byte[] buffer = new byte[1024];
		while((size=ins.read(buffer,0,1024))>=0){
		  outputStream.write(buffer,0,size);
		}
		ins.close();
		buffer=outputStream.toByteArray();
		
		// A copy of your file now exist in buffer, so you can use a FileOutputStream to save the buffer to a new file.
		FileOutputStream fos = new FileOutputStream(dest);
		fos.write(buffer);
		fos.close();		
	}
	
	/*
	 * get the current index for file numbering
	 */
	private int getCurrentFileNumber() {
		return mPreferences.getInt(FILE_NUMBER_KEY, -1);
	}

	/*
	 * increment the index used for file numbering, stored in the Shared Preferences
	 */
	private void updateCurrentFileNumber(int fileNumber) {
		SharedPreferences.Editor editor = mPreferences.edit();
		fileNumber = fileNumber + 1;
		editor.putInt(FILE_NUMBER_KEY, fileNumber);
		editor.commit();
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		if(!isRecording) {
			try {
				recorder.start();
				isRecording = true;
				Log.d(TAG, "RECORDING");
			} catch (Exception e) {
				Log.d(TAG, e.getMessage().toString());
				e.printStackTrace();
			}
		}
	}
	
	protected void onDestroy(){
		//Close the database
		mDb.close();
	    super.onDestroy();
	}
}
