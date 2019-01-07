package com.ksider.mobile.android.view.slidingtab;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.ksider.mobile.android.WebView.R;

public class TabIconView extends RelativeLayout {
	/**
	 * Custom message unread state variable for use with a
	 * {@link android.graphics.drawable.StateListDrawable}.
	 */
	private final int[] STATE_TAB_SELECTED = { R.attr.state_tab_selected };
	private boolean tabSelected;
	private TextView mTextView;

	public TabIconView(Context context, int drable, String tile) {
		super(context);
		loadViews(drable, tile);
	}

	private void loadViews(int drable, String title) {
		LayoutInflater layoutInflater = (LayoutInflater) getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View tabview = layoutInflater.inflate(R.layout.tab_home_icon, this, true);
		ImageView imageView = (ImageView) tabview.findViewById(R.id.tabicon);
		imageView.setImageResource(drable);
		mTextView = (TextView) tabview.findViewById(R.id.tabIcontitle);
		mTextView.setText(title);
	}

	public int convertDIPToPixels(int dip) {
		// In production code this method would exist in a utility library.
		// e.g see my ScreenUtils class: https://gist.github.com/2504204
		DisplayMetrics displayMetrics = getContext().getResources()
				.getDisplayMetrics();
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				dip, displayMetrics);
	}

	@Override
	protected int[] onCreateDrawableState(int extraSpace) {
		// If the message is unread then we merge our custom message unread
		// state into
		// the existing drawable state before returning it.
		if (tabSelected) {
			// We are going to add 1 extra state.
			final int[] drawableState = super
					.onCreateDrawableState(extraSpace + 1);
			mergeDrawableStates(drawableState, STATE_TAB_SELECTED);
			return drawableState;
		} else {
			return super.onCreateDrawableState(extraSpace);
		}
	}
	public void settabSelected(boolean state) {
		// Performance optimisation: only update the state if it has changed.
		if (this.tabSelected != state) {
//			Log.v(Constants.LOG_TAG, "settabSelected:"+state);

			if (state) {	
				mTextView.setTextColor(getContext().getResources().getColor(R.color.main_color));
			} else {
				mTextView.setTextColor(getContext().getResources().getColor(R.color.tab_title_color));
			}
			this.tabSelected = state;
			// Refresh the drawable state so that it includes the message unread
			// state if required.
			refreshDrawableState();
		}
	}

}
