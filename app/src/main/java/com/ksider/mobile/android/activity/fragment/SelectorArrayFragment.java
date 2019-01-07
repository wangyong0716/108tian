package com.ksider.mobile.android.activity.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
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

public class SelectorArrayFragment extends BaseFragment {
    private int type = -1;
    private int selectedId = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_selector_theme, container, false);
        Bundle args = getArguments();
        try {
            type = args.getInt("type");
        } catch (Exception e) {
            type = -1;
        }
        try {
            selectedId = args.getInt("selectedId");
        } catch (Exception e) {
            selectedId = -1;
        }

        LinearLayout listLayout = (LinearLayout) view.findViewById(R.id.container);
        addDefaultButton(listLayout);
        try {
            JSONArray themes = new JSONArray(args.getString("data"));
            for (int i = 0; i < themes.length(); i++) {
                try {
                    View themeView = getActivity().getLayoutInflater().inflate(R.layout.list_theme_content, null);
                    JSONObject themeObject = themes.getJSONObject(i);
                    String themeTitle = themeObject.getString("title");
                    ((TextView) themeView.findViewById(R.id.theme_title)).setText(themeTitle);
                    JSONArray array = themeObject.getJSONArray("themes");
                    GridView gridView = (GridView) themeView.findViewById(R.id.grid_view);
                    final ThemeAdaptor adaptor = new ThemeAdaptor(getThemes(array), getActivity(), selectedId);
                    gridView.setAdapter(adaptor);
                    gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            ThemeModel item = adaptor.getItem(position);
                            if (type == SelectorActivity.TYPE_THEME) {
                                ((SelectorActivity) getActivity()).refreshTheme(false, item.getName());
                                ((SelectorActivity) getActivity()).setThemeId(item.getId());
                            } else if (type == SelectorActivity.TYPE_LOCATION) {
                                ((SelectorActivity) getActivity()).refreshLocation(false, item.getName());
                                ((SelectorActivity) getActivity()).setRegionId(item.getId());
                            } else if (type == SelectorActivity.TYPE_PRICE) {
                                ((SelectorActivity) getActivity()).refreshPrice(false, item.getName());
                                ((SelectorActivity) getActivity()).setSortId(item.getId());
                            }
                        }
                    });
                    listLayout.addView(themeView);
                } catch (JSONException js) {
                    js.printStackTrace();
                }

            }
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

    public void addDefaultButton(LinearLayout linearLayout) {
        View allView = getActivity().getLayoutInflater().inflate(R.layout.selector_theme_all_button, null);
        TextView all = (TextView) allView.findViewById(R.id.all);
        if (type == SelectorActivity.TYPE_THEME) {
            all.setText(R.string.selector_theme_default_button);
        } else if (type == SelectorActivity.TYPE_LOCATION) {
            all.setText(R.string.selector_location_default_button);
        }
        if (selectedId == -1) {
            all.setBackgroundResource(R.drawable.bg_blue_button_rectangle_3);
            all.setTextColor(getActivity().getResources().getColor(R.color.main_dark_green));
        } else {
            all.setBackgroundResource(R.drawable.bg_transparent_button_rectangle_3);
            all.setTextColor(getActivity().getResources().getColor(R.color.selector_text_color));
        }
        allView.findViewById(R.id.all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (type == SelectorActivity.TYPE_THEME) {
                    ((SelectorActivity) getActivity()).refreshTheme(false, getResources().getString(R.string.selector_theme));
                    ((SelectorActivity) getActivity()).setThemeId(-1);
                } else if (type == SelectorActivity.TYPE_LOCATION) {
                    ((SelectorActivity) getActivity()).refreshLocation(false, getResources().getString(R.string.selector_location));
                    ((SelectorActivity) getActivity()).setRegionId(-1);
                }
            }
        });
        linearLayout.addView(allView);
    }

    public List<ThemeModel> getThemes(JSONArray array) {
        List<ThemeModel> list = new ArrayList<ThemeModel>();
        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject object = array.getJSONObject(i);
                ThemeModel model = new ThemeModel();
                model.setId(object.getInt("id"));
                model.setName(object.getString("name"));
                list.add(model);
            } catch (JSONException js) {
                js.printStackTrace();
            }
        }
        return list;
    }
}

class ThemeAdaptor extends PagingBaseAdapter<ThemeModel> {
    protected Activity mContext;
    private int selectedId = -1;

    public ThemeAdaptor(List<ThemeModel> items, Activity mContext, int selectedId) {
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
            view = inflater.inflate(R.layout.list_theme_item, parent, false);
            holder = new ViewHolder();
            holder.theme_name = (TextView) view.findViewById(R.id.theme_name);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }

        holder.theme_name.setText(model.getName());
        if (selectedId == model.getId()) {
            holder.theme_name.setBackgroundResource(R.drawable.bg_blue_button_rectangle_3);
            holder.theme_name.setTextColor(mContext.getResources().getColor(R.color.main_dark_green));
        } else {
            holder.theme_name.setBackgroundResource(R.drawable.bg_transparent_button_rectangle_3);
            holder.theme_name.setTextColor(mContext.getResources().getColor(R.color.selector_text_color));
        }
        return view;
    }

    class ViewHolder {
        public TextView theme_name;
    }
}


