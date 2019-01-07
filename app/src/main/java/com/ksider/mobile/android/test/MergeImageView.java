package com.ksider.mobile.android.test;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.utils.Network;

import java.io.*;

/**
 * Created by yong on 10/13/15.
 */
public class MergeImageView extends ImageView {
    private int defaultMaskId = R.drawable.consult_cancel_icon;
    private String url = "http://pic.108tian.com/pic/m_1881dcfebc024199603821723c4b8c5c.jpg";

    public MergeImageView(Context context) {
        super(context);
//        merge(R.drawable.consult_cancel_icon,R.drawable.consult_confirm_icon);
        Log.v("AAA", "1->startTime=" + System.currentTimeMillis());
//        setImageBitmap(overlay(R.drawable.consult_cancel_icon, R.drawable.consult_confirm_icon));
        setImageResource(url);
        Log.v("AAA", "1->endTime=" + System.currentTimeMillis());
    }

    public MergeImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
//        merge(R.drawable.consult_cancel_icon,R.drawable.consult_confirm_icon);
        Log.v("AAA", "2->startTime=" + System.currentTimeMillis());
//        setImageBitmap(overlay(R.drawable.found_lovers_icon,R.drawable.consult_confirm_icon));
        setImageResource(url);
        Log.v("AAA", "2->endTime=" + System.currentTimeMillis());
    }

    public MergeImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
//        merge(R.drawable.consult_cancel_icon,R.drawable.consult_confirm_icon);
        Log.v("AAA", "3->startTime=" + System.currentTimeMillis());
//        setImageBitmap(overlay(R.drawable.consult_cancel_icon, R.drawable.consult_confirm_icon));
        setImageResource(url);
        Log.v("AAA", "3->endTime=" + System.currentTimeMillis());
    }

    public void merge(int imageId1, int imageId2) {
        Bitmap bottomImage = BitmapFactory.decodeResource(getResources(), imageId1);
        Bitmap topImage = BitmapFactory.decodeResource(getResources(), imageId2);

        // As described by Steve Pomeroy in a previous comment,
        // use the canvas to combine them.
        // Start with the first in the constructor..
        Canvas comboImage = new Canvas(bottomImage);
        // Then draw the second on top of that
        comboImage.drawBitmap(topImage, 0f, 0f, null);

        // bottomImage is now a composite of the two.

        // To write the file out to the SDCard:
        OutputStream os = null;

        try {
            os = new FileOutputStream("/sdcard/DCIM/Camera/" + "myNewFileName.png");
            bottomImage.compress(Bitmap.CompressFormat.PNG, 50, os);

            //Bitmap image.compress(CompressFormat.PNG, 50, os);
        } catch (IOException e) {
            Log.v("error saving", "error saving");
            e.printStackTrace();
        }
    }

    public Bitmap overlay(int imageId1, int imageId2) {
        Bitmap bmp1 = BitmapFactory.decodeResource(getResources(), imageId1);
        Bitmap bmp2 = BitmapFactory.decodeResource(getResources(), imageId2);
        Bitmap bmOverlay = Bitmap.createBitmap(100, 100, bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(bmp2, 0, 0, null);
        return bmOverlay;
    }

    public Bitmap overlay(Bitmap bmp1) {
        Bitmap bmp2 = BitmapFactory.decodeResource(getResources(), defaultMaskId);
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(bmp2, (bmp1.getWidth()-bmp2.getWidth())/2, (bmp1.getHeight()-bmp2.getHeight())/2, null);
        return bmOverlay;
    }

//    public Bitmap overlay(Bitmap bmp2) {
//        Bitmap bmp1 = BitmapFactory.decodeResource(getResources(), defaultMaskId);
//
//        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
//        Canvas canvas = new Canvas(bmOverlay);
//        canvas.drawBitmap(bmp1, new Matrix(), null);
//        canvas.drawBitmap(bmp2, 0, 0, null);
//        return bmOverlay;
//    }

    private Bitmap comp(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        if( baos.toByteArray().length / 1024>1024) {//判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, 50, baos);//这里压缩50%，把压缩后的数据存放到baos中
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        //现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = 300;//这里设置高度为800f
        float ww = 300;//这里设置宽度为480f
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;//设置缩放比例
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        isBm = new ByteArrayInputStream(baos.toByteArray());
        bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        return bitmap;
//        return compressImage(bitmap);//压缩好比例大小后再进行质量压缩
    }

    private Bitmap compressImage(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while ( baos.toByteArray().length / 1024>100) {  //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;//每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }

    public void setImageResource(String url) {
        Log.v("AAA", "url=" + url);
        ImageRequest imageRequest = new ImageRequest(
                url,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        Log.v("AAA","before->time="+System.currentTimeMillis());
                        setImageBitmap(overlay(comp(response)));
                        Log.v("AAA","after->time="+System.currentTimeMillis());
                    }
                }, 0, 0, Bitmap.Config.RGB_565, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError paramVolleyError) {
                paramVolleyError.printStackTrace();
                Log.v("AAA", "error");
            }
        });
        Network.getInstance().addToRequestQueue(imageRequest, "LoadImageView");
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
    }
}
