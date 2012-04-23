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
 * Allows user to either proceed with voice tagging
 * or skip tagging and go back to taking another picture.
 */

package mobile.accessibility;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import mobile.accessibility.MenuView.Btn;
import mobile.accessibility.MenuView.RowListener;
import android.app.Activity;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.util.Log;

public class TagOrSkip extends Activity implements OnCompletionListener {

	private static final String FILE_NUMBER_KEY = "fileNum";
	private int currentFileNumber;
	private SharedPreferences mPreferences;
	private AudioRecorder recorder;
	private boolean isRecording;
	private boolean isTaggingskipped;
	private static final String audioFileName = "tm_file";
	public static final String PREF_NAME = "myPreferences";
	private static final String TAG = "TAG_RECORDER";
	MediaPlayer mp;

	private MenuView menuView;
	private DoubleClicker doubleClicker;

	//DataBase globals
	DbHelper mHelper;
	SQLiteDatabase mDb;
	Cursor mCursor;
	Thread waitThread;

	// Called when the activity is first created
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tagorskip);
		Utility.getMediaPlayer().reset();
		menuView = (MenuView) findViewById(R.id.menu_view3);
		menuView.setFocusable(true);
		menuView.setFocusableInTouchMode(true);
		menuView.setRowListener(new MyRowListener());
		menuView.setButtonNames("Tag Photo");

		doubleClicker = new DoubleClicker();

		//Initialize database
		mHelper = new DbHelper(this);
		//Open data base Connections
		mDb = mHelper.getWritableDatabase();
		String[] columns = new String[] {"_id", DbHelper.COL_IMG, DbHelper.COL_AUD};
		mCursor = mDb.query(DbHelper.TABLE_NAME, columns, null, null, null, null, null);

		Log.d(TAG, "Created TagRecorder");

		//mPreferences = getSharedPreferences(HomeScreen.PREF_NAME, Activity.MODE_PRIVATE);
		initializeSettings();
		isRecording = false;
		isTaggingskipped = false;
		Utility.playInstructionsMP(this, R.raw.taglonginstr, R.raw.tagshortinstr,mPreferences);
	}

	private void initializeSettings() {
		mPreferences = getSharedPreferences(LabelSpeakerActivity.PREF_NAME, Activity.MODE_WORLD_READABLE);
		currentFileNumber = getCurrentFileNumber();		
	}

	private class MyRowListener implements RowListener {

		public void onRowOver() {
			Btn focusedButton = menuView.getFocusedButton();
			doubleClicker.click(focusedButton);
			
			if (doubleClicker.isDoubleClicked() && !isRecording) {
				Log.v(TAG, "Double Clicked - Tag");
				startRecording();
			} else if (doubleClicker.isDoubleClicked() && isRecording) {
				Log.v(TAG, "TAG OVER!");
				stopRecording();
			}
			else {
				if(!isRecording) {
					playTagPhoto();
				}
			}
		}

		public void focusChanged() {
			doubleClicker.reset();
		}

		public void onTwoFingersUp() {
			if (!isRecording) {
				skipRecording();
				//playSkipTagging();
			}
		}
	}

	private void startRecording() {
		if (!isRecording) {
			// set the file name using the file counter and create path to save file
			Utility.getMediaPlayer().reset();
			String fileName = audioFileName + currentFileNumber;
			String internalStoragePath = getFilesDir().toString();

			recorder = new AudioRecorder(fileName, internalStoragePath);
									
			mp = MediaPlayer.create(this, R.raw.recprompt);
			mp.setOnCompletionListener(this);
	    	mp.start();
		}
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
			mp = MediaPlayer.create(this, R.raw.tagsaved);
			mp.setOnCompletionListener(this);
	    	mp.start();
	    	
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void skipRecording(){
		isTaggingskipped = true;
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
		
		Utility.getMediaPlayer().reset();
		mp = MediaPlayer.create(this, R.raw.tagskipped);
		mp.setOnCompletionListener(this);
    	mp.start();
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
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	// this is called when the screen rotates.
	// (onCreate is no longer called when screen rotates due to manifest, see: android:configChanges)
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		setContentView(R.layout.tagorskip);
		initializeSettings();
	}

	@Override
	public void onCompletion(MediaPlayer m) {
		
		if(isRecording){
			// exit activity to return to camera view after user informed
			// that tag has been saved
			isRecording = false;
			finish();
		}else if(isTaggingskipped){
			// exit activity to return to camera view after user informed
			// that tagging has been skipped
			isTaggingskipped = false;
			finish();
		} else {
			//start recording after prompt beep played
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
	
	@Override
	public void onBackPressed() {
	   return;
	}
	
	public void playTagPhoto() {
		Utility.getMediaPlayer().reset();
		Utility.setMediaPlayer(MediaPlayer.create(this,R.raw.tagphoto));
		Utility.getMediaPlayer().start();
	}
	
	public void playSkipTagging() {
		Utility.getMediaPlayer().reset();
		Utility.setMediaPlayer(MediaPlayer.create(this,R.raw.skiptagging));
		Utility.getMediaPlayer().start();
	}
	
	public void initializeWaitThread(final int duration) {
		waitThread = new Thread() {
	    int wait = 0;
	    @Override
	    public void run()
	    {
	    	try
	    	{
	    		super.run();
			    /**
			    * use while to get the splash time. Use sleep() to increase
			    * the wait variable for every 100L.
			    */
	    		while (wait < duration) {
	    			sleep(1);
	    			wait += 1;
	    		}
	    	} catch (Exception e) {
	    	} finally
	    	{
	    		finish();
	    	}
    	}
	    };
	}
}
