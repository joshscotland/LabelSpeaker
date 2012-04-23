/**
 * @author Abdelelah Salama(engobada@cs.washington.edu)
 * @author Chung Han(han@cs.washington.edu)
 * @author Nikhil Karkarey(nikhilkarkarey@gmail.com)
 * 
 * Talking Memories - DeleteOrShare
 * 
 * Designed to meet the requirements of the Winter 2012 UW course, 
 * CSE 481H: Accessibility Capstone
 * 
 * Presents user with two buttons:
 * Delete - on double click, takes the user to confirmation screen
 * Share - on double click, takes the user to the keyboard screen
 */

package mobile.accessibility;
import mobile.accessibility.MenuView.Btn;
import mobile.accessibility.MenuView.RowListener;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;

/*
 * Delete or Share Activity which presents user with two buttons:
 * Delete - on double click, takes the user to confirmation screen
 * Share - on double click, takes the user to the keyboard screen
 */
public class DeleteOrShare extends Activity {
	private SharedPreferences mPreferences;
	public static final String PREF_NAME = "myPreferences";
	private MenuView menuView;
	private DoubleClicker doubleClicker;

	// Called when the activity is first created
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.deleteorshare);
        
        // Initialize the view
		menuView = (MenuView) findViewById(R.id.menu_view);
		menuView.setFocusable(true);
		menuView.setFocusableInTouchMode(true);
		menuView.setRowListener(new MyRowListener());
		menuView.setButtonNames("Delete", "Share");
		
		// Initialize the double clicker
		doubleClicker = new DoubleClicker();
        
		// Get the current preferences
		mPreferences = getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
		
		// Play instructions
		Utility.getMediaPlayer().reset();
		Utility.playInstructionsMP(this, R.raw.delsharefullinstr,R.raw.delshareshortinstr, mPreferences);
     }

    private class MyRowListener implements RowListener {
        public void onRowOver() {
        	Btn focusedButton = menuView.getFocusedButton();
			doubleClicker.click(focusedButton);
			if (focusedButton == Btn.ONE) {
				if (doubleClicker.isDoubleClicked()) {
					// Go to the confirmation activity
					launchDeleteImage();
				} else {
					// Play user feedback
					playDeletePhoto();
				}
			} else if (focusedButton == Btn.TWO) {
				if (doubleClicker.isDoubleClicked()) {
					// Go to the keyboard activity
					launchShareImage();
				} else {
					// Play user feedback
					playSharePhoto();
				}
			}
		}
        
		public void focusChanged() {
        	doubleClicker.reset();
        }

		// Go back to browse screen
		public void onTwoFingersUp() {
			Intent in = new Intent();
			setResult(3,in);
			finish();
		}
	}
    
    // Start the delete confirmation activity
    public void launchDeleteImage() {
    	Intent in = new Intent();
    	setResult(1,in);
		finish();
    }
    
    // Start the keyboard activity
    public void launchShareImage() {
    	Intent in = new Intent();
    	setResult(2, in);
    	finish();
	}
    
    @Override
    public void onBackPressed() {
       return;
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    }
    
    // Play feedback over share button
    public void playSharePhoto() {
		Utility.getMediaPlayer().reset();
    	Utility.setMediaPlayer(MediaPlayer.create(this, R.raw.sharephoto));
    	Utility.getMediaPlayer().start();
    }
    
    // Play feedback over delete button
    public void playDeletePhoto() {
		Utility.getMediaPlayer().reset();
    	Utility.setMediaPlayer(MediaPlayer.create(this, R.raw.deletephoto));
    	Utility.getMediaPlayer().start();
    }

}
