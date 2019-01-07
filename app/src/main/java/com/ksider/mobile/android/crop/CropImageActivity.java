/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ksider.mobile.android.crop;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.opengl.GLES10;
import android.text.format.Formatter;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.*;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.FloatMath;
import android.util.Log;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import com.ksider.mobile.android.WebView.Constants;
import com.ksider.mobile.android.WebView.R;

import java.io.*;

/*
 * Modified from original in AOSP.
 */
public class CropImageActivity extends MonitoredActivity {

    private final Handler handler = new Handler();
    private Uri sourceUri;
    private Uri saveUri;

    private static final int SIZE_DEFAULT = 2048;
    private static final int SIZE_LIMIT = 4096;

    private boolean isSaving = false;
    private Bitmap rotateBitmap;
    private boolean hasMeasured = false;
    private RelativeLayout image_relative_layout = null;
    private int frame_width = 0;
    private int frame_height = 0;
    private float bitmapHeight = 0;
    private float bitmapWidth = 0;
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;
    private PointF start = new PointF();
    private PointF previous = new PointF();
    private PointF mid = new PointF();
    private float oldDist = 1f;
    private int moveX = 0;
    private int moveY = 0;
    private CropImageView imageView = null;
    private ChangeableImageView changeableImageView;
    private int cropL = 0;
    private int cropT = 0;
    private int cropR = 0;
    private int cropB = 0;
    private int cropLeft = 0;
    private int cropTop = 0;
    private int cropWidth = 0;
    private int cropHeight = 0;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.crop__activity_crop);
        initViews();

        setupFromIntent();
        if (rotateBitmap == null) {
            finish();
            return;
        }

        bitmapHeight = (float) rotateBitmap.getHeight();
        bitmapWidth = (float) rotateBitmap.getWidth();
        changeableImageView = (ChangeableImageView) findViewById(R.id.show_image);
        changeableImageView.setImageBitmap(rotateBitmap);
        changeableImageView.setOnTouchListener(listener);
        image_relative_layout = (RelativeLayout) findViewById(R.id.image_relative_layout);
        ViewTreeObserver vto = changeableImageView.getViewTreeObserver();

        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                if (hasMeasured == false) {
                    frame_height = image_relative_layout.getMeasuredHeight();
                    frame_width = image_relative_layout.getMeasuredWidth();
                    hasMeasured = true;
                    setCropFrameInfo();
                    changeableImageView.setMatrix(getInitMatrix());
                }
                return true;
            }
        });
    }

    public void setCropFrameInfo() {
        int dis = frame_width * 7 / 20;
        int centerX = frame_width / 2;
        int centerY = frame_height * 2 / 5;

        cropL = centerX - dis;
        cropT = centerY - dis;
        cropR = centerX + dis;
        cropB = centerY + dis;
        imageView.setImage(frame_height, frame_width, cropL, cropT, cropR, cropB);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private View.OnTouchListener listener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    start.set(event.getX(), event.getY());
                    previous.set(start);
                    mode = DRAG;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    oldDist = spacing(event);
                    if (oldDist > 10f) {
                        midPoint(mid, event);
                        mode = ZOOM;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    mode = NONE;
                    Animation scaleAnimation = adjustImageScale(mid.x, mid.y);
                    Animation translateAnimation = adjustImageTranslate();
                    if (scaleAnimation == null && translateAnimation == null) {
                        break;
                    }
                    AnimationSet as = new AnimationSet(true);
                    if (scaleAnimation != null) {
                        as.addAnimation(scaleAnimation);
                    }
                    if (translateAnimation != null) {
                        as.addAnimation(translateAnimation);
                    }
                    changeableImageView.startAnimation(as);
                    changeableImageView.applayMatrix();
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    mode = NONE;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mode == DRAG) {
                        moveX = (int) (event.getX() - previous.x);
                        moveY = (int) (event.getY() - previous.y);
                        previous.set(event.getX(), event.getY());
                        translate(moveX, moveY);
                    } else if (mode == ZOOM) {
                        float newDist = spacing(event);
                        if (newDist > 10f) {
                            float scale = (newDist / oldDist);
                            oldDist = newDist;
                            scale(scale, mid.x, mid.y);
                        }
                    }
                    break;
            }
            return true;
        }
    };

    public Animation adjustImageTranslate() {
        float changeX = 0f;
        float changeY = 0f;
        float[] array = changeableImageView.getInfoArray();
        if (array[ChangeableImageView.LEFT] > cropL) {
            changeX = cropL - array[ChangeableImageView.LEFT];
        } else if (array[ChangeableImageView.RIGHT] < cropR) {
            changeX = cropR - array[ChangeableImageView.RIGHT];
        }
        if (array[ChangeableImageView.TOP] > cropT) {
            changeY = cropT - array[ChangeableImageView.TOP];
        } else if (array[ChangeableImageView.BOTTOM] < cropB) {
            changeY = cropB - array[ChangeableImageView.BOTTOM];
        }
        if (changeX != 0f || changeY != 0f) {
            changeableImageView.postTranslateMatrix(changeX, changeY);
            Animation animation = new TranslateAnimation(-changeX, 0, -changeY, 0);
            animation.setDuration(200);
            return animation;
        }
        return null;
    }

    public Animation adjustImageScale(float midX, float midY) {
        float[] array = changeableImageView.getInfoArray();
        if (array[ChangeableImageView.WIDTH] < (cropR - cropL) || array[ChangeableImageView.HEIGHT] < (cropB - cropT)) {
            float ratioWidth = (cropR - cropL) / array[ChangeableImageView.WIDTH];
            float ratioHeight = (cropB - cropT) / array[ChangeableImageView.HEIGHT];
            float ratio = ratioWidth > ratioHeight ? ratioWidth : ratioHeight;
            changeableImageView.postScaleMatrix(ratio, midX, midY);
            Animation animation = new ScaleAnimation(1 / ratio, 1, 1 / ratio, 1, midX, midY);
            animation.setDuration(200);
            animation.setFillAfter(false);
            return animation;
        }
        return null;
    }

    public void translate(float x, float y) {
        float[] array = changeableImageView.getInfoArray();
        int bufferDis = (cropR - cropL) / 6;
        int leftBound = cropL + bufferDis;
        int topBound = cropT + bufferDis;
        int rightBound = cropR - bufferDis;
        int bottomBound = cropB - bufferDis;

        if (Math.abs(x) > (cropR - cropL) / 4 || Math.abs(y) > (cropB - cropT) / 4) {
            return;
        }
        if (array[ChangeableImageView.LEFT] > leftBound || array[ChangeableImageView.RIGHT] < rightBound ||
                array[ChangeableImageView.TOP] > topBound || array[ChangeableImageView.BOTTOM] < bottomBound) {
            return;
        }
        if (array[ChangeableImageView.LEFT] + x > leftBound) {
            x = leftBound - array[ChangeableImageView.LEFT];
        } else if (array[ChangeableImageView.RIGHT] + x < rightBound) {
            x = rightBound - array[ChangeableImageView.RIGHT];
        }
        if (array[ChangeableImageView.TOP] + y > topBound) {
            y = topBound - array[ChangeableImageView.TOP];
        } else if (array[ChangeableImageView.BOTTOM] + y < bottomBound) {
            y = bottomBound - array[ChangeableImageView.BOTTOM];
        }
        if (x != 0f || y != 0f) {
            changeableImageView.postTranslateImage(x, y);
        }
    }

    private void scale(float scale, float midX, float midY) {
        Matrix mm = new Matrix();
        mm.set(changeableImageView.getImageMatrix());
        mm.postScale(scale, scale, midX, midY);
        float[] values = new float[9];
        mm.getValues(values);
        int ivWidth = (int) (changeableImageView.getDrawable().getBounds().width() * values[Matrix.MSCALE_X]);
        int ivHeight = (int) (changeableImageView.getDrawable().getBounds().height() * values[Matrix.MSCALE_Y]);
        if ((cropR - cropL) * 2 / 3 > ivWidth || (cropB - cropT) * 2 / 3 > ivHeight) {
            return;
        }
        changeableImageView.postScaleImage(scale, midX, midY);
    }

    public void getCropCoordinate() {
        Point leftTopPoint = changeableImageView.getPointByPoint(new Point(cropL, cropT));
        Point rightBottonPoint = changeableImageView.getPointByPoint(new Point(cropR, cropB));

        cropLeft = leftTopPoint.x > 0 ? leftTopPoint.x : 0;
        cropTop = leftTopPoint.y > 0 ? leftTopPoint.y : 0;
        int width = rightBottonPoint.x - leftTopPoint.x;
        int height = rightBottonPoint.y - leftTopPoint.y;
        cropWidth = width > 0 ? width : 0;
        cropHeight = height > 0 ? height : 0;
        if (cropWidth + cropLeft > bitmapWidth) {
            cropWidth = Math.round(bitmapWidth - cropLeft);
        }
        if (cropHeight + cropTop > bitmapHeight) {
            cropHeight = Math.round(bitmapHeight - cropTop);
        }
    }

    /**
     * Determine the space between the first two fingers
     */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * Calculate the mid point of the first two fingers
     */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    private void initViews() {
        imageView = (CropImageView) findViewById(R.id.crop_image);
        imageView.context = this;

        findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        findViewById(R.id.btn_done).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveImage();
            }
        });
    }


    public static int calculateInSampleSize(BitmapFactory.Options options,int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height/ (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return (inSampleSize>1?inSampleSize:1);
    }

    public String getRealPathFromURI(Uri uri){
        String result;
//        Cursor cursor = getContentResolver().query(sourceUri, null, null, null, null);
//        Log.v("AAA","0->uri="+uri);
//        if (cursor == null) {
            result = uri.getPath();
//            Log.v("AAA","1->result="+result);
//        } else {
//            cursor.moveToFirst();
//            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
//            result = cursor.getString(idx);
//            cursor.close();
//            Log.v("AAA","2->result="+result);
//        }

        return result;
    }

    /**
     * get the size of the memory
     * @return
     * format size of memory
     */
    private String getAvailMemory() {

        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        MemoryInfo mi = new MemoryInfo();
        am.getMemoryInfo(mi);

        return Formatter.formatFileSize(getBaseContext(), mi.availMem);
    }

    public Bitmap getBitmap(Bitmap bitmap,int max){
        Bitmap bit = null;
//method 1->
//        if (bitmap.getWidth()<=max|| bitmap.getHeight()<max){
//            return bitmap;
//        }
//        int scaleX = bitmap.getWidth()/max;
//        int scaleY = bitmap.getHeight()/max;
//        int scale = scaleX>scaleY?scaleX:scaleY;
//        Log.v("AAA","width="+bitmap.getWidth()+"||height="+bitmap.getHeight()+"||max="+max+"||scale="+scale);
//        bit = Bitmap.createScaledBitmap(bitmap,bitmap.getWidth()/scale,bitmap.getHeight()/scale,true);
//        method 2->
        double scale = Math.sqrt((double) bitmap.getHeight() * bitmap.getWidth())/max;
        if (scale<=1.0){
            return bitmap;
        }
        bit = Bitmap.createScaledBitmap(bitmap,(int)(bitmap.getWidth()/scale),(int)(bitmap.getHeight()/scale),true);
        return bit;
    }

    private void setupFromIntent() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        if (extras != null) {
            saveUri = extras.getParcelable(MediaStore.EXTRA_OUTPUT);
        }

        sourceUri = intent.getData();
        if (sourceUri != null) {
            InputStream is = null;

            WindowManager wm = this.getWindowManager();

            int width = wm.getDefaultDisplay().getWidth();
            int height = wm.getDefaultDisplay().getHeight();
            int hh = height/2-(int)getResources().getDimension(R.dimen.crop__bar_height);
            try {
                is = getContentResolver().openInputStream(sourceUri);
                final BitmapFactory.Options option = new BitmapFactory.Options();
                option.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(getRealPathFromURI(sourceUri),option);
                option.inSampleSize = calculateInSampleSize(option, 600, 600);
                option.inJustDecodeBounds = false;

                option.inPreferredConfig = Bitmap.Config.RGB_565;
                option.inPurgeable = true;
                option.inInputShareable = true;
                rotateBitmap = BitmapFactory.decodeStream(is, null, option);

                is = getContentResolver().openInputStream(sourceUri);

                rotateBitmap = getBitmap(rotateBitmap, hh);

//                Log.v("AAA","1->inSampleSize="+option.inSampleSize+"||size="+rotateBitmap.getByteCount());
//                double times = (double)rotateBitmap.getByteCount()/500000;
//                if (times>1) {
//                    option.inSampleSize=(int)Math.round(option.inSampleSize * Math.sqrt(times)+0.5);
//                    is = getContentResolver().openInputStream(sourceUri);
//                    rotateBitmap = BitmapFactory.decodeStream(is, null, option);
//                    Log.v("AAA","2->inSampleSize="+option.inSampleSize+"||size="+rotateBitmap.getByteCount());
//                }

//                rotateBitmap = BitmapFactory.decodeStream(is, null, option);

//                Bitmap bm = BitmapFactory.decodeStream(is, null, option);
//                Log.v("AAA","size="+bm.getByteCount());
//                ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                bm.compress(Bitmap.CompressFormat.JPEG, 30, baos);
//                ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
//                Log.v("AAA","size="+baos.size());
//                rotateBitmap = BitmapFactory.decodeStream(isBm);
//                Log.v("AAA","3->inSampleSize="+option.inSampleSize+"||size="+rotateBitmap.getByteCount());
            } catch (IOException e) {
                Log.e(Constants.LOG_TAG, e.toString());
                setResultException(e);
            } catch (OutOfMemoryError e) {
                Log.e(Constants.LOG_TAG, e.toString());
                setResultException(e);
            } finally {
                CropUtil.closeSilently(is);
            }
        }
    }

    private int calculateBitmapSampleSize(Uri bitmapUri) throws IOException {
        InputStream is = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try {
            is = getContentResolver().openInputStream(bitmapUri);
            BitmapFactory.decodeStream(is, null, options); // Just get image size
        } finally {
            CropUtil.closeSilently(is);
        }

        int maxSize = getMaxImageSize();
        int sampleSize = 1;
        while (options.outHeight / sampleSize > maxSize || options.outWidth / sampleSize > maxSize) {
            sampleSize = sampleSize << 1;
        }
        return sampleSize;
    }

    private int getMaxImageSize() {
        int textureLimit = getMaxTextureSize();
        if (textureLimit == 0) {
            return SIZE_DEFAULT;
        } else {
            return Math.min(textureLimit, SIZE_LIMIT);
        }
    }

    private int getMaxTextureSize() {
        // The OpenGL texture size is the maximum size that can be drawn in an ImageView
        int[] maxSize = new int[1];
        GLES10.glGetIntegerv(GLES10.GL_MAX_TEXTURE_SIZE, maxSize, 0);
        return maxSize[0];
    }

    private void saveImage() {
        if (isSaving || rotateBitmap == null) {
            return;
        }
        isSaving = true;
        getCropCoordinate();
        Bitmap croppedImage = Bitmap.createBitmap(rotateBitmap, cropLeft, cropTop, cropWidth, cropHeight);
        saveImage(croppedImage);
    }

    /**
     * create a handler to save the bitmap
     * @param croppedImage
     */
    private void saveImage(Bitmap croppedImage) {
        if (croppedImage != null) {
            final Bitmap b = croppedImage;
            CropUtil.startBackgroundJob(this, null, getResources().getString(R.string.crop__saving),
                    new Runnable() {
                        public void run() {
                            saveOutput(b);
                        }
                    }, handler
            );
        } else {
            finish();
        }
    }

    /**
     * save the bitmap
     *
     * @param croppedImage
     */
    private void saveOutput(Bitmap croppedImage) {
        if (saveUri != null) {
            OutputStream outputStream = null;
            try {
                outputStream = getContentResolver().openOutputStream(saveUri);
                if (outputStream != null) {
                    croppedImage.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
                }
            } catch (IOException e) {
                setResultException(e);
                Log.e(Constants.LOG_TAG, e.toString());
            } finally {
                CropUtil.closeSilently(outputStream);
            }

            CropUtil.copyExifRotation(
                    CropUtil.getFromMediaUri(this, getContentResolver(), sourceUri),
                    CropUtil.getFromMediaUri(this, getContentResolver(), saveUri)
            );

            setResultUri(saveUri);
        }

        final Bitmap b = croppedImage;
        handler.post(new Runnable() {
            public void run() {
                b.recycle();
            }
        });
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (rotateBitmap != null) {
            rotateBitmap.recycle();
        }
    }

    @Override
    public boolean onSearchRequested() {
        return false;
    }

    private void setResultUri(Uri uri) {
        setResult(RESULT_OK, new Intent().putExtra(MediaStore.EXTRA_OUTPUT, uri));
    }

    private void setResultException(Throwable throwable) {
        setResult(Crop.RESULT_ERROR, new Intent().putExtra(Crop.Extra.ERROR, throwable));
    }

    public Matrix getInitMatrix() {
        //adjust the image to be displayed in the center of the layout
        Matrix initMatrix = new Matrix();
        int centerX = (changeableImageView.getDrawable().getBounds().right - changeableImageView.getDrawable().getBounds().left) / 2;
        int centerY = (changeableImageView.getDrawable().getBounds().bottom - changeableImageView.getDrawable().getBounds().top) / 2;
        int disX = frame_width / 2 - centerX;
        int disY = frame_height / 2 - centerY;
        initMatrix.postTranslate(disX, disY);
        //adjust the image to be displayed rightly in the layout
        float vertical_ratio = (float) frame_height / changeableImageView.getDrawable().getBounds().height();
        float horizontal_ratio = (float) frame_width / changeableImageView.getDrawable().getBounds().width();
        float ratio = vertical_ratio < horizontal_ratio ? vertical_ratio : horizontal_ratio;
        initMatrix.postScale(ratio, ratio, frame_width / 2, frame_height / 2);
        //adjust the size of the image to adapt to the crop frame
        float[] values = new float[9];
        initMatrix.getValues(values);
        float ivWidth = changeableImageView.getDrawable().getBounds().width() * values[Matrix.MSCALE_X];
        float ivHeight = changeableImageView.getDrawable().getBounds().height() * values[Matrix.MSCALE_Y];
        if (ivWidth < (cropR - cropL) || ivHeight < (cropB - cropT)) {
            float ratioWidth = (cropR - cropL) / ivWidth;
            float ratioHeight = (cropB - cropT) / ivHeight;
            float ratio1 = ratioWidth > ratioHeight ? ratioWidth : ratioHeight;
            initMatrix.postScale(ratio1, ratio1, frame_width / 2, frame_height / 2);
        }
        //adjust the coordinate of the image to adapt to the crop frame
        initMatrix.getValues(values);
        float ivLeft = values[Matrix.MTRANS_X];
        float ivTop = (int) values[Matrix.MTRANS_Y];
        float ivRight = ivLeft + (int) (changeableImageView.getDrawable().getBounds().width() * values[Matrix.MSCALE_X]);
        float ivBottom = ivTop + (int) (changeableImageView.getDrawable().getBounds().height() * values[Matrix.MSCALE_Y]);
        float changeX = 0f;
        float changeY = 0f;
        if (ivLeft > cropL) {
            changeX = cropL - ivLeft;
        } else if (ivRight < cropR) {
            changeX = cropR - ivRight;
        }
        if (ivTop > cropT) {
            changeY = cropT - ivTop;
        } else if (ivBottom < cropB) {
            changeY = cropB - ivBottom;
        }
        initMatrix.postTranslate(changeX, changeY);
        return initMatrix;
    }
}
