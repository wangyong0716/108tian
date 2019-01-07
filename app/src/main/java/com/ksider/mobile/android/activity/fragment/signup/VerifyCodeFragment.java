package com.ksider.mobile.android.activity.fragment.signup;

import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.service.SmsContent;
import com.ksider.mobile.android.utils.APIUtils;
import com.ksider.mobile.android.utils.Network;
import com.ksider.mobile.android.utils.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class VerifyCodeFragment extends NetworkFragment {

    public static final String ARG_MOBILE = "mobile";
    private String phone;
    private Boolean mReset = false;
    private Button resendButton;
    private CountDownTimer countDownTimer;
    private VerifyFragment.VerifyStage mStage = VerifyFragment.VerifyStage.OBTAIN_CODE;
    private boolean isResendEnable = false;
    private SmsContent contentObserver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            phone = getArguments().getString(ARG_MOBILE);
            mReset = getArguments().getBoolean("reset", false);
        }
    }

    private void resendCode() {
        if (countDownTimer == null) {
            countDownTimer = new ResendCountDownTimer(60000, 1000).start();
        }
        resendButton.setEnabled(false);
        countDownTimer.start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_verify_code, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EditText phone = (EditText) view.findViewById(R.id.phone_edit);
        phone.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView arg0, int actionId, KeyEvent event) {
                if (EditorInfo.IME_ACTION_DONE == actionId) {
                    return true;
                }
                return false;
            }
        });
        phone.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (getView() != null) {
                    getView().findViewById(R.id.resend_code_button).setEnabled(editable.length() == 11);
                }
            }
        });
        EditText verify_code_edit = (EditText) view.findViewById(R.id.verify_code_edit);
        verify_code_edit.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView text, int actionId, KeyEvent event) {
                if (EditorInfo.IME_ACTION_DONE == actionId) {
                    return true;
                }
                return false;
            }
        });
        verify_code_edit.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        view.findViewById(R.id.nextStep).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verifyCode();
            }
        });
        resendButton = (Button) view.findViewById(R.id.resend_code_button);
        resendButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (requestCode()) {
                    resendCode();
                }
            }
        });

        //add ContentObserver to listener SMS
        contentObserver = new SmsContent(getActivity(), new Handler(), verify_code_edit);
        getActivity().getContentResolver().registerContentObserver(Uri.parse("content://sms/"), true, contentObserver);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (contentObserver != null)
            getActivity().getContentResolver().unregisterContentObserver(contentObserver);
    }

    private Boolean requestCode() {
        if (getView() == null) {
            return false;
        }
        phone = ((EditText) getView().findViewById(R.id.phone_edit)).getText().toString();
        if (StringUtils.checkMobilePhone(phone)) {
            String url = APIUtils.getCode(phone, mReset);
            JsonObjectRequest req = getRequest(url);
            Network.getInstance().addToRequestQueue(req);
            getView().findViewById(R.id.resend_code_button).setEnabled(false);
            return true;
        } else {
            StringUtils.setError((EditText) getView().findViewById(R.id.phone_edit), "电话号码不正确!");
        }
        return false;
    }

    private void verifyCode() {
        if (getView() == null) {
            return;
        }
        phone = ((EditText) getView().findViewById(R.id.phone_edit)).getText().toString();
        Editable verifyCode = ((EditText) getView().findViewById(R.id.verify_code_edit)).getText();
        if (phone == null || phone.length() != 11) {
            StringUtils.setError((EditText) getView().findViewById(R.id.phone_edit), "电话号码不正确!");
            return;
        }
        if (verifyCode == null || TextUtils.isEmpty(verifyCode)) {
            StringUtils.setError((EditText) getView().findViewById(R.id.verify_code_edit), "验证输入错误!");
            return;
        }
        String url = APIUtils.verifyCode(phone, verifyCode.toString(), mReset);
        JsonObjectRequest req = getRequest(url);
        Network.getInstance().addToRequestQueue(req);
    }

    protected void nextStep(String sid) {
        if (getView() == null
                || getView().findViewById(R.id.verify_code_edit) == null) {
            return;
        }
        Editable verifyCode = ((EditText) getView().findViewById(R.id.verify_code_edit)).getText();
        VerifyFragment.VerifyFactor factor = new VerifyFragment.VerifyFactor(phone, verifyCode.toString());
        factor.sid = sid;
        ChildCallback callback = (ChildCallback) getActivity();
        callback.onStageChange(VerifyFragment.VerifyStage.SET_PASSWORD, factor);
    }

    /**
     * 用于重发验证码倒计时
     */
    private class ResendCountDownTimer extends CountDownTimer {

        public ResendCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long remind) {
            if (!isAdded()) {
                return;
            }
            resendButton.setText(getString(R.string.resend_verify_code_countdown, remind / 1000));
        }

        @Override
        public void onFinish() {
            resendButton.setText(R.string.resend_verify_code);
            resendButton.setEnabled(true);
            isResendEnable = true;
        }
    }

    @Override
    public void proccess(JSONObject data) {
        try {
            if (data.getInt("status") == 0) {
                if (mStage == VerifyFragment.VerifyStage.OBTAIN_CODE) {
                    mStage = VerifyFragment.VerifyStage.VERIFY;
                } else {
                    JSONObject json = data.getJSONObject("data");
                    nextStep(json.getString("sid"));
                }
            } else {
                setError(data.getString("msg"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}
