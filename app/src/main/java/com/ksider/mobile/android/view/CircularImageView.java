package com.ksider.mobile.android.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.utils.ImageUtils;
import com.ksider.mobile.android.utils.Network;

public class CircularImageView extends ImageView {
    private int canvasSize;
    private Bitmap image;
    protected CircularImageView _this;

    private Paint paint;
    //draw a border circle outside of the image, the image only shows inside the border, but the border size is larger, the smaller the image will be
    private Paint borderPaint;
    //draw a cover circle inside the edge of the image, the image shows under and inside the cover
    private Paint coverPaint;
    //draw a mask for the image, the shadow would cover all the image
    private Paint shadowPaint;
    //draw a dashed circle inside the image
    private Paint dashPaint;

    private boolean border;
    private boolean shadow;
    private boolean cover;
    private boolean dashed;
    private int DRAW_CIRCLE = 0;
    private int DRAW_RECT = 1;
    private int graphType;//0->circle,default;1->rectangle

    private int borderWidth;
    private int cornerRadiusX;
    private int cornerRadiusY;
    private int coverWidth;
    private int dashDistance;

    private RectF borderRectF;
    private RectF rectF;
    private RectF coverRectF;
    private RectF dashRectf;

    private Path outPath;
    private Path inPath;

    public CircularImageView(final Context context) {
        this(context, null);
        if (Build.VERSION.SDK_INT <= 18) {
            this.setLayerType(LAYER_TYPE_SOFTWARE, null);
        }
    }

    public CircularImageView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.circularImageViewStyle);
        if (Build.VERSION.SDK_INT <= 18) {
            this.setLayerType(LAYER_TYPE_SOFTWARE, null);
        }
    }

    public CircularImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (Build.VERSION.SDK_INT <= 18) {
            this.setLayerType(LAYER_TYPE_SOFTWARE, null);
        }
        // init paint
        paint = new Paint();
        paint.setAntiAlias(true);

        // load the styled attributes and set their properties
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.CircularImageView, defStyle, 0);

        if (attributes.getBoolean(R.styleable.CircularImageView_border, false)) {
            border = true;
            int defaultBorderSize = (int) (4 * getContext().getResources().getDisplayMetrics().density + 0.5f);
            setBorder(attributes.getDimensionPixelOffset(R.styleable.CircularImageView_border_width, defaultBorderSize), attributes.getColor(R.styleable.CircularImageView_border_color, Color.WHITE));
        } else {
            border = false;
        }

        if (attributes.getBoolean(R.styleable.CircularImageView_shadow, false)) {
            shadow = true;
            addShadow(attributes.getColor(R.styleable.CircularImageView_shadow_color, Color.GRAY));
        } else {
            shadow = false;
        }

        if (attributes.getInt(R.styleable.CircularImageView_graph_type, DRAW_CIRCLE) == DRAW_RECT) {
            this.graphType = DRAW_RECT;
            int defaultCornerRadius = (int) (4 * getContext().getResources().getDisplayMetrics().density + 0.5f);
            setCornerRadius(attributes.getDimensionPixelOffset(R.styleable.CircularImageView_corner_radius_x, defaultCornerRadius), attributes.getDimensionPixelOffset(R.styleable.CircularImageView_corner_radius_y, defaultCornerRadius));
            borderRectF = new RectF();
            rectF = new RectF();
        } else {
            this.graphType = DRAW_CIRCLE;
        }

        if (attributes.getBoolean(R.styleable.CircularImageView_cover, false)) {
            cover = true;
            setCover(attributes.getDimensionPixelOffset(R.styleable.CircularImageView_cover_width, 0), attributes.getColor(R.styleable.CircularImageView_cover_color, Color.TRANSPARENT));
            if (graphType == DRAW_RECT) {
                coverRectF = new RectF();
            }
            outPath = new Path();
            inPath = new Path();
        } else {
            cover = false;
        }

        if (attributes.getBoolean(R.styleable.CircularImageView_dashed, false)) {
            dashed = true;
            setDash(attributes.getDimensionPixelOffset(R.styleable.CircularImageView_dash_distance, 0),
                    attributes.getDimensionPixelOffset(R.styleable.CircularImageView_dash_width, 0),
                    attributes.getFloat(R.styleable.CircularImageView_dash_line_length, 15f),
                    attributes.getFloat(R.styleable.CircularImageView_dash_blank_length, 10f),
                    attributes.getColor(R.styleable.CircularImageView_dash_color, Color.WHITE));
            if (graphType == DRAW_RECT) {
                dashRectf = new RectF();
            }
        } else {
            dashed = false;
        }
    }

    public void setDash(int dashDistance, int dashWidth, float dashLineLength, float dashBlankLength, int dashColor) {
        if (dashPaint == null) {
            dashPaint = new Paint();
            dashPaint.setAntiAlias(true);
        }
        dashPaint.setStrokeWidth(dashWidth);
        dashPaint.setStyle(Paint.Style.STROKE);
        dashPaint.setColor(dashColor);
        this.dashDistance = dashDistance;
        DashPathEffect dashEffects = new DashPathEffect(new float[]{dashLineLength, dashBlankLength}, 1);
        dashPaint.setPathEffect(dashEffects);
    }

    public void setCover(int coverWidth, int coverColor) {
        if (coverPaint == null) {
            coverPaint = new Paint();
            coverPaint.setAntiAlias(true);
        }
        coverPaint.setColor(coverColor);
        this.coverWidth = coverWidth;
        this.requestLayout();
        this.invalidate();
    }

    public void setCornerRadius(int cornerRadiusX, int cornerRadiusY) {
        this.cornerRadiusX = cornerRadiusX;
        this.cornerRadiusY = cornerRadiusY;
        this.requestLayout();
        this.invalidate();
    }

    public void setBorder(int borderWidth, int borderColor) {
        if (borderPaint == null) {
            borderPaint = new Paint();
            borderPaint.setAntiAlias(true);
        }
        borderPaint.setColor(borderColor);
        this.borderWidth = borderWidth;
        this.requestLayout();
        this.invalidate();
    }

    public void addShadow(int colorRes) {
        if (shadowPaint == null) {
            shadowPaint = new Paint();
            shadowPaint.setAntiAlias(true);
        }
        setLayerType(LAYER_TYPE_SOFTWARE, shadowPaint);
        shadowPaint.setColor(colorRes);
        shadowPaint.setShadowLayer(3.0f, 0.0f, 0.0f, colorRes);
        this.invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        // load the bitmap
        image = drawableToBitmap(getDrawable());

        // init shader
        if (image != null) {

            canvasSize = canvas.getWidth();
            if (canvas.getHeight() < canvasSize)
                canvasSize = canvas.getHeight();

            BitmapShader shader = new BitmapShader(Bitmap.createScaledBitmap(image, canvasSize, canvasSize, false), Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            paint.setShader(shader);

            int circleCenter = (canvasSize - (borderWidth * 2)) / 2;
            if (graphType == DRAW_CIRCLE) {
                if (border) {
                    canvas.drawCircle(circleCenter + borderWidth, circleCenter + borderWidth, circleCenter + borderWidth - 4.0f, borderPaint);
                }
                if (shadow) {
                    canvas.drawCircle(circleCenter + borderWidth, circleCenter + borderWidth, circleCenter + borderWidth - 4.0f, shadowPaint);
                }
                canvas.drawCircle(circleCenter + borderWidth, circleCenter + borderWidth, circleCenter - 4.0f, paint);
                if (cover) {
                    outPath.addCircle(circleCenter + borderWidth, circleCenter + borderWidth, circleCenter - 4.0f, Path.Direction.CW);
                    inPath.addCircle(circleCenter + borderWidth, circleCenter + borderWidth, circleCenter - 4.0f - coverWidth, Path.Direction.CW);
                    canvas.clipPath(inPath, Region.Op.DIFFERENCE);
                    canvas.drawPath(outPath, coverPaint);
                }
                if (dashed) {
                    canvas.drawCircle(circleCenter + borderWidth, circleCenter + borderWidth, circleCenter + borderWidth - dashDistance - 4.0f, dashPaint);
                }
            } else if (graphType == DRAW_RECT) {
                borderRectF.set(4f, 4f, canvasSize - 4f, canvasSize - 4f);
                if (border) {
                    canvas.drawRoundRect(borderRectF, cornerRadiusX, cornerRadiusY, borderPaint);
                }
                if (shadow) {
                    canvas.drawRoundRect(borderRectF, cornerRadiusX, cornerRadiusY, shadowPaint);
                }
                rectF.set(4f + borderWidth, 4f + borderWidth, canvasSize - 4f - borderWidth, canvasSize - 4f - borderWidth);
                canvas.drawRoundRect(rectF, cornerRadiusX, cornerRadiusY, paint);
                if (cover) {
                    coverRectF.set(4f + borderWidth + coverWidth, 4f + borderWidth + coverWidth, canvasSize - 4f - borderWidth - coverWidth, canvasSize - 4f - borderWidth - coverWidth);
                    outPath.addRect(rectF, Path.Direction.CW);
                    inPath.addRect(coverRectF, Path.Direction.CW);
                    canvas.clipPath(inPath, Region.Op.DIFFERENCE);
                    canvas.drawPath(outPath, coverPaint);
                }
                if (dashed) {
                    dashRectf.set(4f + borderWidth + dashDistance, 4f + borderWidth + dashDistance, canvasSize - 4f - borderWidth - dashDistance, canvasSize - 4f - borderWidth - dashDistance);
                    canvas.drawRoundRect(dashRectf, cornerRadiusX, cornerRadiusY, dashPaint);
                }
            }
        }
    }

    public void setImageResource(String url) {
        if (url == null) return;
        if (!url.equals(this.getTag())) {
            setImageDrawable(null);
        }

        setTag(url);
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            if (url.startsWith("u_")) {
                url = url.substring(2);
            }
            url = ImageUtils.formatImageUrl(url, ImageUtils.MOBILE);
        }

        _this = this;
        ImageRequest imageRequest = new ImageRequest(
                url,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        _this.setImageBitmap(response);
                    }
                }, 0, 0, Config.RGB_565, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError paramVolleyError) {

            }
        });
        Network.getInstance().addToRequestQueue(imageRequest, "LoadImageView");
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = measureWidth(widthMeasureSpec);
        int height = measureHeight(heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    private int measureWidth(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            // The parent has determined an exact size for the child.
            result = specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            // The child can be as large as it wants up to the specified size.
            result = specSize;
        } else {
            // The parent has not imposed any constraint on the child.
            result = canvasSize;
        }

        return result;
    }

    private int measureHeight(int measureSpecHeight) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpecHeight);
        int specSize = MeasureSpec.getSize(measureSpecHeight);

        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            // The child can be as large as it wants up to the specified size.
            result = specSize;
        } else {
            // Measure the text (beware: ascent is a negative number)
            result = canvasSize;
        }

        return (result + 2);
    }

    public Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable == null) {
            return null;
        } else if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}