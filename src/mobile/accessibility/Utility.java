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
 * Set and Get global variables
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Vibrator;

public class Utility extends Application {
	
	private static String imagePath;
	private static String audioPath;
	private static String recEmail;
	private static int rowId = -1;
	private static Vibrator vibrator;
	private static MediaPlayer mp;
	
	private static final String VOICE_INSTR_PREF = "voiceInstructions";
	
	// Text to speech
	private static TtsProviderFactory ttsProviderImpl;

	public static String getImagePath() {
	    return imagePath;
	}

	public static void setImagePath(String var) {
	imagePath = var;
	}
	
	public static String getAudioPath() {
	    return audioPath;
	}

	public static void setAudioPath(String var) {
		audioPath = var;
	}
	
	public static int getRowId() {
	    return rowId;
	}

	public static void setRowId(int var) {
		rowId = var;
	}
	
	public static String getReceiverEmail() {
		return recEmail;
	}
	
	public static void setReceiverEmail(String s) {
		recEmail = s;
	}
	
	public static void setMediaPlayer(MediaPlayer m) {
		mp = m; 
	}
	
	public static MediaPlayer getMediaPlayer() {
		return mp;
	}
	
	public static void setTextToSpeech(Context context) {
		ttsProviderImpl = TtsProviderFactory.getInstance();
		if (ttsProviderImpl != null) {
		    ttsProviderImpl.init(context);
		}
	}
	
	public static TtsProviderFactory getTextToSpeech() {
		return ttsProviderImpl;
	}
	
	public static void setVibrator(Vibrator v) {
		vibrator = v;
	}
	
	public static Vibrator getVibrator() {
		return vibrator;
	}

	public static void playInstructions(String fullVoice, String shortVoice, SharedPreferences pref) {
		int option = pref.getInt(VOICE_INSTR_PREF, 0);
		if (option == 0)
			ttsProviderImpl.say(fullVoice);
		else if (option == 1)
			ttsProviderImpl.say(shortVoice);
		else {
			vibrator.vibrate(150);
		}
	}
	
	// TODO: DELETE ME
	public static void playInstructionsMP(Activity activity,int fullInst, int shortInst, SharedPreferences pref) {
		if(mp != null) {
			mp.reset();
		}
		int option = pref.getInt(VOICE_INSTR_PREF, 0);
		if(option == 0) {
			Utility.setMediaPlayer(MediaPlayer.create(activity, fullInst));
			Utility.getMediaPlayer().start();
		}
		else if(option == 1) {
			Utility.setMediaPlayer(MediaPlayer.create(activity, shortInst));
			Utility.getMediaPlayer().start();
		}
		else {
			vibrator.vibrate(150);
		}
	}
	
	// TODO: Make this the standard option
	public static void playInstructionsMP(Activity activity, String fullInst, String shortInst, SharedPreferences pref) {
		if(mp != null) {
			mp.reset();
		}
		int option = pref.getInt(VOICE_INSTR_PREF, 0);
		if(option == 0) {
			Utility.getTextToSpeech().say(fullInst);
		}
		else if(option == 1) {
			Utility.getTextToSpeech().say(shortInst);
		}
		else {
			vibrator.vibrate(150);
		}
	}
	
	public static void copyFile(File src, File dst) throws IOException
	{
	    FileChannel inChannel = new FileInputStream(src).getChannel();
	    FileChannel outChannel = new FileOutputStream(dst).getChannel();
	    try
	    {
	        inChannel.transferTo(0, inChannel.size(), outChannel);
	    }
	    finally
	    {
	        if (inChannel != null)
	            inChannel.close();
	        if (outChannel != null)
	            outChannel.close();
	    }
	}
	
	
}
