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
 * Initializes the Text to speech
 */

package mobile.accessibility;
import java.util.Locale;

import android.content.Context;
import android.speech.tts.TextToSpeech;

public class TtsProviderImpl extends TtsProviderFactory implements TextToSpeech.OnInitListener {

private TextToSpeech tts;

@Override
public void say(String sayThis) {
    tts.speak(sayThis, TextToSpeech.QUEUE_FLUSH, null);
}

@Override
public void onInit(int status) {
	tts.setLanguage(Locale.US);
}

public void shutdown() {
    tts.shutdown();
}

@Override
public void init(Context context) {
	if (tts == null) {
        tts = new TextToSpeech(context, this);
    }
	
}

@Override
public void stop() {
	tts.stop();
}

@Override
public boolean isSpeaking(){
	return tts.isSpeaking();
}


}
