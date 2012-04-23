/**
 * @author Michael Q. Lam(mqtlam@cs.washington.edu)
 * @author Levi Lindsey(levisl@cs.washington.edu)
 * @author Chris Raastad(craastad@cs.washington.edu)
 * 
 * Designed to meet the requirements of the Winter 2011 UW course, 
 * CSE 481H: Accessibility Capstone
 * 
 * TouchKeyboard implements a soft keyboard for text-entry that is accessible 
 * to blind users.  As the user slides a finger over a key, that key's text is 
 * spoken.  When the user lifts her finger from a key, that key is entered.
 * 
 * modified by Chung Han (han@cs.washington.edu)
 */

package mobile.accessibility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TouchKeyboard extends Activity implements OnTouchListener, 
		OnDoubleTapListener, OnGestureListener, OnFocusChangeListener, 
		OnLongClickListener {
	
	private SharedPreferences mPreferences;
	
	// Phonetic spellings for each of the keys; some characters aren't 
	// pronounced correctly if just the single character is given to the 
	// TTS object.
	private static final Map<String, String> PRONUNCIATION_MAP;
	static {
		Map<String, String> aMap = new HashMap<String, String>();
		aMap.put("a", "eh");
		aMap.put("b", "bee");
		aMap.put("c", "see");
		aMap.put("d", "dee");
		aMap.put("e", "ee");
        aMap.put("f", "eff");
        aMap.put("g", "G");
        aMap.put("h", "H");
        aMap.put("i", "I");
        aMap.put("j", "jay");
        aMap.put("k", "kay");
        aMap.put("l", "L");
        aMap.put("m", "M");
        aMap.put("n", "N");
        aMap.put("o", "O");
        aMap.put("p", "pee");
        aMap.put("q", "Q");
        aMap.put("r", "are");
        aMap.put("s", "ess");
        aMap.put("t", "tee");
        aMap.put("u", "you");
        aMap.put("v", "vee");
        aMap.put("w", "double you");
        aMap.put("x", "ex");
        aMap.put("y", "why");
        aMap.put("z", "zee");
        
        aMap.put("A", "capital eh");
        aMap.put("B", "capital bee");
        aMap.put("C", "capital see");
        aMap.put("D", "capital dee");
        aMap.put("E", "capital ee");
        aMap.put("F", "capital eff");
        aMap.put("G", "capital G");
        aMap.put("H", "capital H");
        aMap.put("I", "capital I");
        aMap.put("J", "capital jay");
        aMap.put("K", "capital kay");
        aMap.put("L", "capital L");
        aMap.put("M", "capital M");
        aMap.put("N", "capital N");
        aMap.put("O", "capital O");
        aMap.put("P", "capital pee");
        aMap.put("Q", "capital Q");
        aMap.put("R", "capital are");
        aMap.put("S", "capital ess");
        aMap.put("T", "capital tee");
        aMap.put("U", "capital you");
        aMap.put("V", "capital vee");
        aMap.put("W", "capital double you");
        aMap.put("X", "capital ex");
        aMap.put("Y", "capital why");
        aMap.put("Z", "capital zee");
        
        aMap.put("0", "0");
        aMap.put("1", "1");
        aMap.put("2", "2");
        aMap.put("3", "3");
        aMap.put("4", "4");
        aMap.put("5", "5");
        aMap.put("6", "6");
        aMap.put("7", "7");
        aMap.put("8", "8");
        aMap.put("9", "9");
        
        aMap.put(".", "period");
        aMap.put("@", "at sign");
        aMap.put("#", "number sign");
        aMap.put("$", "dollar sign");
        aMap.put("%", "percent sign");
        aMap.put("&", "amper sand");
        aMap.put("*", "asterisk");
        aMap.put("-", "hyphen");
        aMap.put("+", "plus sign");
        aMap.put("(", "open parenthesis");
        aMap.put(")", "close parenthesis");
        aMap.put("!", "exclamation point");
        aMap.put("\"", "quotation mark");
        aMap.put("'", "apostrophe");
        aMap.put(":", "colon");
        aMap.put(";", "semee colon");
        aMap.put("/", "forward slehsh");
        aMap.put("?", "question mark");
        aMap.put(",", "comma");
        aMap.put("SPACE", "space");
        aMap.put(" ", "space");
        
        aMap.put("ABC", "letters");
        aMap.put("?123", "numbers and punctuation");
        aMap.put("DONE", "done");
        aMap.put("CAPS", null);
        aMap.put("NEXT", "next character");
        aMap.put("PREV", "previous character");
        aMap.put("DEL", "delete current character");
        aMap.put("BKSP", "backspace");
        aMap.put("CHARS", "hear current characters");
        aMap.put("TEXT", "hear current text");
        aMap.put("INSTR", "hear instructions");
		
		PRONUNCIATION_MAP = Collections.unmodifiableMap(aMap);
	}
	
	private static final int[] QWERTY_VIEW_IDS = 
		{R.id.keyboard_prev, R.id.keyboard_next, R.id.keyboard_del, 
		R.id.keyboard_bksp, R.id.keyboard_q, R.id.keyboard_w, 
		R.id.keyboard_e, R.id.keyboard_r, R.id.keyboard_t, 
		R.id.keyboard_y, R.id.keyboard_u, R.id.keyboard_i, 
		R.id.keyboard_o, R.id.keyboard_p, R.id.keyboard_a, 
		R.id.keyboard_s, R.id.keyboard_d, R.id.keyboard_f, 
		R.id.keyboard_g, R.id.keyboard_h, R.id.keyboard_j, 
		R.id.keyboard_k, R.id.keyboard_l, R.id.keyboard_caps, 
		R.id.keyboard_z, R.id.keyboard_x, R.id.keyboard_c, 
		R.id.keyboard_v, R.id.keyboard_b, R.id.keyboard_n, 
		R.id.keyboard_m, R.id.keyboard_per, R.id.keyboard_switch, 
		R.id.keyboard_spa, R.id.keyboard_done, R.id.keyboard_chars, 
		R.id.keyboard_text, R.id.keyboard_instr};
	
	private static final int[] NUM_PUNCT_VIEW_IDS = 
		{R.id.keyboard_prev, R.id.keyboard_next, R.id.keyboard_del, 
		R.id.keyboard_bksp, R.id.keyboard_1, R.id.keyboard_2, 
		R.id.keyboard_3, R.id.keyboard_4, R.id.keyboard_5, 
		R.id.keyboard_6, R.id.keyboard_7, R.id.keyboard_8, 
		R.id.keyboard_9, R.id.keyboard_0, R.id.keyboard_at, 
		R.id.keyboard_num, R.id.keyboard_dol, R.id.keyboard_perc, 
		R.id.keyboard_amp, R.id.keyboard_ast, R.id.keyboard_hyph, 
		R.id.keyboard_plus, R.id.keyboard_open, R.id.keyboard_close, 
		R.id.keyboard_exc, R.id.keyboard_quot, R.id.keyboard_apo, 
		R.id.keyboard_col, R.id.keyboard_semi, R.id.keyboard_for, 
		R.id.keyboard_ques, R.id.keyboard_com, R.id.keyboard_per, 
		R.id.keyboard_switch, R.id.keyboard_spa, R.id.keyboard_done, 
		R.id.keyboard_chars, R.id.keyboard_text, R.id.keyboard_instr};
	
	// Possible keyboard mode values.
	public static final int QWERTY =		0;
	public static final int NUM_PUNCT =		1;
	
	private static final String TAG = "TouchKeyboard";
	
	private GestureDetector mDetector;
	private Resources mResource;
	private LinearLayout mLayout;
	private TextView mTextDisplay;
	private Button mClearFocus;
	
	private Button[] mKeys;
	private int mLastKey = -1;
	private int mKeyCount;
	
	private boolean mCapsOn;
	private List<Character> mEnteredText;
	private int mCursorIndex;
	private String mCurrentString;
	
	// Called when the activity is first created
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(TAG, "+++ ON CREATE  +++");
		super.onCreate(savedInstanceState);
		mPreferences = getSharedPreferences(LabelSpeakerActivity.PREF_NAME, Activity.MODE_WORLD_READABLE);
		Utility.setReceiverEmail("");

		// Setup the entered text structures.
		String startingText = "";
		populateCurrentList(startingText);
		mCurrentString = (startingText != null) ? startingText : "";
		mCurrentString += " ";
		mCursorIndex = 0;
		
		// Get a handle to this application's resources.
		mResource = getResources();
		
		// Setup the starting keyboard mode.
		setupKeyboard(QWERTY);
		
		mDetector = new GestureDetector(this, this);
		
		speakInstructions();
	}
	
	// Store a handle for each key on the given keyboard and the text display
	private void setupKeyboardViews(int[] keyboardIds) {
		mLayout = (LinearLayout) findViewById(R.id.keyboard_layout);
		mTextDisplay = (TextView) findViewById(R.id.text_display);
		mClearFocus = (Button) findViewById(R.id.clear_focus_hack);
		
		mKeyCount = keyboardIds.length;
		mKeys = new Button[mKeyCount];
		
		for(int i = 0; i < keyboardIds.length; i++) {
			mKeys[i] = (Button) findViewById(keyboardIds[i]);
		}
		
		mClearFocus.requestFocus();
		
		drawString();
	}
	
	// Setup the layout and listeners for the given keyboard mode
	private void setupKeyboard(int keyboardMode) {
		switch(keyboardMode) {
		case QWERTY:
			setContentView(R.layout.qwerty_keyboard);
			setupKeyboardViews(QWERTY_VIEW_IDS);
			mCapsOn = false;
			break;
		case NUM_PUNCT:
			setContentView(R.layout.num_punct_keyboard);
			setupKeyboardViews(NUM_PUNCT_VIEW_IDS);
			mCapsOn = true;
			break;
		default:
			Log.e(TAG, "Error: invalid keyboardMode passed to setupKeyboardLayout");
			finish();
			return;
		}
		
		setListeners();
	}
	
	// Set touch, focus and click listeners
	private void setListeners() {
		mLayout.setOnLongClickListener(this);
		mLayout.setOnTouchListener(this);
		mTextDisplay.setOnTouchListener(this);
		mTextDisplay.setOnLongClickListener(this);
		
		for(int i = 0; i < mKeyCount; i++) {
			mKeys[i].setOnTouchListener(this);
			mKeys[i].isFocusableInTouchMode();
			mKeys[i].setOnFocusChangeListener(this);
			mKeys[i].setOnLongClickListener(this);
		}
	}
	
	// Called for touches inside of a key or the text display
	@Override
	public boolean onTouch(View v, MotionEvent me) {
		Log.v(TAG, "onTouch");
		int action = me.getAction();
		if ((action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) && me.getPointerCount() == 2) {
			Intent in = new Intent();
			setResult(3,in);
			Utility.getTextToSpeech().say("");
			finish();
			return true;
		}
		else {
			mDetector.onTouchEvent(me);
			if(me.getAction() == MotionEvent.ACTION_DOWN || 
					me.getAction() == MotionEvent.ACTION_UP || 
					me.getAction() == MotionEvent.ACTION_MOVE) {
				return consumeTouchEvent(me);
			} else {
				return false;
			}
		}
		
		
	}
	
	// Called for touches outside of a key or the text display
	@Override
	public boolean onTouchEvent(MotionEvent me) {
		Log.v(TAG, "onTouchEvent");
		
		if(me.getAction() == MotionEvent.ACTION_DOWN || 
				me.getAction() == MotionEvent.ACTION_UP || 
				me.getAction() == MotionEvent.ACTION_MOVE) {
			return consumeTouchEvent(me);
		} else {
			return super.onTouchEvent(me);
		}
	}
	
	// Consumes the motion event with either focus or click based actions
	private boolean consumeTouchEvent(MotionEvent me) {
		double x = me.getRawX();
		double y = me.getRawY();
		int[] loc = new int[2];
		int[] dim = new int[2];
		boolean eventConsumed = false;
		
		mTextDisplay.getLocationOnScreen(loc);
		dim[0] = mTextDisplay.getWidth();
		dim[1] = mTextDisplay.getHeight();
		// The motion event occurred over the text display.
		if(x <= (loc[0] + dim[0]) && y <= (loc[1] + dim[1])) {/////////////Don't I need to add: && x >= loc[0] && y >= loc[1]  ?!?!?!?!? 
			//////////////////////			user is past the last character, then say so).
			//////////////////////		- if there is more text than fits in the width of the screen, 
			//////////////////////			and the motion event is near either end of the TextView, 
			//////////////////////			then read the current char at the cursor and scroll the 
			//////////////////////			entire text over by one and move the cursor over by one.  
			//////////////////////			This needs to be dependent upon an elapsed time since a 
			//////////////////////			timeStarted variable, and needs to reset that variable 
			//////////////////////			once the scroll has occurred.  Have there be three 
			//////////////////////			different segments that cause this scrolling activity on 
			//////////////////////			either end; have the closest to the end have a faster 
			//////////////////////			elapsed time limit than the others.
			//////////////////////		- if this is an ACTION_UP event (the above two behaviors 
			//////////////////////			only happening with down and move events), then move 
			//////////////////////			the cursor to the location of the event (if past the 
			//////////////////////			end, then move it to the end), and speak the current 
			//////////////////////			location.
			
			eventConsumed = false;
			
		} else {
			for(int i = 0; i < mKeyCount; i++) {
				mKeys[i].getLocationOnScreen(loc);
				dim[0] = mKeys[i].getWidth();
				dim[1] = mKeys[i].getHeight();
				
				// The motion event occurs over a key.
				if(x >= loc[0] && x <= (loc[0] + dim[0]) && y >= loc[1] && y <= (loc[1] + dim[1])) {///////////// Don't I need to add:  ?!?!?!?!?
					// Save which key this is.
					if(i != mLastKey) {
						mLastKey = i;
					}
					
					// Consume the event.
					switch(me.getAction()) {
					case MotionEvent.ACTION_DOWN:
						mKeys[i].requestFocus();
						break;
					case MotionEvent.ACTION_UP:
						keyPressed((String) mKeys[i].getText());
						mClearFocus.requestFocus();
						break;
					case MotionEvent.ACTION_MOVE:
						mKeys[i].requestFocus();
						break;
					default:
						Log.e(TAG, "Error: invalid MotionEvent type passed to findKey "+
								me.getAction());
						finish();
						return false;
					}
					
					eventConsumed = true;
				}
			}
		}
		
		return eventConsumed;
	}
	
	/*
	 * Speak the highlighted letter according to its PRONUNCIATION_MAP value, 
	 * which has phonetic spellings for some letters that aren't pronounced 
	 * correctly if just a single letter is given.
	 */
	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		String keyText = (String)((Button)v).getText();
		if(hasFocus) {
			if(keyText.equals("CAPS")) {
				String capsString = (mCapsOn) ? "off" : "on";
				Utility.getTextToSpeech().say("Turn caps lock " + capsString);
			} else {
				if(!mCapsOn && keyText.length() <= 1) {
					Utility.getTextToSpeech().say(PRONUNCIATION_MAP.get(keyText.toLowerCase()));
				} else {
					Utility.getTextToSpeech().say(PRONUNCIATION_MAP.get(keyText));
				}
			}
		}
	}
	
	// Activate the given key according to its text value
	private void keyPressed(String keyText) {
		if(keyText.equals("ABC")) {
			setupKeyboard(QWERTY);
		} else if(keyText.equals("?123")) {
			setupKeyboard(NUM_PUNCT);
		} else if(keyText.equals("DONE")) {
			returnResult();
		} else if(keyText.equals("CAPS")) {
			mCapsOn = !mCapsOn;
			if(mCapsOn) {
				Utility.getTextToSpeech().say("Caps lock on");
			} else {
				Utility.getTextToSpeech().say("Caps lock off");
			}
		} else if(keyText.equals("NEXT")) {
			moveCursorForward(true);
		} else if(keyText.equals("PREV")) {
			moveCursorBackward(true);
		} else if(keyText.equals("DEL")) {
			deleteChar();
		} else if(keyText.equals("BKSP")) {
			backspace();
		} else if(keyText.equals("CHARS")) {
			speakCurrentChars();
		} else if(keyText.equals("TEXT")) {
			speakCurrentText();
		} else if(keyText.equals("INSTR")) {
			speakInstructions();
		} else if(keyText.equals("SPACE")) {
			addChar(' ');
		} else {	// keyText is some character.
			if(mCapsOn) {		// mCapsOn is set to true in the num_punct layout.
				addChar(keyText.charAt(0));
			} else {
				addChar(keyText.toLowerCase().charAt(0));
			}
		}
	}
	
	/** Speak the currently entered text. */
	@Override
	public boolean onDoubleTap(MotionEvent me) {
		consumeDoubleTapEvent(me);
		return true;
	}
	
	/** Speak the currently entered text. */
	private void consumeDoubleTapEvent(MotionEvent me) {
	}
	
	/** Speak the instructions for the keyboard. */
	@Override
	public boolean onLongClick(View arg0) {
		return true;
	}
	
	/** Disables hard key presses. */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
		if(keyCode == KeyEvent.KEYCODE_MENU 
				|| keyCode == KeyEvent.KEYCODE_BACK 
				|| keyCode == KeyEvent.KEYCODE_SEARCH){
			// Do nothing.
		} else {
			return super.onKeyDown(keyCode, keyEvent);
		}
		return true;
	}
	
	/** Return the entered text to the caller Activity. */
	private void returnResult() {
		Log.d("share", mCurrentString);
		if(isValidEmail(mCurrentString.trim())) {
			Utility.setReceiverEmail(mCurrentString);
			Intent in = new Intent();
			setResult(5, in);
			Utility.getTextToSpeech().say("");
			finish();
		}
		else {
			Utility.getTextToSpeech().say("Invalid email address. Try again.");
			Utility.setReceiverEmail("");
		}
	}
	
	/** Removes from the entered text whichever character is at the cursor's current index. */
	private void deleteChar() {
		String messageString;
		
		if(mCursorIndex < mEnteredText.size()) {
			messageString = "Deleted "+
					PRONUNCIATION_MAP.get(""+mEnteredText.get(mCursorIndex));
			mEnteredText.remove(mCursorIndex);
		} else {
			messageString = "You are at the end of your text.  There is no " +
					"character to delete here.";
		}
		
		Utility.getTextToSpeech().say(messageString);
		speakCurrentChar();
		
		drawString();
	}
	
	/** Removes from the entered text whichever character is before the cursor's current index. */
	private void backspace() {
		String messageString;
		
		if(mCursorIndex > 0) {
			messageString = "Deleted "+
					PRONUNCIATION_MAP.get(""+mEnteredText.get(--mCursorIndex));
			mEnteredText.remove(mCursorIndex);
		} else {
			messageString = "You are at the start of your text.  There is no " +
					"previous character to delete.";
		}
		
		Utility.getTextToSpeech().say(messageString);
		speakCurrentChar();
		
		drawString();
	}
	
	/** Adds the given character to the current cursor's current index in the entered text. */
	private void addChar(char character) {
		mEnteredText.add(mCursorIndex, character);
		moveCursorForward(false);
		
		Utility.getTextToSpeech().say("Entered " + PRONUNCIATION_MAP.get(""+character));
		//speakCurrentChar();
		
		drawString();
	}
	
	/** Decrements the index of the cursor in the entered text. */
	private void moveCursorBackward(boolean speakIt) {
		String messageString;
		
		if(mCursorIndex <= 0) {
			messageString = "Already at start. Unable to move backward.";
		} else {
			messageString = "Moved backward.";
			mCursorIndex--;
		}
		
		// Speak what happened.
		if(speakIt) {
			Utility.getTextToSpeech().say(messageString);
			speakCurrentChar();
		}
		
		drawString();
	}
	
	/** Increments the index of the cursor in the entered text. */
	private void moveCursorForward(boolean speakIt) {
		String messageString;
		
		if(mCursorIndex >= mEnteredText.size()) {
			messageString = "Already at end. Unable to move forward.";
		} else {
			messageString = "Moved forward.";
			mCursorIndex++;
		}
		
		// Speak what happened.
		if(speakIt) {
			Utility.getTextToSpeech().say(messageString);
			speakCurrentChar();
		}
		
		drawString();
	}
	
	/** Draws the currently entered String with the current cursor location. */
	private void drawString() {
		populateCurrentString();
		drawFormattedString();
	}
	
	/** Returns a String representation of the chars in the mEnteredText list. */
	private void populateCurrentString() {
		mCurrentString = "";
		
		for(char character : mEnteredText) {
			mCurrentString += character;
		}
		
		mCurrentString += " ";
	}
	
	/** Populate the mEnteredText list with the characters in the given String. */
	private void populateCurrentList(String startingString) {
		mEnteredText = new ArrayList<Character>();
		
		if(startingString != null) {
			int length = startingString.length();
			
			for(int i = 0; i < length; i++) {
				mEnteredText.add(i, startingString.charAt(i));
			}
		}
	}
	
	/** Changes the text color of the character to which the cursor currently points. */
	private void drawFormattedString() {
		// Force the TextView to use Spannable storage so styles can be
		// attached.
		mTextDisplay.setText(mCurrentString, TextView.BufferType.SPANNABLE);
		// Get the EditText's internal text storage.
		Spannable spannable = new SpannableString(mCurrentString);
		// Highlight the location of the cursor.
		spannable.setSpan(new BackgroundColorSpan(mResource.getColor(R.color.highlight)), mCursorIndex,
				mCursorIndex + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
		mTextDisplay.setText(spannable);
	}
	
	/** Speak the character to which the cursor is currently pointing. */
	private void speakCurrentChar() {
		if(mEnteredText.size() <= 0) {
			Utility.getTextToSpeech().say("There is no entered text.");
		} else {
			// The cursor is at the start of the text.
			if(mCursorIndex <= 0) {
				Utility.getTextToSpeech().say("You are at the start of your text.");
				
			// The cursor is at the end of the text.
			} if(mCursorIndex >= mEnteredText.size()) {
				Utility.getTextToSpeech().say("You are at the end of your text.");
				
			// The cursor is NOT at the end of the text.
			} else {
				Utility.getTextToSpeech().say("Current letter is " + 
						PRONUNCIATION_MAP.get(""+mCurrentString.charAt(mCursorIndex)));
			}
		}
	}
	
	/** Speak the entirety of the currently entered text. */
	private void speakCurrentText() {
		String messageString;
		
		if(mEnteredText.size() > 0) {
			// Should notify the user if the entered text begins with silent whitespace.
			String spaceNotifier = "" + mCurrentString.charAt(0);
			spaceNotifier = (spaceNotifier.equals(" ")) ? "SPACE " : "";
			messageString = "Current text is " + spaceNotifier + mCurrentString;
		} else {
			messageString = "There is no entered text.";
		}
		
		Utility.getTextToSpeech().say(messageString);
	}
	
	/** Speak the entirety of the currently entered text, one character at a time. */
	private void speakCurrentChars() {
		String messageString;
		
		if(mEnteredText.size() > 0) {
			int length = mEnteredText.size();
			messageString = "Current characters are ";
			for(int i = 0; i < length; i++) {
				messageString += PRONUNCIATION_MAP.get(""+mCurrentString.charAt(i))+" ";
			}
		} else {
			messageString = "There are no entered characters.";
		}
		
		Utility.getTextToSpeech().say(messageString);
	}
	
	/** Speak full instructions for the keyboard. */
	private void speakInstructions() {
		String longInst = "Kee board screen. Enter email address of recipient. Use two fingers swipe action to return to photo browse screen." +
		"To use the touchscreen kee board hold phone vertically. " +
				"Pressing down your finger and sliding it over the screen's " +
				"keys will tell you which key you are currently over.  " +
				"Releasing your finger will select and activate whichever " +
				"key you are leaving." +
	"The keys above the kee board will allow you to delete " +
				"and move forward and backward through the text that you " +
				"have already entered.  The keys below the keyboard will " +
				"allow you to hear what you have already entered and to " +
				"hear these instructions again. When you have finished " +
				"typing, press the bottom right key in order to leave the " +
				"keyboard.";
		String shortInst = "Kee board screen. Enter email address of recipient.";
		
		Utility.getMediaPlayer().reset();
		Utility.playInstructions(longInst, shortInst, mPreferences);
	}
	
// ************************************************************************* //
	// Unused methods from the various implemented listener interfaces.
	
	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}
	
	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		return false;
	}
	
	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}
	
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		return false;
	}
	
	@Override
	public void onLongPress(MotionEvent e) {}
	
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}
	
	@Override
	public void onShowPress(MotionEvent e) {}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {
		return false;
	}
	
	public boolean isValidEmail(String s) {
		String  expression="^\\S+@\\S+$";
		String email = s;
	    Pattern pattern = Pattern.compile(expression,Pattern.CASE_INSENSITIVE);
	    Matcher m = pattern.matcher(email);
		return m.matches();
	}
}
