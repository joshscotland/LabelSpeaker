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
 * @author Josh Scotland (joshscotland@gmail.com)
 * 
 * Main activity for LabelSpeaker. The app is designed to be an
 * accessible application for labeling items with a barcode.
 * 
 * This application is modeled after PhotoLabeller (UW CSE 481H 2011).
 * 		Abdelelah Salama(engobada@cs.washington.edu)
 * 		Chung Han(han@cs.washington.edu)
 * 		Nikhil Karkarey(nikhilkarkarey@gmail.com)
 */

import mobile.accessibility.DoubleClicker;
import mobile.accessibility.MenuView;
import mobile.accessibility.PhotoBrowse;
import mobile.accessibility.PhotoTaker;
import mobile.accessibility.R;
import mobile.accessibility.SetOptions;
import mobile.accessibility.Utility;
import mobile.accessibility.LabelSpeakerActivity;
import mobile.accessibility.MenuView.Btn;
import mobile.accessibility.MenuView.RowListener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;

public class LabelSpeakerActivity extends Activity {
    private SharedPreferences mPreferences;
	private MenuView menuView;
	private DoubleClicker doubleClicker;
	private static final String FILE_NUMBER = "fileNum";
	public static final String PREF_NAME = "myPreferences";
	private static final String VOICE_INSTR_PREF = "voiceInstructions";

	// Called when the activity is first created
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		//Initialize text to speech
		Utility.setTextToSpeech(getApplicationContext());
		
		menuView = (MenuView) findViewById(R.id.home_view);
		menuView.setFocusable(true);
		menuView.setFocusableInTouchMode(true);
		menuView.setRowListener(new MyRowListener());
		menuView.setButtonNames("Capture", "Browse", "Options");
		
		doubleClicker = new DoubleClicker();
	
		setFileNumbering();
		setInstructionPreferences();
		Utility.setVibrator((Vibrator) getSystemService(Context.VIBRATOR_SERVICE));
		Utility.playInstructionsMP(this, R.raw.hsfullinst, R.raw.hsshortinst, mPreferences);
	}	
    private class MyRowListener implements RowListener {
    	
        public void onRowOver() {
        	Btn focusedButton = menuView.getFocusedButton();
			doubleClicker.click(focusedButton);

			if (focusedButton == Btn.ONE) {
				if (doubleClicker.isDoubleClicked()) {
					launchPhotoTaker();
				} else {
					playTakePhotos();
				}
			} else if (focusedButton == Btn.TWO) {
				if (doubleClicker.isDoubleClicked()) {
					launchPhotoBrowse();
				} else {
					playBrowsePhotos();
				}
			} else if (focusedButton == Btn.THREE) {
				if (doubleClicker.isDoubleClicked()) {
					launchOptions();
				} else {
					playOptions();
				}
			}

		}
        
        public void focusChanged() {
        	doubleClicker.reset();
        }

		public void onTwoFingersUp() {
			exitApp();
		}
	}
    
    public void launchPhotoTaker() {
    	startActivity(new Intent(this, PhotoTaker.class));
    }
    
    public void launchPhotoBrowse() {
    	startActivity(new Intent(this, PhotoBrowse.class));
    }
    
    public void launchOptions() {
    	startActivity(new Intent(this, SetOptions.class));
    }

	/*
	 * Sets the file number counter in the application Shared Preferences for
	 * the first time
	 */
	private void setFileNumbering() {
		// initialize shared preferences
		mPreferences = getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
		if (!mPreferences.contains(FILE_NUMBER)) {
			SharedPreferences.Editor editor = mPreferences.edit();
			editor.putInt(FILE_NUMBER, 0);
			editor.commit();
		}
	}
	
	private void setInstructionPreferences() {
		
		// initialize shared preferences
		mPreferences = getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
		if (!mPreferences.contains(VOICE_INSTR_PREF)) {
			SharedPreferences.Editor editor = mPreferences.edit();
			editor.putInt(VOICE_INSTR_PREF, 0);
			editor.commit();
		}
		
	}
	
	public void onStop(){
		super.onStop();
	}
	
	public void  onPause(){
		super.onPause();
	}
	
	public void onRestart(){
		super.onRestart();
		Utility.playInstructionsMP(this, R.raw.hsfullinst, R.raw.hsshortinst, mPreferences);
		menuView.requestFocus();
		menuView.resetButtonFocus();
	}
	
	public void onResume() {
		super.onResume();
		Utility.playInstructionsMP(this, R.raw.hsfullinst, R.raw.hsshortinst, mPreferences);
		menuView.requestFocus();
		menuView.resetButtonFocus();
	}
	
	@Override
	public void onBackPressed() {
	   return;
	}
	
	public void exitApp() {
		Utility.getMediaPlayer().reset();
		Utility.setMediaPlayer(MediaPlayer.create(this, R.raw.goodbye));
		Utility.getMediaPlayer().start();
		finish();
	}
	
	// Play feedback over take button
	public void playTakePhotos() {
		generateMediaPlayer(MediaPlayer.create(this, R.raw.takephoto));
	}
	
	// Play feedback over browse button
	public void playBrowsePhotos() {
		generateMediaPlayer(MediaPlayer.create(this, R.raw.browsephoto));
	}
	
	// Play feedback over options button
	public void playOptions() {
		generateMediaPlayer(MediaPlayer.create(this, R.raw.options));
	}
	
	public void generateMediaPlayer(MediaPlayer mp) {
		if (Utility.getMediaPlayer() != null) {
			Utility.getMediaPlayer().reset();
		}
		Utility.setMediaPlayer(mp);
		Utility.getMediaPlayer().start();
	}
}