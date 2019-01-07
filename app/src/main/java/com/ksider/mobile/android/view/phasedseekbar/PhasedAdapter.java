package com.ksider.mobile.android.view.phasedseekbar;

import android.graphics.drawable.StateListDrawable;

public interface PhasedAdapter {

    public int getCount();

    public StateListDrawable getItem(int position);

}
