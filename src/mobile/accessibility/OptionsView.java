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
 * Uses a canvas to draw an option view that is accessible
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
import android.view.View;
 
public class OptionsView extends MenuView {

	private int _selectedIndex;
	
    public OptionsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        requestFocus();
        _selectedIndex = 0;
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        _height = View.MeasureSpec.getSize(heightMeasureSpec);
        _width = View.MeasureSpec.getSize(widthMeasureSpec);
     
        setMeasuredDimension(_width, _height);
     
        _bitmap = Bitmap.createBitmap(_width, _height, Bitmap.Config.ARGB_8888);
        _canvas = new Canvas(_bitmap);
     
        if (_buttonCount == 2)
        	drawTwoButtons();
        else
        	drawThreeButtons();
    }
    
    public void setSelectedIndex(int index) {
    	_selectedIndex = index;
    }
    
    protected void drawTwoButtons() {
        Paint p = new Paint();
        p.setDither(true);
        p.setAntiAlias(true);
        
        // draw borders
        p.setColor(Color.rgb(192, 192, 192));
        p.setStrokeWidth(7);
        _canvas.drawRect(0, 7, _width, _height / 2 - 7, p);
        _canvas.drawRect(0, _height / 2 + 7, _width, _height - 7, p);
        p.setStrokeWidth(0);
        
        // draw gradient rectangles
        LinearGradient gradient = new LinearGradient(7, 14, _width - 7, _height / 2 - 14, Color.BLACK, Color.rgb(0, 0, 0), Shader.TileMode.MIRROR);
        p.setShader(gradient);
        _canvas.drawRect(7, 14, _width - 7, _height / 2 - 14, p);
        gradient = new LinearGradient(7, _height / 2 + 14, _width - 7, _height - 14, Color.BLACK, Color.rgb(0, 0, 0), Shader.TileMode.MIRROR);
        p.setShader(gradient);
        _canvas.drawRect(7, _height / 2 + 14, _width - 7, _height - 14, p);
        
        // highlight selected rectangle
        if (_selectedIndex == 0) {
        	gradient = new LinearGradient(7, 14, _width - 7, _height / 2 - 14, Color.rgb(255, 165, 0), Color.rgb(205, 115, 0), Shader.TileMode.MIRROR);
        	p.setShader(gradient);
        	_canvas.drawRect(7, 14, _width - 7, _height / 2 - 14, p);
        } else if (_selectedIndex == 1) {
        	gradient = new LinearGradient(7, _height / 2 + 14, _width - 7, _height - 14, Color.rgb(255, 165, 0), Color.rgb(205, 115, 0), Shader.TileMode.MIRROR);
        	p.setShader(gradient);
        	_canvas.drawRect(7, _height / 2 + 14, _width - 7, _height - 14, p);
        }
    	
        // draw texts
		_paint.setStyle(Paint.Style.FILL);
		_paint.setAntiAlias(true);
		_paint.setTextSize(getResources().getDimensionPixelSize(R.dimen.font_size_small));
		
		Rect rectangle = new Rect();
		_paint.getTextBounds(_button1, 0, _button1.length(), rectangle);
		float textHeight = rectangle.centerY();
		float startPositionX = (_width) / 2;

		_paint.setTextAlign(Paint.Align.CENTER);
		_canvas.drawText(_button1, startPositionX, (_height / 4) - textHeight / 2, _paint);
		_canvas.drawText(_button2, startPositionX, _height * 3 / 4 - textHeight / 2, _paint);
		
        invalidate();
    }
    
    private void drawThreeButtons() {
        Paint p = new Paint();
        p.setDither(true);
        p.setAntiAlias(true);
        
        // draw borders
        p.setColor(Color.rgb(192, 192, 192));
        p.setStrokeWidth(7);
        _canvas.drawRect(0, 7, _width, _height / 3 - 7, p);
        _canvas.drawRect(0, _height / 3 + 7, _width, _height * 2 / 3 - 7, p);
        _canvas.drawRect(0, _height * 2 / 3 + 7, _width, _height - 7, p);
        p.setStrokeWidth(0);

        // draw gradient rectangles
        LinearGradient gradient = new LinearGradient(7, 14, _width - 7, _height / 3 - 14, Color.BLACK, Color.rgb(0, 0, 0), Shader.TileMode.MIRROR);
        p.setShader(gradient);
        _canvas.drawRect(7, 14, _width - 7, _height / 3 - 14, p);
        gradient = new LinearGradient(7, _height / 3 + 14, _width - 7, _height * 2 / 3 - 14, Color.BLACK, Color.rgb(0, 0, 0), Shader.TileMode.MIRROR);
        p.setShader(gradient);
        _canvas.drawRect(7, _height / 3 + 14, _width - 7, _height * 2 / 3 - 14, p);
        gradient = new LinearGradient(7, _height * 2 / 3 + 14, _width - 7, _height - 14, Color.BLACK, Color.rgb(0, 0, 0), Shader.TileMode.MIRROR);
        p.setShader(gradient);
        _canvas.drawRect(7, _height * 2 / 3 + 14, _width - 7, _height - 14, p);

        // highlight selected rectangle
        if (_selectedIndex == 0) {
        	gradient = new LinearGradient(7, 14, _width - 7, _height / 3 - 14, Color.rgb(255, 165, 0), Color.rgb(205, 115, 0), Shader.TileMode.MIRROR);
        	p.setShader(gradient);
        	_canvas.drawRect(7, 14, _width - 7, _height / 3 - 14, p);
        } else if (_selectedIndex == 1) {
        	gradient = new LinearGradient(7, _height / 3 + 14, _width - 7, _height * 2 / 3 - 14, Color.rgb(255, 165, 0), Color.rgb(205, 115, 0), Shader.TileMode.MIRROR);
        	p.setShader(gradient);
        	_canvas.drawRect(7, _height / 3 + 14, _width - 7, _height * 2 / 3 - 14, p);
        } else if (_selectedIndex == 2) {
        	gradient = new LinearGradient(7, _height * 2 / 3 + 14, _width - 7, _height - 14, Color.rgb(255, 165, 0), Color.rgb(205, 115, 0), Shader.TileMode.MIRROR);
        	p.setShader(gradient);
        	_canvas.drawRect(7, _height * 2 / 3 + 14, _width - 7, _height - 14, p);
        }
        
        // draw texts
		_paint.setStyle(Paint.Style.FILL);
		_paint.setAntiAlias(true);
		_paint.setTextSize(getResources().getDimensionPixelSize(R.dimen.font_size_small));
		
		Rect rectangle = new Rect();
		_paint.getTextBounds(_button1, 0, _button1.length(), rectangle);
		float textHeight = rectangle.centerY();
		float startPositionX = (_width) / 2;

		_paint.setTextAlign(Paint.Align.CENTER);
		_canvas.drawText(_button1, startPositionX, (_height / 3) / 2 - textHeight, _paint);
		_canvas.drawText(_button2, startPositionX, (_height / 2) - textHeight, _paint);
		_canvas.drawText(_button3, startPositionX, _height - (_height / 3) / 2 - textHeight, _paint);
		
        invalidate();
    }
    
	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawBitmap(_bitmap, 0, 0, _paint);
	}
	
}
