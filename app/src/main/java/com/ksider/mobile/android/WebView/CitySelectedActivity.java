package com.ksider.mobile.android.WebView;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import com.ksider.mobile.android.model.MessageEvent;
import com.ksider.mobile.android.slide.SlidingLayout;
import com.ksider.mobile.android.utils.Storage;
import com.ksider.mobile.android.view.listview.IndexBarView;
import com.ksider.mobile.android.view.listview.PinnedHeaderAdapter;
import com.ksider.mobile.android.view.listview.PinnedHeaderListView;
import de.greenrobot.event.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.*;

public class CitySelectedActivity extends BaseActivity {
    protected List<CityItem> mItems;
    protected ArrayList<CityItem> mListItems;
    protected Map<Integer, Integer> mSectionIndex;
    protected ArrayList<Integer> mListSectionPos;
    protected PinnedHeaderListView mListView;
    protected PinnedHeaderAdapter mAdaptor;
    // search box
    protected EditText mSearchView;
    protected ProgressBar mLoadingView;
    protected TextView mEmptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupViews();
        new SlidingLayout(this);
        customActionBar("城市筛选");

        mListSectionPos = new ArrayList<Integer>();
        mListItems = new ArrayList<CityItem>();
        mSectionIndex = new HashMap<Integer, Integer>();
        mItems = getCityList();
        // for handling configuration change
        if (savedInstanceState != null) {
            mListItems = (ArrayList<CityItem>) savedInstanceState.getSerializable("mListItems");
            mListSectionPos = savedInstanceState.getIntegerArrayList("mListSectionPos");
            if (mListItems != null && mListItems.size() > 0 && mListSectionPos != null && mListSectionPos.size() > 0) {
                setListAdaptor();
            }

            String constraint = savedInstanceState.getString("constraint");
            if (constraint != null && constraint.length() > 0) {
                mSearchView.setText(constraint);
                setIndexBarViewVisibility(constraint);
            }
        } else {
            new Poplulate().execute(mItems);
        }
    }

    private void setupViews() {
        setContentView(R.layout.activity_city_selected);
        mSearchView = (EditText) findViewById(R.id.search_city_edittext);
        mLoadingView = (ProgressBar) findViewById(R.id.loading_view);
        mListView = (PinnedHeaderListView) findViewById(R.id.list_view);
        mEmptyView = (TextView) findViewById(R.id.empty_view);
        findViewById(R.id.delete_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchView.setText("");
//                findViewById(R.id.delete_image).setVisibility(View.INVISIBLE);
            }
        });
    }

    public List<CityItem> getCityList() {
        String citys = Storage.sharedPref.getString("openCity", null);
        List<CityItem> items = new ArrayList<CityItem>();
        if (citys != null) {
            try {
                JSONObject openCity = new JSONObject(citys);
                for (Iterator<String> it = openCity.keys(); it.hasNext(); ) {
                    JSONArray province = openCity.getJSONArray(it.next());
                    for (int i = 0; i < province.length(); i++) {
                        try {
                            JSONObject city = province.getJSONObject(i);
                            CityItem item = new CityItem();
                            item.chname = city.getString("name");
                            item.enname = city.getString("city");
                            item.id = city.getInt("id");
                            item.type = PinnedHeaderAdapter.TYPE_ITEM;
                            items.add(item);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Collections.sort(items, new Comparator<CityItem>() {
            @Override
            public int compare(CityItem key, CityItem key1) {
                return key.enname.compareToIgnoreCase(key1.enname);
            }
        });
        return items;
    }

    public List<CityItem> getHotCityList() {
        String citys = Storage.sharedPref.getString("openCity", null);
        List<CityItem> items = new ArrayList<CityItem>();
        if (citys != null) {
            try {
                JSONObject openCity = new JSONObject(citys);
                for (Iterator<String> it = openCity.keys(); it.hasNext(); ) {
                    JSONArray province = openCity.getJSONArray(it.next());
                    for (int i = 0; i < province.length(); i++) {
                        try {
                            JSONObject city = province.getJSONObject(i);
                            Boolean isHot = city.getBoolean("isHot");
                            if (isHot) {
                                CityItem item = new CityItem();
                                item.chname = city.getString("name");
                                item.enname = city.getString("city");
                                item.id = city.getInt("id");
                                item.type = PinnedHeaderAdapter.TYPE_ITEM;
                                items.add(item);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Collections.sort(items, new Comparator<CityItem>() {
            @Override
            public int compare(CityItem key, CityItem key1) {
                return key.enname.compareToIgnoreCase(key1.enname);
            }
        });
        return items;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        mSearchView.addTextChangedListener(filterTextWatcher);
        super.onPostCreate(savedInstanceState);
    }

    private void setListAdaptor() {
        mAdaptor = new PinnedHeaderAdapter(this, mListItems, mListSectionPos, mSectionIndex);
        mListView.setAdapter(mAdaptor);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CityItem item = mAdaptor.getItem(position);
                if (item != null) {
                    Storage.putInt("cityId", item.id);
                    Storage.putString("cityName", item.chname);
                    EventBus.getDefault().postSticky(new MessageEvent(MessageEvent.UPDATE_DEFAULTCITY));
                    CitySelectedActivity.this.finish();
                }
            }
        });

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        // set header view
        View pinnedHeaderView = inflater.inflate(R.layout.section_row_view, mListView, false);
        mListView.setPinnedHeaderView(pinnedHeaderView);

        IndexBarView indexBarView = (IndexBarView) inflater.inflate(R.layout.index_bar_view, mListView, false);
        indexBarView.setData(mListView, mListItems, mListSectionPos);
        mListView.setIndexBarView(indexBarView);

        // set preview text view
        View previewTextView = inflater.inflate(R.layout.preview_view, mListView, false);
        mListView.setPreviewView(previewTextView);

        // for configure pinned header view on scroll change
        mListView.setOnScrollListener(mAdaptor);
    }

    private TextWatcher filterTextWatcher = new TextWatcher() {
        public void afterTextChanged(Editable s) {
            String str = s.toString();
            if (mAdaptor != null && str != null) {
                mAdaptor.getFilter().filter(str);
            }
            if (str != null && !str.equals("")) {
                findViewById(R.id.delete_image).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.delete_image).setVisibility(View.INVISIBLE);
            }
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int
                after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int
                count) {

        }
    };

    public class ListFilter extends Filter {
        @Override
        public FilterResults performFiltering(CharSequence constraint) {
            String constraintStr = constraint.toString().toLowerCase(Locale.getDefault());
            FilterResults result = new FilterResults();

            if (constraint != null && constraint.toString().length() > 0) {
                ArrayList<CityItem> filterItems = new ArrayList<CityItem>();
                synchronized (this) {
                    for (CityItem item : mItems) {
                        if (item.enname.startsWith(constraintStr) || item.chname.startsWith(constraintStr)) {
                            filterItems.add(item);
                        }
                    }
                    result.count = filterItems.size();
                    result.values = filterItems;
                }
            } else {
                synchronized (this) {
                    result.count = mItems.size();
                    result.values = mItems;
                }
            }
            return result;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            ArrayList<CityItem> filtered = (ArrayList<CityItem>) results.values;
            setIndexBarViewVisibility(constraint.toString());
            // sort array and extract sections in background Thread
            new Poplulate().execute(filtered);
        }
    }

    private void setIndexBarViewVisibility(String constraint) {
        // hide index bar for search results
        if (constraint != null && constraint.length() > 0) {
            mListView.setIndexBarVisibility(false);
        } else {
            mListView.setIndexBarVisibility(true);
        }
    }

    private class Poplulate extends AsyncTask<List<CityItem>, Void, Void> {

        private void showLoading(View contentView, View loadingView, View emptyView) {
            contentView.setVisibility(View.GONE);
            loadingView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }

        private void showContent(View contentView, View loadingView, View emptyView) {
            contentView.setVisibility(View.VISIBLE);
            loadingView.setVisibility(View.GONE);
            emptyView.setVisibility(View.GONE);
        }

        private void showEmptyText(View contentView, View loadingView, View emptyView) {
            contentView.setVisibility(View.GONE);
            loadingView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPreExecute() {
            // show loading indicator
            showLoading(mListView, mLoadingView, mEmptyView);
            super.onPreExecute();
        }

        protected void getCurrentCity(List<CityItem> list) {
            CityItem item = new CityItem();
            item.chname = "当前城市";
            item.enname = "当前城市";
            item.type = PinnedHeaderAdapter.TYPE_SECTION;
            item.expand = true;
            list.add(item);
            mListSectionPos.add(list.size() - 1);
            item = new CityItem();
            item.id = Storage.getSharedPref().getInt("cityId", 1);
            item.chname = Storage.getSharedPref().getString("cityName", "北京");
            item.enname = item.chname;
            item.type = PinnedHeaderAdapter.TYPE_ITEM;
            list.add(item);
            mSectionIndex.put(mListItems.size() - 1, mListItems.size() - 2);
        }

        protected void setHotCity(List<CityItem> list) {
            CityItem item = new CityItem();
            item.chname = "热门城市";
            item.enname = "热门城市";
            item.type = PinnedHeaderAdapter.TYPE_SECTION;
            item.expand = true;
            list.add(item);
            int sectionIndex = list.size() - 1;
            mListSectionPos.add(sectionIndex);
            List<CityItem> hotCities = getHotCityList();
            for (CityItem city : hotCities) {
                list.add(city);
                mSectionIndex.put(mListItems.size() - 1, sectionIndex);
            }
        }

        @Override
        protected Void doInBackground(List<CityItem>... params) {
            mListItems.clear();
            mListSectionPos.clear();
            List<CityItem> items;
            boolean addHeader = true;
            if (params != null && params.length > 0) {
                items = params[0];
                if (items.size() == mItems.size()) {
                    addHeader = true;
                    getCurrentCity(mListItems);
                    setHotCity(mListItems);
                } else {
                    addHeader = false;
                }
            } else {
                getCurrentCity(mListItems);
                setHotCity(mListItems);
                items = mItems;
            }
            if (items != null && items.size() > 0) {
                String prev_section = "";
                int sectionIndex = 0;
                for (CityItem current_item : items) {
                    String current_section = current_item.enname.substring(0, 1).toUpperCase(Locale.getDefault());
                    if (!prev_section.equals(current_section) && addHeader) {
                        CityItem item = new CityItem();
                        item.chname = current_section;
                        item.enname = current_section;
                        item.type = PinnedHeaderAdapter.TYPE_SECTION;
                        mListItems.add(item);
                        sectionIndex = mListItems.size() - 1;
                        mListSectionPos.add(sectionIndex);
                        prev_section = current_section;
                    }
                    mListItems.add(current_item);
                    if (addHeader) {
                        mSectionIndex.put(mListItems.size() - 1, sectionIndex);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (!isCancelled()) {
                if (mListItems.size() <= 0) {
                    showEmptyText(mListView, mLoadingView, mEmptyView);
                } else {
                    setListAdaptor();
                    showContent(mListView, mLoadingView, mEmptyView);
                }
            }
            super.onPostExecute(result);
        }
    }

    public class SortIgnoreCase implements Comparator<Map<String, String>> {
        public int compare(String s1, String s2) {
            return s1.compareToIgnoreCase(s2);
        }

        @Override
        public int compare(Map<String, String> key, Map<String, String> key2) {

            return 0;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mListItems != null && mListItems.size() > 0) {
            outState.putSerializable("mListItems", mListItems);
        }
        if (mListSectionPos != null && mListSectionPos.size() > 0) {
            outState.putIntegerArrayList("mListSectionPos", mListSectionPos);
        }
        super.onSaveInstanceState(outState);
    }

    public static class CityItem implements Serializable {
        private static final long serialVersionUID = -2584778988712738502L;
        public String chname;
        public String enname;
        public int type;
        public int id;
        public Boolean expand = false;
    }
}
