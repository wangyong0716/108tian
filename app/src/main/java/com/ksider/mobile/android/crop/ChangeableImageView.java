package com.ksider.mobile.android.crop;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by yong on 2015/5/8.
 */
public class ChangeableImageView extends ImageView{
    public static final int LEFT = 0;
    public static final int TOP = 1;
    public static final int RIGHT = 2;
    public static final int BOTTOM = 3;
    public static final int WIDTH = 4;
    public static final int HEIGHT = 5;
    private float[] infoArray = new float[6];
    private Matrix matrix = new Matrix();
    private boolean changed = true;

    public ChangeableImageView(Context context) {
        super(context);
        matrix.set(this.getImageMatrix());
        this.setLayerType(LAYER_TYPE_NONE, null);
    }

    public ChangeableImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        matrix.set(this.getImageMatrix());
    }

    public ChangeableImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        matrix.set(this.getImageMatrix());
        this.setLayerType(LAYER_TYPE_NONE, null);
    }

    public float[] getInfoArray() {
        if (changed) {
            getDisplayInfo();
        }
        return infoArray;
    }

    public void setMatrix(Matrix mat) {
        matrix.set(mat);
        this.setImageMatrix(matrix);
        changed = true;
    }

    /**
     * get current information of imageView
     */
    public void getDisplayInfo() {
        float[] values = new float[9];
        matrix.getValues(values);
        infoArray[LEFT] = values[Matrix.MTRANS_X];
        infoArray[TOP] = values[Matrix.MTRANS_Y];
        infoArray[WIDTH] = this.getDrawable().getBounds().width() * values[matrix.MSCALE_X];
        infoArray[HEIGHT] = this.getDrawable().getBounds().height() * values[matrix.MSCALE_Y];
        infoArray[RIGHT] = infoArray[LEFT] + infoArray[WIDTH];
        infoArray[BOTTOM] = infoArray[TOP] + infoArray[HEIGHT];
        changed = false;
    }

    /**
     * just modify matrix, image will be changed later
     *
     * @param scale
     * @param midX
     * @param midY
     */
    public void postScaleMatrix(float scale, float midX, float midY) {
        matrix.postScale(scale, scale, midX, midY);
        changed = true;
    }

    /**
     * just modify matrix, image will be changed later
     *
     * @param x
     * @param y
     */
    public void postTranslateMatrix(float x, float y) {
        matrix.postTranslate(x, y);
        changed = true;
    }

    /**
     * apply the matrix to the image
     */
    public void applayMatrix() {
        this.setImageMatrix(matrix);
    }

    public void postScaleImage(float scale, float midX, float midY) {
        matrix.postScale(scale, scale, midX, midY);
        this.setImageMatrix(matrix);
        changed = true;
    }

    public void postTranslateImage(float x, float y) {
        matrix.postTranslate(x, y);
        this.setImageMatrix(matrix);
        changed = true;
    }

    public Matrix getMatrix() {
        return matrix;
    }

    /**
     * get the coordinate of a point on the image by its coordinate on the screen
     *
     * @param absolutePoint the coordinate relative to parent layout
     * @return the coordinate relative to the image
     */
    public Point getPointByPoint(Point absolutePoint) {
        if (changed) {
            getDisplayInfo();
        }
        int x = (int) ((absolutePoint.x - infoArray[LEFT]) * this.getDrawable().getBounds().width() / infoArray[WIDTH]);
        int y = (int) ((absolutePoint.y - infoArray[TOP]) * this.getDrawable().getBounds().height() / infoArray[HEIGHT]);
        x = x > 0 ? x : 0;
        y = y > 0 ? y : 0;
        x = x < this.getDrawable().getBounds().right ? x : this.getDrawable().getBounds().right;
        y = y < this.getDrawable().getBounds().bottom ? y : this.getDrawable().getBounds().bottom;
        Point relativePoint = new Point(x, y);
        return relativePoint;
    }
}
