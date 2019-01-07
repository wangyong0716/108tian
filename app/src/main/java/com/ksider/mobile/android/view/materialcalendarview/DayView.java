package com.ksider.mobile.android.view.materialcalendarview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckedTextView;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.utils.StringUtils;

import java.util.List;

/**
 * Display one day of a {@linkplain MaterialCalendarView}
 */
@SuppressLint("ViewConstructor")
class DayView extends CheckedTextView {
    private CalendarDay date;
    private int selectionColor = Color.GRAY;

    private final int fadeTime;
    private Drawable customBackground = null;
    private Drawable selectionDrawable;

    public DayView(Context context, CalendarDay day) {
        super(context);

        fadeTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        setSelectionColor(this.selectionColor);

        setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        setPadding(0, (int) getResources().getDimension(R.dimen.calendar_text_padding_top), 0, 0);
        setLineSpacing(getResources().getDimension(R.dimen.calendar_text_money_padding), 1);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            setTextAlignment(TEXT_ALIGNMENT_CENTER);
        }

        setDay(day);
    }

    public void setDay(CalendarDay date) {
        this.date = date;
        setText(getLabel());
    }

    public
    @NonNull
    String getLabel() {
        return String.valueOf(date.getDay());
    }

    public void addMoney(double price) {
        String money = "\n¥";
        if (price >= 100) {
            money += Math.round(price);
        } else {
            money += StringUtils.getPrice(price);
        }
        if (money.length() > 7) {
            money = "...";
        }

        String label = getLabel();
        String content = label + money;
        SpannableStringBuilder builder = new SpannableStringBuilder(content);

        if (date.getDayOfWeek() == 1 || date.getDayOfWeek() == 7) {
            ForegroundColorSpan dateColor = new ForegroundColorSpan(getResources().getColor(R.color.calendar_text_color_weekend));
            builder.setSpan(dateColor, 0, label.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            ForegroundColorSpan priceColor = new ForegroundColorSpan(getResources().getColor(R.color.orange));
            builder.setSpan(priceColor, label.length(), content.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            ForegroundColorSpan dateColor = new ForegroundColorSpan(getResources().getColor(R.color.calendar_text_color_normal));
            builder.setSpan(dateColor, 0, label.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            ForegroundColorSpan priceColor = new ForegroundColorSpan(getResources().getColor(R.color.calendar_default_money_color));
            builder.setSpan(priceColor, label.length(), content.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        AbsoluteSizeSpan absoluteSizeSpan = new AbsoluteSizeSpan((int) getResources().getDimension(R.dimen.calendar_money_text_size));
        builder.setSpan(absoluteSizeSpan, label.length(), content.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        builder.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC), label.length(), content.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        setText(builder);
    }

    public void setSelectionColor(int color) {
        this.selectionColor = color;
        regenerateBackground();
    }

    /**
     * @param selectionDrawable custom selection drawable
     */
    public void setSelectionDrawable(Drawable selectionDrawable) {
        this.selectionDrawable = selectionDrawable;
        invalidate();
    }

    /**
     * @param customBackground background to draw behind everything else
     */
    public void setCustomBackground(Drawable customBackground) {
        this.customBackground = customBackground;
        invalidate();
    }

    public CalendarDay getDate() {
        return date;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }

    protected void setupSelection(boolean showOtherDates, boolean inRange, boolean inMonth) {
        boolean enabled = inMonth && inRange;
        setEnabled(enabled);
        setVisibility(enabled || showOtherDates ? View.VISIBLE : View.INVISIBLE);
    }

    private final Rect tempRect = new Rect();

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        if (customBackground != null) {
            canvas.getClipBounds(tempRect);
            customBackground.setBounds(tempRect);
            customBackground.setState(getDrawableState());
            customBackground.draw(canvas);
        }
        super.onDraw(canvas);
    }

    private void regenerateBackground() {
        if (selectionDrawable != null) {
            setBackgroundDrawable(selectionDrawable);
        } else {
            setBackgroundDrawable(generateBackground(selectionColor, fadeTime));
        }
    }

    private static Drawable generateBackground(int color, int fadeTime) {
        StateListDrawable drawable = new StateListDrawable();
        drawable.setExitFadeDuration(fadeTime);
        drawable.addState(new int[]{android.R.attr.state_checked}, generateCircleDrawable(color));
        drawable.addState(new int[]{android.R.attr.state_pressed}, generateCircleDrawable(color));

        drawable.addState(new int[]{}, generateCircleDrawable(Color.TRANSPARENT));

        return drawable;
    }

    private static Drawable generateCircleDrawable(final int color) {
        ShapeDrawable drawable = new ShapeDrawable(new OvalShape());
//        ShapeDrawable drawable = new ShapeDrawable(new RectShape());
        drawable.setShaderFactory(new ShapeDrawable.ShaderFactory() {
            @Override
            public Shader resize(int width, int height) {
                return new LinearGradient(0, 0, 0, 0, color, color, Shader.TileMode.REPEAT);
            }
        });
        return drawable;
    }


    /**
     * @param facade apply the facade to us
     */
    void applyFacade(DayViewFacade facade) {
        setCustomBackground(facade.getBackgroundDrawable());
        setSelectionDrawable(facade.getSelectionDrawable());

        // Facade has spans
        List<DayViewFacade.Span> spans = facade.getSpans();
        if (!spans.isEmpty()) {
            String label = getLabel();
            SpannableString formattedLabel = new SpannableString(getLabel());
            for (DayViewFacade.Span span : spans) {
                formattedLabel.setSpan(span.span, 0, label.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            setText(formattedLabel);
        }
        // Reset in case it was customized previously
        else {
            setText(getLabel());
        }
    }
}
