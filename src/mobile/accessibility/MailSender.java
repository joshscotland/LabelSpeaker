/**
 * @author Abdelelah Salama(engobada@cs.washington.edu)
 * @author Chung Han(han@cs.washington.edu)
 * @author Nikhil Karkarey(nikhilkarkarey@gmail.com)
 * 
 * Talking Memories - HomeScreen
 * 
 * Designed to meet the requirements of the Winter 2012 UW course, 
 * CSE 481H: Accessibility Capstone
 * 
 * Sends an email with an image and its audio tag to the intended recipient
 */

package mobile.accessibility;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.TelephonyManager;

public class MailSender extends Activity implements OnCompletionListener {
	Timer timer;
	MyTextView send;
		
	@Override 
	public void onCreate(Bundle icicle) { 
	  super.onCreate(icicle); 
	  setContentView(R.layout.mailsender);
	  send = (MyTextView) findViewById(R.id.send_mail);
	  new MailSendTask(send).execute();
	  
	}

	/*
	 * This class used to send email with attached image and audio tags
	 * asynchronously
	 * 
	 */
	private class MailSendTask extends AsyncTask<Void, Void, Boolean>{
		private Mail m;
		private MyTextView send;
				
		public MailSendTask(MyTextView send){
			this.m = new Mail("talkingmemories@gmail.com", "talkingmemories2012"); 
			this.send = send;
		}
		
		// runs on UI thread
		@Override
		protected void onPreExecute(){
			Utility.getTextToSpeech().say("Sending email. Please wait.");
			send.setText("Sending email. Please wait");
			send.setClickable(false);
		}
		
		// run on background thread
		@Override
		protected Boolean doInBackground(Void... params) {
			TelephonyManager mgr = (TelephonyManager) getSystemService("phone");
			String phoneNumber = mgr.getLine1Number();
			
			// Play feedback every 5 seconds
			long delay = 5000;
			long period = 5000;
			timer = new Timer();
			timer.scheduleAtFixedRate(
				new TimerTask() {
					public void run() {
						  Utility.getTextToSpeech().say("sending email. Please wait");
			  		}
				}, delay,period);
			  
			  // Talking memories sharing email title
		      String[] toArr = {Utility.getReceiverEmail()}; 
		      m.setTo(toArr); 
		      m.setFrom("talkingmemories@gmail.com"); 
		      m.setSubject("A friend with the phone number: " + phoneNumber + " is sharing a new tagged image with you."); 
		      m.setBody("Attached is a TalkingMemories image file and its associated audio tag." + "\n" +
		    		   "Play the audio tag using QuickTime."); 
		 
		      try { 
		    	  // get path of attachments to send
		    	  File srcImgFile = new File(Utility.getImagePath());
		    	  File srcAudFile = new File(Utility.getAudioPath());
		  		  File destImgFile = new File(Environment.getExternalStorageDirectory(), "tmImage.jpg");
		  		  File destAudFile = new File(Environment.getExternalStorageDirectory(), "tmAudio.3gp");
		  		try {
		  			//copy attachment files from internal storage to external storage
		  			Utility.copyFile(srcImgFile, destImgFile);
		  			Utility.copyFile(srcAudFile, destAudFile);
		  		} catch (IOException e) {
		  			return false;
		  		}
		  		
		  		// add attachments
		  		String fileImgPath = Environment.getExternalStorageDirectory().toString()+ "/tmImage.jpg";
		  		String fileAudPath = Environment.getExternalStorageDirectory().toString()+ "/tmAudio.3gp";
		        m.addAttachment(fileImgPath, "tmImage.jpg");
		        m.addAttachment(fileAudPath, "tmAudio.3gp");
		        
		        // try sending the email
		        if(m.send()) { 
		        	return true;
		        } else { 
		        	return false;
		        } 
		      } catch(Exception e) {
		    	  return false;
		      }
			
		}
		
		// runs on UI thread
		@Override
		protected void onPostExecute(Boolean success){
			timer.cancel();
			if(success){
				Utility.getTextToSpeech().stop();
				playMessage(R.raw.photosharelong);
				send.setText("Photo shared successfully.");
			} else {
				Utility.getTextToSpeech().stop();
				playMessage(R.raw.photofaillong);
				send.setText("Photo share failed.");
			}
		}
	}	
	
	@Override
    public void onBackPressed() {
       return;
    }
	
	private void playMessage(int id)
	{	
    	MediaPlayer m = MediaPlayer.create(this, id);
    	m.setOnCompletionListener(this);
    	m.start();
    }

	@Override
	public void onCompletion(MediaPlayer mp) {
		Intent in = new Intent();
		setResult(3, in);
		finish();
	}
		
		

}
