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
 * Uses a canvas to draw a view that is accessible
 */

package mobile.accessibility;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
 
public class MenuView extends View {
    public enum Btn {
        NONE(0),
        ONE(1),
        TWO(2),
        THREE(3);

        private int mValue;

        private Btn(int value) {
            mValue = value;
        }

        public int getValue() {
            return mValue;
        }

        public static Btn fromInt(int i) {
            for (Btn s : values()) {
                if (s.getValue() == i) {
                    return s;
                }
            }
            return NONE;
        }
    }
    
    protected Btn mFocusedButton = Btn.NONE;
    protected Btn mInitialPush = Btn.NONE;
    
    protected int _height;
    protected int _width;
    protected Bitmap _bitmap;
    protected Canvas _canvas;
    protected Paint _paint;
    
    protected int _buttonCount;
    protected String _button1;
    protected String _button2;
    protected String _button3;
    
    protected RowListener mRowListener;
    
    public interface RowListener {
        abstract void onRowOver();
        abstract void focusChanged();
        abstract void onTwoFingersUp();
    }
    
    public MenuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        requestFocus();
        
        _paint = new Paint();
        _paint.setColor(Color.WHITE);
        _paint.setStyle(Paint.Style.STROKE);
        _buttonCount = 3;
        _button1 = "Button1";
        _button2 = "Button2";
        _button3 = "Button3";
    }
     
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        _height = View.MeasureSpec.getSize(heightMeasureSpec);
        _width = View.MeasureSpec.getSize(widthMeasureSpec);
     
        setMeasuredDimension(_width, _height);
     
        _bitmap = Bitmap.createBitmap(_width, _height, Bitmap.Config.ARGB_8888);
        _canvas = new Canvas(_bitmap);
     
        if (_buttonCount == 1)
        	drawOneButton();
        else if (_buttonCount == 2)
        	drawTwoButtons();
        else
        	drawThreeButtons();
    }
    
    private Paint createBorderPaint() {
    	Paint p = new Paint();
        p.setDither(true);
        p.setAntiAlias(true);
        p.setColor(Color.rgb(192, 192, 192));
        p.setStrokeWidth(7);
        return p;
    }
    
    private float getTextHeight() {
		_paint.setStyle(Paint.Style.FILL);
		_paint.setAntiAlias(true);
		_paint.setTextSize(getResources().getDimensionPixelSize(R.dimen.font_size));
		
		Rect rectangle = new Rect();
		_paint.getTextBounds(_button1, 0, _button1.length(), rectangle);
		return rectangle.centerY();
    }
    
    private void drawOneButton() {
        // draw borders
        Paint p = createBorderPaint();
        _canvas.drawRect(0, 7, _width, _height - 7, p);
        p.setStrokeWidth(0);
        
        // draw gradient rectangles
        LinearGradient gradient = new LinearGradient(7, 14, _width - 7, _height - 14, Color.RED, Color.rgb(155, 0, 0), Shader.TileMode.MIRROR);
        p.setShader(gradient);
        _canvas.drawRect(7, 14, _width - 7, _height - 14, p);
    	
        // draw texts
		float textHeight = getTextHeight();
		float startPositionX = (_width) / 2;

		_paint.setTextAlign(Paint.Align.CENTER);
		_canvas.drawText(_button1, startPositionX, (_height / 2) - textHeight / 2, _paint);
		
        invalidate();
    }
    
    private void drawTwoButtons() {
    	// draw borders
        Paint p = createBorderPaint();
        _canvas.drawRect(0, 7, _width, _height / 2 - 7, p);
        _canvas.drawRect(0, _height / 2 + 7, _width, _height - 7, p);
        p.setStrokeWidth(0);
        
        // draw gradient rectangles
        LinearGradient gradient = new LinearGradient(7, 14, _width - 7, _height / 2 - 14, Color.RED, Color.rgb(155, 0, 0), Shader.TileMode.MIRROR);
        p.setShader(gradient);
        _canvas.drawRect(7, 14, _width - 7, _height / 2 - 14, p);
        gradient = new LinearGradient(7, _height / 2 + 14, _width - 7, _height - 14, Color.BLUE, Color.rgb(0, 0, 110), Shader.TileMode.MIRROR);
        p.setShader(gradient);
        _canvas.drawRect(7, _height / 2 + 14, _width - 7, _height - 14, p);
    	
        // draw texts
		float textHeight = getTextHeight();
		float startPositionX = (_width) / 2;

		_paint.setTextAlign(Paint.Align.CENTER);
		_canvas.drawText(_button1, startPositionX, (_height / 4) - textHeight / 2, _paint);
		_canvas.drawText(_button2, startPositionX, _height * 3 / 4 - textHeight / 2, _paint);
		
        invalidate();
    }
    
    private void drawThreeButtons() {
    	// draw borders
        Paint p = createBorderPaint();
        _canvas.drawRect(0, 7, _width, _height / 3 - 7, p);
        _canvas.drawRect(0, _height / 3 + 7, _width, _height * 2 / 3 - 7, p);
        _canvas.drawRect(0, _height * 2 / 3 + 7, _width, _height - 7, p);
        p.setStrokeWidth(0);
        
        // draw gradient rectangles
        LinearGradient gradient = new LinearGradient(7, 14, _width - 7, _height / 3 - 14, Color.RED, Color.rgb(155, 0, 0), Shader.TileMode.MIRROR);
        p.setShader(gradient);
        _canvas.drawRect(7, 14, _width - 7, _height / 3 - 14, p);
        gradient = new LinearGradient(7, _height / 3 + 14, _width - 7, _height * 2 / 3 - 14, Color.BLUE, Color.rgb(0, 0, 110), Shader.TileMode.MIRROR);
        p.setShader(gradient);
        _canvas.drawRect(7, _height / 3 + 14, _width - 7, _height * 2 / 3 - 14, p);
        gradient = new LinearGradient(7, _height * 2 / 3 + 14, _width - 7, _height - 14, Color.MAGENTA, Color.rgb(78, 0, 78), Shader.TileMode.MIRROR);
        p.setShader(gradient);
        _canvas.drawRect(7, _height * 2 / 3 + 14, _width - 7, _height - 14, p);
    	
        // draw texts
		float textHeight = getTextHeight();
		float startPositionX = (_width) / 2;

		_paint.setTextAlign(Paint.Align.CENTER);
		_canvas.drawText(_button1, startPositionX, (_height / 3) / 2 - textHeight, _paint);
		_canvas.drawText(_button2, startPositionX, (_height / 2) - textHeight, _paint);
		_canvas.drawText(_button3, startPositionX, _height - (_height / 3) / 2 - textHeight, _paint);
		
        invalidate();
    }
    
    public void setRowListener(RowListener rowListener) {
        mRowListener = rowListener;
    }
    
    public Btn getFocusedButton() {
        return mFocusedButton;
    }
    
    public void setButtonNames(String s1) {
    	_buttonCount = 1;
    	_button1 = s1;
    }
    
    public void setButtonNames(String s1, String s2) {
    	_buttonCount = 2;
    	_button1 = s1;
    	_button2 = s2;
    }
    
    public void setButtonNames(String s1, String s2, String s3) {
    	_buttonCount = 3;
    	_button1 = s1;
    	_button2 = s2;
    	_button3 = s3;
    }

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawBitmap(_bitmap, 0, 0, _paint);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();

		if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {	
			if (event.getPointerCount() == 2) {
				mRowListener.onTwoFingersUp();
				return true;
			}
			
			int y = (int) event.getY();
			int height = this.getHeight();

			if (_buttonCount == 1) {
				if (mFocusedButton != Btn.ONE)
					focusButton(Btn.ONE);
			} else if (_buttonCount == 2) {
				if ((y < height / 2) && (mFocusedButton != Btn.ONE)) {
					focusButton(Btn.ONE);
				} else if ((y > height / 2) && (mFocusedButton != Btn.TWO)) {
					focusButton(Btn.TWO);
				}
			} else {
				if ((y < height / 3) && (mFocusedButton != Btn.ONE)) {
					focusButton(Btn.ONE);
				} else if ((y > height / 3 && y < height * 2 / 3) && (mFocusedButton != Btn.TWO)) {
					focusButton(Btn.TWO);
				} else if ((y > height * 2 / 3) && (mFocusedButton != Btn.THREE)) {
					focusButton(Btn.THREE);
				}
			}
			
			if (action == MotionEvent.ACTION_DOWN)
				mInitialPush = mFocusedButton;
			
			return true;
		} else if (action == MotionEvent.ACTION_UP) {
			if (mInitialPush != mFocusedButton)
				mRowListener.focusChanged();
			
			mFocusedButton = Btn.NONE;
			mInitialPush = Btn.NONE;			
			return true;
		}

		return false;
	}
	
	private void focusButton(Btn b) {
		mFocusedButton = b;
		mRowListener.onRowOver();
	}
	
	public void resetButtonFocus() {
		mFocusedButton = Btn.NONE;
	}
}
