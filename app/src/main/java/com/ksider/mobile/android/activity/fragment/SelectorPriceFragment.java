package com.ksider.mobile.android.activity.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.WebView.SelectorActivity;

public class SelectorPriceFragment extends BaseFragment {
    private int selectedId = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_selector_price, container, false);
        Bundle args = getArguments();
        try {
            selectedId = args.getInt("selectedId");
        } catch (Exception e) {
            selectedId = 0;
        }
        RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.radio);
        if (selectedId < radioGroup.getChildCount()) {
            ((RadioButton) radioGroup.getChildAt(selectedId)).setChecked(true);
        }
        radioGroup.setOnCheckedChangeListener(listener);
        return view;
    }

    private RadioGroup.OnCheckedChangeListener listener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            String priceName;
            int sortId;
            switch (checkedId) {
                case R.id.default_sort:
                    priceName = getResources().getString(R.string.selector_default_sort);
                    sortId = 0;
                    break;
                case R.id.price_asc_sort:
                    priceName = getResources().getString(R.string.selector_price_asc_sort);
                    sortId = 1;
                    break;
                case R.id.price_desc_sort:
                    priceName = getResources().getString(R.string.selector_price_desc_sort);
                    sortId = 2;
                    break;
                case R.id.distance_sort:
                    priceName = getResources().getString(R.string.selector_distance_sort);
                    sortId = 3;
                    break;
                default:
                    priceName = getResources().getString(R.string.selector_default_sort);
                    sortId = 0;
                    break;
            }
            ((SelectorActivity) getActivity()).refreshPrice(false, priceName);
            ((SelectorActivity) getActivity()).setSortId(sortId);
        }
    };
}
