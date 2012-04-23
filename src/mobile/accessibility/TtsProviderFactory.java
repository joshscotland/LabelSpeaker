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
import android.content.Context;

import android.os.Build;

public abstract class TtsProviderFactory {

public abstract void say(String sayThis);

public abstract void init(Context context);

public abstract void shutdown();

public abstract void stop();

public abstract boolean isSpeaking();



private static TtsProviderFactory sInstance;

public static TtsProviderFactory getInstance() {
    if (sInstance == null) {
        int sdkVersion = Integer.parseInt(Build.VERSION.SDK);
        if (sdkVersion < Build.VERSION_CODES.DONUT) {
            return null;
        }

        try {
            String className = "TtsProviderImpl";
            Class<? extends TtsProviderFactory> clazz =
                    Class.forName(TtsProviderFactory.class.getPackage().getName() + "." + className)
                            .asSubclass(TtsProviderFactory.class);
            sInstance = clazz.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
    return sInstance;
}}