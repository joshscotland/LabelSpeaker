/**
 * @author Abdelelah Salama(engobada@cs.washington.edu)
 * @author Chung Han(han@cs.washington.edu)
 * @author Nikhil Karkarey(nikhilkarkarey@gmail.com)
 * 
 * Talking Memories - DoubleClicker
 * 
 * Designed to meet the requirements of the Winter 2012 UW course, 
 * CSE 481H: Accessibility Capstone
 * 
 * Detects double clicks
 */

package mobile.accessibility;

import java.sql.Timestamp;
import java.util.Stack;

import mobile.accessibility.MenuView.Btn;

public class DoubleClicker {
	
	private static final int DOUBLE_CLICK_DELAY = 1000; // 1 second = 1000
	private Stack<ClickEntry> clickStack;
	private boolean doubleClicked;
	
	protected class ClickEntry {
		protected Btn button;
		protected Timestamp time;
		ClickEntry(Btn button, Timestamp time) {
			this.button = button;
			this.time = time;
		}
	}
	
	public DoubleClicker() {
		doubleClicked = false;
		clickStack = new Stack<ClickEntry>();
		ClickEntry entry = new ClickEntry(Btn.NONE, new Timestamp(System.currentTimeMillis()));
		clickStack.push(entry);
	}
	
	public void click() {
		click(Btn.NONE);
	}
	
	public void click(Btn button) {
		Btn focusedButton = button;
		ClickEntry entry = new ClickEntry(focusedButton, new Timestamp(System.currentTimeMillis()));
		doubleClicked = false;
		if (clickStack.size() == 3) {
			clickStack.remove(0);
		}
		
		clickStack.push(entry);
		
		if (clickStack.size() == 3) {
			ClickEntry entry1 = clickStack.get(1);
			ClickEntry entry2 = clickStack.get(2);
			if (entry1.button == entry2.button)
				doubleClicked = Math.abs((entry1.time.getTime() - entry2.time.getTime())) < DOUBLE_CLICK_DELAY;
		}
	}
	
	public boolean isDoubleClicked() {
		return doubleClicked;		
	}
	
	public void reset() {
    	clickStack.removeAllElements();
    	ClickEntry entry = new ClickEntry(Btn.NONE, new Timestamp(System.currentTimeMillis()));
		clickStack.push(entry);
	}
}
