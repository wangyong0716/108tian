package com.ksider.mobile.android.crop;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.util.AttributeSet;
import android.widget.ImageView;

public class CropImageView extends ImageView {

    Context context;
    private final int crop_width = 5;
    private int screenWidth;
    private int screenHeight;
    private int cropL = 0;
    private int cropT = 0;
    private int cropR = 0;
    private int cropB = 0;
    //////////

    public CropImageView(Context context) {
        super(context);
        this.setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    public CropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    public CropImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    public void setImage(int height, int width, int cropL, int cropT, int cropR, int cropB) {
        screenWidth = width;
        screenHeight = height;
        this.cropL = cropL;
        this.cropT = cropT;
        this.cropR = cropR;
        this.cropB = cropB;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

//        Paint paint = new Paint();
//        paint.setARGB(200, 127, 255, 212);
//        paint.setStrokeWidth(crop_width);
//
//        canvas.drawLine(cropL, cropT, cropR, cropT, paint);
//        canvas.drawLine(cropR, cropT, cropR, cropB, paint);
//        canvas.drawLine(cropR, cropB, cropL, cropB, paint);
//        canvas.drawLine(cropL, cropB, cropL, cropT, paint);
//
//        paint.setARGB(124, 0, 0, 0);
//        canvas.drawRect(0, 0, screenWidth, cropT, paint);
//        canvas.drawRect(cropR, cropT, screenWidth, cropB, paint);
//        canvas.drawRect(0, cropB, screenWidth, screenHeight, paint);
//        canvas.drawRect(0, cropT, cropL, cropB, paint);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setARGB(124, 0, 0, 0);
        Path path = new Path();
        path.addRect(0, 0, screenWidth, screenHeight, Path.Direction.CW);
        Path path1 = new Path();
        path1.addCircle((cropR + cropL) / 2, (cropB + cropT) / 2, (cropR - cropL) / 2, Path.Direction.CW);
//        path.op(path1, Region.Op.DIFFERENCE);
        canvas.clipPath(path1, Region.Op.DIFFERENCE);
        canvas.drawPath(path, paint);
        paint.setARGB(200, 127, 255, 212);
        paint.setStrokeWidth(crop_width);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle((cropR + cropL) / 2, (cropB + cropT) / 2, (cropR - cropL) / 2, paint);
    }
}
