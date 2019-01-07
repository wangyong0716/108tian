// @author Bhavya Mehta
package com.ksider.mobile.android.view.listview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.ksider.mobile.android.WebView.CitySelectedActivity.CityItem;
import com.ksider.mobile.android.WebView.R;

import java.util.ArrayList;
import java.util.List;

// Represents right side index bar view with unique first latter of list view row text 
public class IndexBarView extends View {

	// index bar margin
	float mIndexbarMargin;

	// user touched Y axis coordinate value
	float mSideIndexY;

	// flag used in touch events manipulations
	boolean mIsIndexing = false;

	// holds current section position selected by user
	int mCurrentSectionPosition = -1;

	// array list to store section positions
	public ArrayList<Integer> mListSections;

	// array list to store listView data
	List<CityItem> mListItems;

	// paint object
	Paint mIndexPaint;

	// context object
	Context mContext;

	// interface object used as bridge between list view and index bar view for
	// filtering list view content on touch event
	IIndexBarFilter mIndexBarFilter;

	public IndexBarView(Context context) {
		super(context);
		this.mContext = context;
	}

	public IndexBarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
	}

	public IndexBarView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.mContext = context;
	}

	public void setData(PinnedHeaderListView listView, List<CityItem> listItems, ArrayList<Integer> listSections) {
		this.mListItems = listItems;
		this.mListSections = listSections;

		// list view implements mIndexBarFilter interface
		mIndexBarFilter = listView;

		// set index bar margin from resources
		mIndexbarMargin = mContext.getResources().getDimension(R.dimen.index_bar_view_margin);
		// index bar item color and text size
		mIndexPaint = new Paint();
		mIndexPaint.setColor(mContext.getResources().getColor(R.color.black));
		mIndexPaint.setAntiAlias(true);
		mIndexPaint.setTextSize(mContext.getResources().getDimension(R.dimen.index_bar_view_text_size));
	}

	protected ArrayList<Integer> getSection() {
		ArrayList<Integer> sections = (ArrayList<Integer>) mListSections.clone();
		if (sections.size() > 1) {
			sections.remove(0);
			sections.remove(0);
		}
		return sections;
	}

	// draw view content on canvas using paint
	@Override
	protected void onDraw(Canvas canvas) {
		ArrayList<Integer> sections = getSection();
		if (sections != null && sections.size() > 1) {
			float sectionHeight = (getMeasuredHeight() - 2 * mIndexbarMargin) / sections.size();
			float paddingTop = (sectionHeight - (mIndexPaint.descent() - mIndexPaint.ascent())) / 2;
			for (int i = 0; i < sections.size(); i++) {
				float paddingLeft = (getMeasuredWidth() - mIndexPaint.measureText(getSectionText(sections.get(i)))) / 2;
				canvas.drawText(getSectionText(sections.get(i)), paddingLeft, mIndexbarMargin
				        + (sectionHeight * i) + paddingTop + mIndexPaint.descent(), mIndexPaint);
			}
		}
		super.onDraw(canvas);
	}

	public String getSectionText(int sectionPosition) {
		return mListItems.get(sectionPosition).enname;
	}

	boolean contains(float x, float y) {
		// Determine if the point is in index bar region, which includes the
		// right margin of the bar
		return (x >= getLeft() && y >= getTop() && y <= getTop() + getMeasuredHeight());
	}

	void filterListItem(float sideIndexY) {
		mSideIndexY = sideIndexY;
		ArrayList<Integer> sections = getSection();
		// filter list items and get touched section position with in index bar
		mCurrentSectionPosition = (int) (((mSideIndexY) - getTop() - mIndexbarMargin) / ((getMeasuredHeight() - (2 * mIndexbarMargin)) / sections
		        .size()));

		if (mCurrentSectionPosition >= 0 && mCurrentSectionPosition < sections.size()) {
			int position = sections.get(mCurrentSectionPosition);
			String previewText = mListItems.get(position).chname;
			mIndexBarFilter.filterList(mSideIndexY, position, previewText);
		}
	}

	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {

		case MotionEvent.ACTION_DOWN:
			// If down event occurs inside index bar region, start indexing
			if (contains(ev.getX(), ev.getY())) {
				// It demonstrates that the motion event started from index
				// bar
				mIsIndexing = true;
				// Determine which section the point is in, and move the
				// list to
				// that section
				filterListItem(ev.getY());
				return true;
			} else {
				mCurrentSectionPosition = -1;
				return false;
			}
		case MotionEvent.ACTION_MOVE:
			if (mIsIndexing) {
				// If this event moves inside index bar
				if (contains(ev.getX(), ev.getY())) {
					// Determine which section the point is in, and move the
					// list to that section
					filterListItem(ev.getY());
					return true;
				} else {
					mCurrentSectionPosition = -1;
					return false;
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			if (mIsIndexing) {
				mIsIndexing = false;
				mCurrentSectionPosition = -1;
			}
			break;
		}
		return false;
	}
}
