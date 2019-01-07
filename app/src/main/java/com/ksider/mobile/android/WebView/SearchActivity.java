package com.ksider.mobile.android.WebView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ksider.mobile.android.slide.SlidingLayout;
import com.ksider.mobile.android.utils.APIUtils;
import com.ksider.mobile.android.utils.Network;
import com.ksider.mobile.android.utils.Storage;
import com.ksider.mobile.android.view.paging.PagingBaseAdapter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchActivity extends ActionBarActivity {
    private ListView listView;
    private SearchAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        new SlidingLayout(this);
        customActionBar();
        listView = (ListView) findViewById(R.id.search_list);
        View header = getLayoutInflater().inflate(R.layout.activity_search_header, null, false);
        listView.addHeaderView(header);
        adapter = new SearchAdapter(this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SearchItem item = adapter.getItem(position - 1);
                if (item != null) {
                    Intent intent = new Intent(SearchActivity.this, SearchResultActivity.class);
                    intent.putExtra("keyword", item.getName());
                    intent.putExtra("cityId", item.getId());
                    startActivity(intent);
                }
            }
        });
        Network.getInstance().addToRequestQueue(getRequest());
    }

    public void process(JSONArray array) {
        ArrayList<SearchItem> list = new ArrayList<SearchItem>();
        for (int i = 0; i < array.length(); i++) {
            try {
                SearchItem item = new SearchItem();
                JSONObject object = array.getJSONObject(i);
                item.setId(object.getInt("cityId"));
                item.setName(object.getString("word"));
                list.add(item);
            } catch (JSONException js) {
                js.printStackTrace();
            }
        }
        adapter.updateData(list);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    protected void customActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.white));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        ImageView backButton = (ImageView) findViewById(R.id.list_backbutton);
        backButton.setImageResource(R.drawable.backbutton_icon);
        backButton.getDrawable().setAlpha(255);

        backButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                SearchActivity.this.onBackPressed();
            }
        });
        final EditText editText = (EditText) findViewById(R.id.searchview);
        editText.setPadding((int) getResources().getDimension(R.dimen.margin_standard), 0, 0, 0);
        if (editText != null) {
            editText.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
            editText.setHintTextColor(getResources().getColor(R.color.translucent_white));
            editText.setOnKeyListener(new OnKeyListener() {
                @Override
                public boolean onKey(View view, int keyCode, KeyEvent event) {
                    if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        Intent intent = new Intent(SearchActivity.this, SearchResultActivity.class);
                        if (editText.getText() != null && editText.getText().toString().length() > 0) {
                            intent.putExtra("keyword", editText.getText().toString());
                            intent.putExtra("cityId", Storage.sharedPref.getInt("cityId", 1));
                            startActivity(intent);
                            editText.setText("");
                            return true;
                        }
                    }
                    return false;
                }
            });
            View submmit = findViewById(R.id.submmit);
            submmit.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (editText.getText() != null && editText.getText().toString().length() > 0) {
                        Intent intent = new Intent(SearchActivity.this, SearchResultActivity.class);
                        intent.putExtra("keyword", editText.getText().toString());
                        intent.putExtra("cityId", Storage.sharedPref.getInt("cityId", 1));
                        startActivity(intent);
                    }
                }
            });
        }
    }

    protected String getRequestUrl() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("action", "getHotWordList");
        params.put("cityId", Storage.sharedPref.getInt("cityId", 1));
        return APIUtils.getUrl(APIUtils.SEARCH, params);
    }


    protected JsonObjectRequest getRequest() {
        Log.v("AAA", "searchUrl=" + getRequestUrl());
        return new JsonObjectRequest(getRequestUrl(), null, new Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response != null && response.getInt("status") == 0) {
                        process(response.getJSONArray("data"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
    }

    class SearchItem {
        private String name;
        private int id;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }

    class SearchAdapter extends PagingBaseAdapter<SearchItem> {
        protected Activity mContext;

        public SearchAdapter(Activity context) {
            mContext = context;
        }

        public SearchAdapter(Activity context, List<SearchItem> list) {
            mContext = context;
            items = list;
        }

        public void updateData(List<SearchItem> list) {
            items = list;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public SearchItem getItem(int position) {
            if (position < 0 || position >= getCount()) {
                return null;
            }
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return items.get(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            ViewHolder holder;
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.list_search_item, parent, false);
                holder = new ViewHolder();
                holder.name = (TextView) view.findViewById(R.id.search_name);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            SearchItem item = getItem(position);
            holder.name.setText(item.getName());
            return view;
        }
    }

    class ViewHolder {
        public TextView name;
    }

}
