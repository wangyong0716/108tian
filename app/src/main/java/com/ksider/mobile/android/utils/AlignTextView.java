package com.ksider.mobile.android.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.ksider.mobile.android.WebView.R;

import java.util.ArrayList;

/**
 * Created by yong on 7/23/15.
 * <p/>
 * properties can't be used any more:
 * margin
 */
public class AlignTextView extends TextView {
    private String text = "";
    public int textHeight; //文本的高度
    public int textWidth;//文本的宽度

    private TextPaint mPaint = null;
    private float lineSpace = 0;//行间距
    private float spacing = 0f;
    private int fontHeight = 0;//行高
    private int paddingLeft = 0;
    private int paddingRight = 0;
    private int paddingTop = 0;
    private int paddingBottom = 0;

    private int marginTop;
    private int marginBottom;
    private int marginLeft;
    private int marginRight;

    private int startX = 0;
    private int endX = 0;
    private int startY = 0;
    private int endY = 0;

    private boolean hasContent = false;

    private ArrayList<Line> lines = new ArrayList<Line>();

    public AlignTextView(Context context) {
        super(context);
        Log.v("LLL", "constructor");
    }

    public AlignTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.v("LLL", "constructor");
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AlignTextView);

        float textsize = typedArray.getDimension(R.styleable.AlignTextView_textSize, 14);
        int textcolor = typedArray.getColor(R.styleable.AlignTextView_textColor, -1442840576);
        lineSpace = typedArray.getDimension(R.styleable.AlignTextView_lineSpacingExtra, 15);
        int typeface = typedArray.getInt(R.styleable.AlignTextView_typeface, 0);
        typedArray.recycle();

        paddingTop = (int) typedArray.getDimension(R.styleable.AlignTextView_paddingTop, 0);
        paddingBottom = (int) typedArray.getDimension(R.styleable.AlignTextView_paddingBottom, 0);
        paddingLeft = (int) typedArray.getDimension(R.styleable.AlignTextView_paddingLeft, 0);
        paddingRight = (int) typedArray.getDimension(R.styleable.AlignTextView_paddingRight, 0);

        marginTop = (int) typedArray.getDimension(R.styleable.AlignTextView_marginTop, 0);
        marginBottom = (int) typedArray.getDimension(R.styleable.AlignTextView_marginBottom, 0);
        marginLeft = (int) typedArray.getDimension(R.styleable.AlignTextView_marginLeft, 0);
        marginRight = (int) typedArray.getDimension(R.styleable.AlignTextView_marginRight, 0);

        mPaint = new TextPaint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(textcolor);
        mPaint.setTextSize(textsize);
        spacing = mPaint.getFontSpacing();
        switch (typeface) {
            case 0:
                mPaint.setTypeface(Typeface.DEFAULT);
                break;
            case 1:
                mPaint.setTypeface(Typeface.SANS_SERIF);
                break;
            case 2:
                mPaint.setTypeface(Typeface.SERIF);
                break;
            case 3:
                mPaint.setTypeface(Typeface.MONOSPACE);
                break;
            default:
                mPaint.setTypeface(Typeface.DEFAULT);
                break;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.v("LLL", "onMeasure");
        if (!hasContent) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return ;
        }
        int measuredWidth = measureWidth(widthMeasureSpec);
        textWidth = measuredWidth;
//        try {
//            paddingLeft = getTotalPaddingLeft();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        try {
//            paddingRight = getTotalPaddingRight();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        try {
//            paddingTop = getTotalPaddingTop();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        try {
//            paddingBottom = getTotalPaddingBottom();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        textWidth = textWidth - paddingLeft - paddingRight;
        Log.v("AAA", "measure->width=" + textWidth);
        int measuredHeight;
        int tHeight = measureHeight(heightMeasureSpec);
        measuredHeight = tHeight + paddingTop + paddingBottom;
        this.setMeasuredDimension(measuredWidth, measuredHeight);
        this.setLayoutParams(new LinearLayout.LayoutParams(measuredWidth, measuredHeight));
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private int measureHeight(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        preHandleTextHeight();
        int result = textHeight;
        if (specMode == MeasureSpec.AT_MOST) {
            result = specSize;
        } else if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        }
        return result;
    }

    private int measureWidth(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        int result = 500;
        if (specMode == MeasureSpec.AT_MOST) {
            result = specSize;
        } else if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!hasContent){
            return ;
        }
        Log.v("LLL", "onDraw");
        Log.v("AAA", "onDraw->height=" + textHeight);
        Paint.FontMetrics fm = mPaint.getFontMetrics();
        fontHeight = (int) Math.ceil(fm.descent - fm.top) + (int) lineSpace;//计算字体高度（字体高度＋行间距）
        startX = paddingLeft;
        startY = (int) Math.ceil(fm.descent - fm.top) + paddingTop;

        float scaleX = mPaint.getTextScaleX();
        for (int i = 0; i < lines.size(); i++) {
            Line l = lines.get(i);
            if (l.end) {
                mPaint.setTextScaleX(scaleX);
            } else {
                mPaint.setTextScaleX(textWidth / l.length);
            }
            canvas.drawText(l.content, startX, startY + fontHeight * i, mPaint);
            Log.v("AAA","draw->content="+l.content+"|startX="+startX+"|startY="+(startY + fontHeight * i));
        }
        Log.v("AAA", "onDraw->height=" + textHeight);
    }

    @Override
    protected boolean setFrame(int l, int t, int r, int b) {
        Log.v("LLL", "setFrame");
        return super.setFrame(l, t, r, b);

    }

    public void setContent(String text) {
        Log.v("LLL", "setContent");
        hasContent = true;
        this.text = halfToFull(text);
//        int measuredHeight;
//        preHandleTextHeight();
//        measuredHeight = textHeight + paddingTop + paddingBottom;
//        this.setMeasuredDimension(textWidth + paddingLeft + paddingRight, measuredHeight);
//        this.setLayoutParams(new LinearLayout.LayoutParams(textWidth + paddingLeft + paddingRight, measuredHeight));
    }

    public void preHandleTextHeight() {
        Paint.FontMetrics fm = mPaint.getFontMetrics();
        fontHeight = (int) Math.ceil(fm.descent - fm.top) + (int) lineSpace;
        int line = 0;
        int w = 0;
        lines.clear();
        String element = "";
        Log.v("AAA", "text=" + text + "|textWidth=" + textWidth);
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            float[] widths = new float[1];
            String srt = String.valueOf(ch);
            mPaint.getTextWidths(srt, widths);
            if (ch == '\n') {
                element += ch;
                Line l = new Line();
                l.content = element;
                l.end = true;
                l.length = mPaint.measureText(element);
                lines.add(l);
                //add blank to the head of a new paragraph
//                element = "\t\t";
//                w = (int) mPaint.measureText(element);
                element = "";
                w = 0;
                Log.v("AAA", "1line" + line + "->content:" + l.content);
                line++;
            } else if ((ch == ' ' || ch == '\t') && element.equals("")) {
//                add indent at the beginning of every new paragraph
//                element+="/t";
                continue;
            } else {
                int charWidth = (int) (Math.ceil(widths[0]));
                w += charWidth;
                if (w > textWidth) {
                    Line l = new Line();
                    l.content = element;
                    l.end = false;
                    l.length = w - charWidth;
                    lines.add(l);
                    element = "";
                    line++;
                    Log.v("AAA", "2line" + line + "->content:" + l.content + "|textWidth=" + textWidth + "|w=" + w + "|charWidth=" + charWidth);
                    w = charWidth;
                }
                element += ch;
            }
        }
        if (!element.equals("")) {
            Line l = new Line();
            l.content = element;
            l.end = true;
            l.length = mPaint.measureText(element);
            lines.add(l);
            Log.v("AAA", "line" + line + "->content:" + l.content);
            line++;
        }
        Log.v("AAA", "line=" + line);
        textHeight = (line) * fontHeight + 2;
    }

    public int getDp(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    /**
     * half blank to full
     *
     * @param input
     * @return
     */
    public static String halfToFull(String input) {
        char[] c = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == 32) //半角空格
            {
                c[i] = (char) 12288;
                continue;
            }

            //根据实际情况，过滤不需要转换的符号
            //if (c[i] == 46) //半角点号，不转换
            // continue;
            //数字，大小写英文字母不转换
            if (c[i] >= 48 && c[i] < 58 || c[i] >= 65 && c[i] < 91 || c[i] >= 97 && c[i] < 123) {
                continue;
            }

            if (c[i] > 32 && c[i] < 127)    //其他符号都转换为全角
                c[i] = (char) (c[i] + 65248);
        }
        return new String(c);
    }

    /**
     * full blank to half blank
     *
     * @param input
     * @return
     */
    public static String fullToHalf(String input) {
        char[] c = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == 12288) //全角空格
            {
                c[i] = (char) 32;
                continue;
            }

            if (c[i] > 65280 && c[i] < 65375)
                c[i] = (char) (c[i] - 65248);
        }
        return new String(c);
    }

    class Line {
        float length;
        String content;
        boolean end;
    }
}
