/**
 * @author Abdelelah Salama(engobada@cs.washington.edu)
 * @author Chung Han(han@cs.washington.edu)
 * @author Nikhil Karkarey(nikhilkarkarey@gmail.com)
 * 
 * Talking Memories - AudioRecorder
 * 
 * Designed to meet the requirements of the Winter 2012 UW course, 
 * CSE 481H: Accessibility Capstone
 * 
 * Initializes, starts, and stops the recorder
 */
package mobile.accessibility;

import java.io.IOException;
import android.media.MediaRecorder;
import android.util.Log;

	public class AudioRecorder {
		private static final String TAG = "AUDIORECORDER";
		final MediaRecorder recorder = new MediaRecorder();
		final String path;
		final String internalStoragePath;
	  	
  // Creates a new audio recording at the given path
  public AudioRecorder(String path, String internalStoragePath) {
	  this.internalStoragePath = internalStoragePath;
	  this.path = sanitizePath(path);
   }

  // Prepare file name with proper audio extension
  private String sanitizePath(String path) {
	  if (!path.startsWith("/")) {
		  path = "/" + path;
	  }
	  if (!path.contains(".")) {
		  path += ".3gp";
	  }
    
	  String audioPath = internalStoragePath + path;
	  Log.d(TAG, audioPath);
	  return audioPath;
  }

  // Start a new recording
  public void start() throws IOException {
	    recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
	    recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
	    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
	    recorder.setOutputFile(path);
	    recorder.prepare();
	    recorder.start();
	    
  }

  // Stop recording
  public void stop() throws IOException {
	  recorder.stop();
	  recorder.release();
  }
}