package com.ksider.mobile.android.activity.fragment.signup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.ksider.mobile.android.WebView.R;
import org.json.JSONObject;

/**
 * Created by wenkui on 2/11/15.
 */
public class VerifyFragment extends NetworkFragment{
    public static class VerifyFactor {
        public String phone;
        public String verifyCode;
        public String sid;

        public VerifyFactor(String phone, String verifyCode) {
            this.phone = phone;
            this.verifyCode = verifyCode;
        }
    }

    public enum VerifyStage {
        OBTAIN_CODE, VERIFY, SET_PASSWORD
    }

    private Boolean mReset = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mReset = getArguments().getBoolean("reset", false);
    }
        @Override
    public void proccess(JSONObject data) {

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_verify_code, container, false);
        return view;
    }
}
