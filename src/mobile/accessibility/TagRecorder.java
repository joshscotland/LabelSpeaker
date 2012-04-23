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
 * Records voice tags and associates them with their images
 */

package mobile.accessibility;

import java.io.IOException;
import android.app.Activity;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class TagRecorder extends Activity implements OnClickListener{
	
	private static final String FILE_NUMBER_KEY = "fileNum";
	private static final String TAG = "TAG_RECORDER";
	private SharedPreferences mPreferences;
	private static final String audioFileName = "tm_file";
	public static final String PREF_NAME = "myPreferences";
	private Button button;
	private int currentFileNumber;
	private AudioRecorder recorder;
	private boolean isRecording;
	private static final String VERBOSE_INST_RECORD = "Touch screen to begin recording. " +
			" Touch screen again to stop recording. ";
	
	//DataBase globals
	DbHelper mHelper;
	SQLiteDatabase mDb;
	Cursor mCursor;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tagrecorder);
		
		//Initialize database
		mHelper = new DbHelper(this);
		//Open data base Connections
		mDb = mHelper.getWritableDatabase();
		String[] columns = new String[] {"_id", DbHelper.COL_IMG, DbHelper.COL_AUD};
		mCursor = mDb.query(DbHelper.TABLE_NAME, columns, null, null, null, null, null);
		
		Log.d(TAG, "Created TagRecorder")	;
		initializeUI();
		isRecording = false;
		Utility.getTextToSpeech().say(VERBOSE_INST_RECORD);
	}
	
	private void initializeUI() {
		mPreferences = getSharedPreferences(LabelSpeakerActivity.PREF_NAME, Activity.MODE_WORLD_READABLE);
		button = (Button)findViewById(R.id.StartandStop);
		button.setOnClickListener(this);
		if(isRecording) {
			button.setText(R.string.stopRecording);
		}
		
		// get the current file counter value from Shared  Preferences
		currentFileNumber = getCurrentFileNumber();
	}

	@Override
	public void onClick(View v) {
		if(!isRecording) {
			// set the file name using the file counter and create path to save file 
			String fileName = audioFileName + currentFileNumber;
			String internalStoragePath = getFilesDir().toString();
			
			recorder = new AudioRecorder(fileName, internalStoragePath);
			
			try {
				recorder.start();
				isRecording = true;
				button.setText(R.string.stopRecording);
				Log.d(TAG, "RECORDING");
				
			} catch (Exception e)  {
				Log.d(TAG, e.getMessage().toString());
				e.printStackTrace();
			}
				
		} else {
			try {
				// stop recording, update the file number counter in Shared Preferences
				// and exit activity to return to camera
				recorder.stop();
				mCursor.moveToLast();
				ContentValues cv = new ContentValues(1);
				cv.put(DbHelper.COL_AUD, mCursor.getString(1).replace(".jpg", ".3gp")); 
				mDb.update(DbHelper.TABLE_NAME, cv, "iFile = ?", new String[] {mCursor.getString(1)});
				mCursor.requery();
				mCursor.moveToLast();
				Log.d(TAG, mCursor.getString(0) + ", " + mCursor.getString(1) + ", " + mCursor.getString(2));
				updateCurrentFileNumber(currentFileNumber);
				button.setText(R.string.startRecording);
				isRecording = false;
				finish();
							
			} catch (IOException e) {
				Log.d(TAG, e.getMessage().toString());
				e.printStackTrace();
			}
							
		}
		
	}
	
	/*
	 * Returns current value of file counter from shared preferences
	 */
	private int getCurrentFileNumber() {
		return mPreferences.getInt(FILE_NUMBER_KEY, -1);
	}
	
	/*
	 * Increments the file counter in the shared preferences,
	 * where parameter fileNumber is the current file counter
	 * 
	 */
	private void updateCurrentFileNumber(int fileNumber) {
		SharedPreferences.Editor editor = mPreferences.edit();
		fileNumber = fileNumber + 1;
		editor.putInt(FILE_NUMBER_KEY, fileNumber);
		editor.commit();
	}
	
	//this is called when the screen rotates.
	// (onCreate is no longer called when screen rotates due to manifest, see: android:configChanges)
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
	    super.onConfigurationChanged(newConfig);
	    setContentView(R.layout.tagrecorder);

	    initializeUI();
	}
}
