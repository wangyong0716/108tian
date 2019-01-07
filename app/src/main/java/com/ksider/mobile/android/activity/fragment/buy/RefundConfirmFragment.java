package com.ksider.mobile.android.activity.fragment.buy;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.ksider.mobile.android.WebView.R;

/**
 * Created by yong on 2015/6/2.
 */
public class RefundConfirmFragment extends Fragment {
    protected View mRoot;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_refund_confirm, container, false);
        mRoot.findViewById(R.id.submit).setOnClickListener(submitListener);

        String content = getResources().getString(R.string.refund_handle_info);
        SpannableStringBuilder builder = new SpannableStringBuilder(content);

        ForegroundColorSpan blueColor = new ForegroundColorSpan(getResources().getColor(R.color.personal_info_text_color_selected));
        ForegroundColorSpan redColor = new ForegroundColorSpan(getResources().getColor(R.color.red));
        ForegroundColorSpan blackColor = new ForegroundColorSpan(getResources().getColor(R.color.label_color));

        AbsoluteSizeSpan largeSizeSpan = new AbsoluteSizeSpan((int) getResources().getDimension(R.dimen.text_large_size));
        AbsoluteSizeSpan middleSizeSpan = new AbsoluteSizeSpan((int) getResources().getDimension(R.dimen.text_middle_size));

        int numberLength = "108".length();
        int nameLength = "108天".length();
        int durationStart = content.indexOf('5');
        int durationEnd = durationStart + "5个工作日".length();
        builder.setSpan(blueColor, 0, nameLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setSpan(blackColor, nameLength, durationStart, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setSpan(redColor, durationStart, durationEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setSpan(blackColor, durationEnd, content.length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        builder.setSpan(largeSizeSpan, 0, numberLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setSpan(middleSizeSpan, numberLength, content.length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ((TextView) mRoot.findViewById(R.id.soft_tips)).setText(builder);

        return mRoot;
    }

    private View.OnClickListener submitListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (getActivity() != null) {
                getActivity().finish();
            }
        }
    };
}
