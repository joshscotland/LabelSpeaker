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
import android.os.Bundle;
import android.os.Vibrator;

public class LabelSpeakerActivity extends Activity {
	private static final String FILE_NUMBER = "fileNum";
	public static final String PREF_NAME = "myPreferences";
	private static final String VOICE_INSTR_PREF = "voiceInstructions";

	private SharedPreferences mPreferences;
	private MenuView menuView;
	private DoubleClicker doubleClicker;

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
		menuView.setButtonNames(getString(R.string.label_button), 
				getString(R.string.scan_button), getString(R.string.options_button));

		doubleClicker = new DoubleClicker();

		setFileNumbering();
		setInstructionPreferences();
		Utility.setVibrator((Vibrator) getSystemService(Context.VIBRATOR_SERVICE));
		Utility.playInstructionsMP(this, getString(R.string.home_screen_instructions), 
				getString(R.string.home_screen), mPreferences);
	}
	
	private class MyRowListener implements RowListener {

		public void onRowOver() {
			Btn focusedButton = menuView.getFocusedButton();
			doubleClicker.click(focusedButton);

			if (focusedButton == Btn.ONE) {
				if (doubleClicker.isDoubleClicked()) {
					Utility.getTextToSpeech().say(getString(R.string.barcode_to_label));
					
					//Scan the barcode
					IntentIntegrator integrator = new IntentIntegrator(LabelSpeakerActivity.this);
					integrator.initiateScan();
				} else {
					Utility.getTextToSpeech().say(getString(R.string.label_button));
				}
			} else if (focusedButton == Btn.TWO) {
				if (doubleClicker.isDoubleClicked()) {
					startActivity(new Intent(LabelSpeakerActivity.this, PhotoBrowse.class));
				} else {
					Utility.getTextToSpeech().say(getString(R.string.scan_button));
				}
			} else if (focusedButton == Btn.THREE) {
				if (doubleClicker.isDoubleClicked()) {
					startActivity(new Intent(LabelSpeakerActivity.this, SetOptions.class));
				} else {
					Utility.getTextToSpeech().say(getString(R.string.options_button));
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
	
	// Handles scanned barcode results
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
		if (scanResult != null && scanResult.getContents() != null) {
			Utility.getTextToSpeech().say(getString(R.string.barcode_to_label_success));
			
			startActivity(new Intent(LabelSpeakerActivity.this, PhotoTaker.class));
		} else {
			// Utility.getTextToSpeech().say(getString(R.string.barcode_to_label_fail));
		}
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

	public void onPause(){
		super.onPause();
	}

	public void onRestart(){
		super.onRestart();
		Utility.playInstructionsMP(this, getString(R.string.home_screen_instructions), 
				getString(R.string.home_screen), mPreferences);
		menuView.requestFocus();
		menuView.resetButtonFocus();
	}

	public void onResume() {
		super.onResume();
		onRestart();
	}

	@Override
	public void onBackPressed() {
		//Disable the back key
		return;
	}

	public void exitApp() {
		Utility.getTextToSpeech().say(getString(R.string.goodbye));
		finish();
	}
}