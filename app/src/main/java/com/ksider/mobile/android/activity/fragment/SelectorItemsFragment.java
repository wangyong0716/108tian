package com.ksider.mobile.android.activity.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.WebView.SelectorActivity;
import com.ksider.mobile.android.model.ThemeModel;
import com.ksider.mobile.android.view.paging.gridview.PagingBaseAdapter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class SelectorItemsFragment extends BaseFragment {
    private GridView gridView;
    private ItemAdaptor adaptor;
    private int type = -1;
    private int selectedId = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_selector_location, container, false);
        gridView = (GridView) view.findViewById(R.id.grid_view);

        Bundle args = getArguments();
        try {
            type = args.getInt("type");
        } catch (Exception e) {
            type = -1;
        }
        try {
            selectedId = args.getInt("selectedId");
        } catch (Exception e) {
            selectedId = 0;
        }

        try {
            JSONArray data = new JSONArray(args.getString("data"));
            adaptor = new ItemAdaptor(getItems(data), getActivity(), selectedId);
            gridView.setAdapter(adaptor);
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ThemeModel item = adaptor.getItem(position);
                    if (type == SelectorActivity.TYPE_THEME) {
                        if (item.getId() <= 0) {
                            ((SelectorActivity) getActivity()).refreshTheme(false, getResources().getString(R.string.selector_theme));
                        } else {
                            ((SelectorActivity) getActivity()).refreshTheme(false, item.getName());
                        }
                        ((SelectorActivity) getActivity()).setThemeId(item.getId());
                    } else if (type == SelectorActivity.TYPE_LOCATION) {
                        if (item.getId() <= 0) {
                            ((SelectorActivity) getActivity()).refreshLocation(false, getResources().getString(R.string.selector_location));
                        } else {
                            ((SelectorActivity) getActivity()).refreshLocation(false, item.getName());
                        }
                        ((SelectorActivity) getActivity()).setRegionId(item.getId());
                    } else if (type == SelectorActivity.TYPE_PRICE) {
                        ((SelectorActivity) getActivity()).refreshPrice(false, item.getName());
                        ((SelectorActivity) getActivity()).setSortId(item.getId());
                    }
                }
            });
        } catch (JSONException js) {
            js.printStackTrace();
        }

        view.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SelectorActivity) getActivity()).close();
            }
        });
        return view;
    }

    public List<ThemeModel> getItems(JSONArray themes) {
        List<ThemeModel> list = new ArrayList<ThemeModel>();
        ThemeModel all = new ThemeModel();
        all.setId(-1);
        all.setName(getResources().getString(R.string.selector_theme_default_button));
        list.add(all);
        for (int i = 0; i < themes.length(); i++) {
            try {
                JSONObject object = themes.getJSONObject(i);
                if (object.getInt("_id") <= 0) {
                    continue;
                }
                ThemeModel model = new ThemeModel();
                model.setId(object.getInt("_id"));
                model.setName(object.getString("name"));
                list.add(model);
            } catch (JSONException js) {
                js.printStackTrace();
            }
        }
        return list;
    }
}

class ItemAdaptor extends PagingBaseAdapter<ThemeModel> {
    protected Activity mContext;
    protected int selectedId = 0;

    public ItemAdaptor(List<ThemeModel> items, Activity mContext, int selectedId) {
        super(items);
        this.mContext = mContext;
        this.selectedId = selectedId;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public ThemeModel getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder holder;
        ThemeModel model = getItem(position);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_item_item, parent, false);
            holder = new ViewHolder();
            holder.theme_name = (TextView) view.findViewById(R.id.theme_name);
            if (selectedId == model.getId()) {
                holder.theme_name.setBackgroundResource(R.drawable.bg_blue_button_rectangle_3);
                holder.theme_name.setTextColor(mContext.getResources().getColor(R.color.main_dark_green));
            } else {
                holder.theme_name.setBackgroundResource(R.drawable.bg_transparent_button_rectangle_3);
                holder.theme_name.setTextColor(mContext.getResources().getColor(R.color.selector_text_color));
            }
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }

        holder.theme_name.setText(model.getName());
        return view;
    }

    class ViewHolder {
        public TextView theme_name;
    }
}
