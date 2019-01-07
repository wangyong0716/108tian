/*
* Copyright (C) 2014 Vincent Mi
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

package com.ksider.mobile.android.view.rounded;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.util.AttributeSet;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.view.LoadImageView;

@SuppressWarnings("UnusedDeclaration")
public class RoundedImageView extends LoadImageView {
  public static final String TAG = "RoundedImageView";
  private final RectF roundRect = new RectF();
  private float rect_adius = 10;//控制圆角大小
  private final Paint maskPaint = new Paint();
  private final Paint zonePaint = new Paint();


  public RoundedImageView(Context context) {
    this(context, null);
  }

  public RoundedImageView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public RoundedImageView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);

    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RoundedImageView, defStyle, 0);
    rect_adius = a.getDimensionPixelSize(R.styleable.RoundedImageView_corner_radius, 10);
    a.recycle();
    init();
  }
  private void init() {
      maskPaint.setAntiAlias(true);
      maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
      //
      zonePaint.setAntiAlias(true);
      zonePaint.setColor(Color.WHITE);
      //
//      float density = getResources().getDisplayMetrics().density;
//      rect_adius = rect_adius * density;
  }

  public void setRectAdius(float adius) {
      rect_adius = adius;
      invalidate();
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right,
          int bottom) {
      super.onLayout(changed, left, top, right, bottom);
      int w = getWidth();
      int h = getHeight();
      roundRect.set(0, 0, w, h);
  }

  @Override
  public void draw(Canvas canvas) {
      canvas.saveLayer(roundRect, zonePaint, Canvas.ALL_SAVE_FLAG);
      canvas.drawRoundRect(roundRect, rect_adius, rect_adius, zonePaint);
      canvas.saveLayer(roundRect, maskPaint, Canvas.ALL_SAVE_FLAG);
      super.draw(canvas);

      canvas.restore();
  }

}
