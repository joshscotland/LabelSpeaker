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
 * Allows users to browse their taken photos and
 * hear back their recorded voice tags
 */

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ViewFlipper;

public class PhotoBrowse extends Activity implements OnClickListener, OnPreparedListener
{
	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	private static final String TAG = "BROWSING";
	private GestureDetector gestureDetector;
	private SharedPreferences mPreferences;
	View.OnTouchListener gestureListener;
	ViewFlipper imageFrame;
	RelativeLayout slideShowBtn;
	Handler handler;
	Runnable runnable;
	ImageView imageView;
	RelativeLayout.LayoutParams params;
	List<String> ImageList;
    List<String> AudioList;
    MediaPlayer mp = new MediaPlayer();
    MediaPlayer m;
    int imageCount;
    int requestCode;
    String s;
    String audioPath;
    AudioManager audiomanager;
    
    
    
    //DATABASE globals
	DbHelper mHelper;
	SQLiteDatabase mDb;
	Cursor mCursor;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.photobrowse);
		
		
		//Initialize database
		mHelper = new DbHelper(this);
		//Open data base Connections
		mDb = mHelper.getWritableDatabase();
		String[] columns = new String[] {"_id", DbHelper.COL_IMG, DbHelper.COL_AUD};
		mCursor = mDb.query(DbHelper.TABLE_NAME, columns, null, null, null, null, null);
		//get the user settings
		mPreferences = getSharedPreferences(LabelSpeakerActivity.PREF_NAME, Activity.MODE_WORLD_READABLE);
		
		audiomanager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		
		imageFrame = (ViewFlipper) findViewById(R.id.imageFrames);
		
		// Get the path of images and voice tags
		File parentFolder = getFilesDir();
		mp.setOnPreparedListener(this);
		addFlipperImages(imageFrame, parentFolder);
		
		// Play instructions
		Utility.getMediaPlayer().reset();
		Utility.playInstructionsMP(this, R.raw.browsefullinstr, R.raw.browseshortinstr, mPreferences);

		// Gesture detection
		gestureDetector = new GestureDetector(new MyGestureDetector());
		gestureListener = new View.OnTouchListener()
		{
			public boolean onTouch(View v, MotionEvent event) {
	
				int action = event.getAction();
				if ((action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) && event.getPointerCount() == 2) {
					finish();
					return true;
				}
				
				if (gestureDetector.onTouchEvent(event))
					return true;
				else
					return false;
			}
		};
		handler = new Handler();
		imageFrame.setOnClickListener(PhotoBrowse.this);
		imageFrame.setOnTouchListener(gestureListener);
	}

	private void addFlipperImages(ViewFlipper flipper, File parent)
	{
		if (isDataBaseEmpty())
		{
			// NO images taken yet, so show a default proper image
			imageView = new ImageView(this);
			params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,RelativeLayout.LayoutParams.FILL_PARENT);
			Bitmap imbm = BitmapFactory.decodeResource(getResources(), R.drawable.noimagefound);
			imageView.setImageBitmap(imbm);
			imageView.setLayoutParams(params);
			flipper.addView(imageView);
		}
		else {
			// Show the most recent picture first
			mCursor.moveToLast();
			s = mCursor.getString(1);
			audioPath = mCursor.getString(2);
			try 
			{
				imageView = new ImageView(this);
				params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,RelativeLayout.LayoutParams.FILL_PARENT);
				FileInputStream imageStream = new FileInputStream(s);
				BitmapFactory.Options o = new BitmapFactory.Options();
				o.inPurgeable = true;
				o.inInputShareable = true;
				Bitmap imbm = BitmapFactory.decodeFileDescriptor(imageStream.getFD(), null, o);
				imageView.setImageBitmap(imbm);
				imageView.setLayoutParams(params);
				flipper.addView(imageView);
			} 	
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			} 
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	class MyGestureDetector extends SimpleOnGestureListener
	{
		public boolean onSingleTapConfirmed(MotionEvent e)
		{
			// replay instructions
			Utility.getMediaPlayer().reset();
			mp.reset();
			Utility.playInstructionsMP(PhotoBrowse.this, R.raw.browsefullinstr, R.raw.browseshortinstr, mPreferences);
			return true;
		}
		
		// play voice tag
        public boolean onDoubleTap(MotionEvent e) {
			Utility.getMediaPlayer().reset();
        	if (!isDataBaseEmpty()) {
				mp.reset();
				playTag(audioPath);
				handler.removeCallbacks(runnable);
				return true;
			}
			else {
				Utility.getMediaPlayer().reset();
				Utility.setMediaPlayer(MediaPlayer.create(PhotoBrowse.this,R.raw.noimagesfound));
				Utility.getMediaPlayer().start();
				return true;
			}
        }
        
        // Open DeleteOrShare activity
        @Override
        public void onLongPress(MotionEvent e) {
        	if (!isDataBaseEmpty()) {
            	Utility.setImagePath(s);
            	Utility.setAudioPath(audioPath);
            	Utility.setRowId(mCursor.getInt(0));
            	startActivityForResult(new Intent(PhotoBrowse.this, DeleteOrShare.class),requestCode);
        	}
        	else {
        		Utility.getMediaPlayer().reset();
        		Utility.setMediaPlayer(MediaPlayer.create(PhotoBrowse.this, R.raw.noimagesfound));
        		Utility.getMediaPlayer().start();
        	}
       }
        
		public boolean onSingleTapUp(MotionEvent e)
		{			
			handler.removeCallbacks(runnable);
			return true;
		}
	
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,float velocityY)
		{
			try 
			{
				if (e1.getY() - e2.getY() > SWIPE_MAX_OFF_PATH&& Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
					Log.d("slideShow", "slideShowAccepted");
					if(isDataBaseEmpty()) {
						Utility.getMediaPlayer().reset();
						Utility.setMediaPlayer(MediaPlayer.create(PhotoBrowse.this, R.raw.noimagesfound));
						Utility.getMediaPlayer().start();
						return true;
					}
					// Start a slide show
					mp.reset();
					Utility.getMediaPlayer().reset();
					Utility.setMediaPlayer(MediaPlayer.create(PhotoBrowse.this, R.raw.startslideshow));
					int duration = Utility.getMediaPlayer().getDuration();
					Log.d("slideShow", "duration= " + duration);
					Utility.getMediaPlayer().start();
					runnable = new Runnable()
					{
						public void run()
						{
							Utility.getMediaPlayer().reset();
							s = mCursor.getString(1);
							audioPath = mCursor.getString(2);
							
							mCursor.moveToNext();
							if(mCursor.isAfterLast()) {
								mCursor.moveToFirst();
							}
							
							int wait = getAudioDuration(audioPath);
							handler.postDelayed(runnable, wait);
							try 
							{
								params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,RelativeLayout.LayoutParams.FILL_PARENT);
								FileInputStream imageStream = new FileInputStream(s);
								BitmapFactory.Options o = new BitmapFactory.Options();
								o.inPurgeable = true;
								o.inInputShareable = true;
								Bitmap imbm = BitmapFactory.decodeFileDescriptor(imageStream.getFD(), null, o);
								playSoundEffects(R.raw.imagechange);
								imageView.setImageBitmap(imbm);
								mp.reset();
								playTag(audioPath);
							} 	
							catch (IOException e)
							{
								e.printStackTrace();
							}
						}
					};
					handler.postDelayed(runnable, duration);
					
					return true;
				}
					
				// right to left swipe
				if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY)
				{
					mp.reset();
					handler.removeCallbacks(runnable);
					imageFrame.setInAnimation(inFromRightAnimation());
					imageFrame.setOutAnimation(outToLeftAnimation());
					
					if (!isDataBaseEmpty()) 
					{
						mCursor.moveToNext();
						if(mCursor.isAfterLast())
						{
							if(isOnlyOnePicture()) {
								Utility.getMediaPlayer().reset();
								Utility.setMediaPlayer(MediaPlayer.create(PhotoBrowse.this, R.raw.onlyonepic));
								Utility.getMediaPlayer().start();
							}
							else {
								Utility.getMediaPlayer().reset();
								Utility.setMediaPlayer(MediaPlayer.create(PhotoBrowse.this, R.raw.endpic));
								Utility.getMediaPlayer().start();
							}
							mCursor.moveToLast();
						} else
						{
							Utility.getMediaPlayer().reset();
							s = mCursor.getString(1);
							audioPath = mCursor.getString(2);
							try 
							{
								params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,RelativeLayout.LayoutParams.FILL_PARENT);
								FileInputStream imageStream = new FileInputStream(s);
								BitmapFactory.Options o = new BitmapFactory.Options();
								o.inPurgeable = true;
								o.inInputShareable = true;
								Bitmap imbm = BitmapFactory.decodeFileDescriptor(imageStream.getFD(), null, o);
								playSoundEffects(R.raw.imagechange);
								imageView.setImageBitmap(imbm);
							} 	
							catch (FileNotFoundException e)
							{
								e.printStackTrace();
							} 
							catch (IOException e)
							{
								e.printStackTrace();
							}
						}
					}
					else {
						Utility.getMediaPlayer().reset();
						Utility.setMediaPlayer(MediaPlayer.create(PhotoBrowse.this, R.raw.noimagesfound));
						Utility.getMediaPlayer().start();
					}
				}
				else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY)
				{
					mp.reset();
					handler.removeCallbacks(runnable);
					imageFrame.setInAnimation(inFromLeftAnimation());
					imageFrame.setOutAnimation(outToRightAnimation());
					
					if (!isDataBaseEmpty()) 
					{
						mCursor.moveToPrevious();
						if(mCursor.isBeforeFirst()) {
							if(isOnlyOnePicture()) {
								Utility.getMediaPlayer().reset();
								Utility.setMediaPlayer(MediaPlayer.create(PhotoBrowse.this,R.raw.onlyonepic));
								Utility.getMediaPlayer().start();
							}
							else {
								Utility.getMediaPlayer().reset();
								Utility.setMediaPlayer(MediaPlayer.create(PhotoBrowse.this, R.raw.endpic));
								Utility.getMediaPlayer().start();
							}
							mCursor.moveToFirst();
						} else
						{
							Utility.getMediaPlayer().reset();
							s = mCursor.getString(1);
							audioPath = mCursor.getString(2);
							try 
							{
								params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,RelativeLayout.LayoutParams.FILL_PARENT);
								FileInputStream imageStream = new FileInputStream(s);
								BitmapFactory.Options o = new BitmapFactory.Options();
								o.inPurgeable = true;
								o.inInputShareable = true;
								Bitmap imbm = BitmapFactory.decodeFileDescriptor(imageStream.getFD(), null, o);
								playSoundEffects(R.raw.imagechange);
								imageView.setImageBitmap(imbm);
							} 	
							catch (FileNotFoundException e)
							{
								e.printStackTrace();
							} 
							catch (IOException e)
							{
								e.printStackTrace();
							}
						}
					}
					else {
						Utility.getMediaPlayer().reset();
						Utility.setMediaPlayer(MediaPlayer.create(PhotoBrowse.this,R.raw.noimagesfound));
						Utility.getMediaPlayer().start();
					}
				}
			}
			catch (Exception e)
			{
				// nothing
			}
			return false;
		}
	}

	public void onClick(View view) {
	
	}
	
	private Animation inFromRightAnimation()
	{
		Animation inFromRight = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, +1.2f,
											Animation.RELATIVE_TO_PARENT, 0.0f,
											Animation.RELATIVE_TO_PARENT, 0.0f,
											Animation.RELATIVE_TO_PARENT, 0.0f);
		inFromRight.setDuration(500);
		inFromRight.setInterpolator(new AccelerateInterpolator());
		return inFromRight;
	}
	private Animation outToLeftAnimation() 
	{
		Animation outtoLeft = new TranslateAnimation(
											Animation.RELATIVE_TO_PARENT, 0.0f,
											Animation.RELATIVE_TO_PARENT, -1.2f,
											Animation.RELATIVE_TO_PARENT, 0.0f,
											Animation.RELATIVE_TO_PARENT, 0.0f);
		outtoLeft.setDuration(500);
		outtoLeft.setInterpolator(new AccelerateInterpolator());
		return outtoLeft;
	}
	private Animation inFromLeftAnimation()
	{
		Animation inFromLeft = new TranslateAnimation(
											Animation.RELATIVE_TO_PARENT, -1.2f,
											Animation.RELATIVE_TO_PARENT, 0.0f,
											Animation.RELATIVE_TO_PARENT, 0.0f,
											Animation.RELATIVE_TO_PARENT, 0.0f);
		inFromLeft.setDuration(500);
		inFromLeft.setInterpolator(new AccelerateInterpolator());
		return inFromLeft;
	}
	private Animation outToRightAnimation()
	{
		Animation outtoRight = new TranslateAnimation(
											Animation.RELATIVE_TO_PARENT, 0.0f,
											Animation.RELATIVE_TO_PARENT, +1.2f,
											Animation.RELATIVE_TO_PARENT, 0.0f,
											Animation.RELATIVE_TO_PARENT, 0.0f);
		outtoRight.setDuration(500);
		outtoRight.setInterpolator(new AccelerateInterpolator());
		return outtoRight;
	}
	
	// play voice tag
	private void playTag(String s)
	{	
    	try
        {	
    		
    		FileInputStream f = new FileInputStream(s);
        	mp.setDataSource(f.getFD());
    	} 
        catch (IllegalArgumentException e)
        {
    		Log.e(TAG, e.getMessage().toString());
    		e.printStackTrace();
    	} catch (IOException e) {
    		Log.e(TAG, e.getMessage().toString());
    		e.printStackTrace();
    	}
    	try
    	{	
    		Log.d("TAG", "just before prepareAsync");
    		Log.d("TAG", s);
    		mp.prepareAsync();
    	}
    	catch (IllegalStateException e)
    	{
    		Log.e(TAG, e.getMessage().toString());
    	}
            	
	}
	
	private int getAudioDuration(String audioPath) {
		File file = new File(audioPath);
		MediaPlayer temp = new MediaPlayer();
		FileInputStream fs;
		FileDescriptor fd;
		int length = 0;
		try {
			fs = new FileInputStream(file);
			fd = fs.getFD();
			temp.setDataSource(fd);
			temp.prepare(); // might be optional
			length = temp.getDuration();
			temp.release();
		}
		catch(Exception e) {
			
		}
		return length;
	}

	@Override
	public void onPrepared(MediaPlayer arg0) {
		Log.d("TAG", "just before mp.start");
		int volume = audiomanager.getStreamVolume(AudioManager.STREAM_MUSIC);
		Log.d(TAG, String.valueOf(volume));
		mp.setVolume(volume, volume);
		mp.start();
	}

	// play sound effects
	private void playSoundEffects(int imageId)
	{	
		if(m != null) {
			m.reset();
		}
    	m = MediaPlayer.create(this, imageId);
    	m.start();
    }
	
	private boolean isDataBaseEmpty() {
		Cursor cur = mDb.rawQuery("SELECT COUNT(*) FROM " + DbHelper.TABLE_NAME, null);
		if (cur != null) {
			// Always one row returned.
		    cur.moveToFirst();                       
		    if (cur.getInt (0) == 0)
		    {       
		    	 // Zero count means empty table.
		        return true;
	        }
		    return false;
	    }
		return false;
	}
	
	private boolean isOnlyOnePicture() {
		Cursor cur = mDb.rawQuery("SELECT COUNT(*) FROM " + DbHelper.TABLE_NAME, null);
		if (cur != null) {
			// Always one row returned.
		    cur.moveToFirst();                       
		    if (cur.getInt (0) == 1)
		    {            
		    	// Zero count means empty table.
		        return true;
	        }
		    return false;
	    }
		return false;
	}
	
	@Override
	public void onPause(){
		super.onPause();
		mp.reset();
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
 		  //start the confirmation delete activity
 		  startActivityForResult(new Intent(this, DeleteImage.class), requestCode);
 	   }
 	   else if (resultCode == 2) {
 		   // start the keyboard activity
  		  startActivityForResult(new Intent(this, TouchKeyboard.class), requestCode);
 	   }
 	   else if (resultCode == 4) {
 		   // delete picture
 			Utility.getMediaPlayer().reset();
 			Utility.setMediaPlayer(MediaPlayer.create(this, R.raw.deletedphoto));
 			Utility.getMediaPlayer().start();
 		    confirmDelete();
 	   }
 	   else if (resultCode == 5) {
 		   startActivityForResult(new Intent(this, MailSender.class), requestCode);
 	   }
 	   else if (resultCode == 6) {
 		   Utility.getMediaPlayer().reset();
 		   Utility.setMediaPlayer(MediaPlayer.create(this, R.raw.cancelleddeletion));
 		   Utility.getMediaPlayer().start();
 	   }
 	   else {
 		   Utility.getMediaPlayer().reset();
 		   Utility.setMediaPlayer(MediaPlayer.create(this, R.raw.browseshortinstr));
 		   Utility.getMediaPlayer().start();
 	   }
    }
	
	public void confirmDelete() {
		// get the current row position
		int rowPosition = mCursor.getPosition();
		// delete image
		int rowId = Utility.getRowId();
		mDb.delete(DbHelper.TABLE_NAME, "_id = " + rowId, null);
		// load updated database
		mCursor.requery();
		if(isDataBaseEmpty()) {
			params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,RelativeLayout.LayoutParams.FILL_PARENT);
			Bitmap imbm = BitmapFactory.decodeResource(getResources(), R.drawable.noimagefound);
			imageView.setImageBitmap(imbm);
		}
		else {
			// move cursor to the next in line picture
			mCursor.moveToFirst();
			mCursor.move(rowPosition);
			if(mCursor.isAfterLast()) {
				mCursor.moveToLast();
			}
			
			// show next in line picture
			s = mCursor.getString(1);
			audioPath = mCursor.getString(2);
			try 
			{
				params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,RelativeLayout.LayoutParams.FILL_PARENT);
				FileInputStream imageStream = new FileInputStream(s);
				BitmapFactory.Options o = new BitmapFactory.Options();
				o.inPurgeable = true;
				o.inInputShareable = true;
				Bitmap imbm = BitmapFactory.decodeFileDescriptor(imageStream.getFD(), null, o);
				playSoundEffects(R.raw.imagechange);
				imageView.setImageBitmap(imbm);
			} 	
			catch (IOException e)
			{
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

