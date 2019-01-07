package com.ksider.mobile.android.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import com.ksider.mobile.android.WebView.R;

public class Triangle extends View {
	protected int mDrawColor;
	public Triangle(Context context) {
		this(context, null);
	}

	public Triangle(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public Triangle(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.Triangle, defStyleAttr, 0);
		mDrawColor = attributes.getColor(R.styleable.Triangle_draw_color, Color.RED);
		attributes.recycle();
	}
	public void setDrawColor(int color){
		
		mDrawColor = color;
		invalidate();
	}
	@SuppressLint("DrawAllocation")
    @Override  
    protected void onDraw(Canvas canvas){
		super.onDraw(canvas);
		// 创建画笔  
        Paint p = new Paint(); 
        p.setColor(mDrawColor);// 设置红色
        Path path = new Path();  
        path.moveTo(getWidth(), getHeight());
        path.lineTo(getWidth(), 0);  
        path.lineTo(0, 0);
        path.close(); // 使这些点构成封闭的多边形  
        canvas.drawPath(path, p); 
	}
}
