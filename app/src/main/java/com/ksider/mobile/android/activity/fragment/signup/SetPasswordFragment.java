package com.ksider.mobile.android.activity.fragment.signup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import com.ksider.mobile.android.utils.APIUtils;
import com.ksider.mobile.android.utils.Network;
import com.ksider.mobile.android.utils.StringUtils;
import com.ksider.mobile.android.utils.UserInfo;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created with IntelliJ IDEA. User: liuqi Date: 13-6-6 Time: 下午5:35 To change
 * this template use File | Settings | File Templates.
 */
public class SetPasswordFragment extends NetworkFragment {

    public static final String ARG_SESSION = "sessionId";
    private String mSessionId;

    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSessionId = getArguments().getString(ARG_SESSION);
        }
//        MessageUtils.register(MessageUtils.NOTIFY_LOGIN_RESULT, this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        MessageUtils.unregister(MessageUtils.NOTIFY_LOGIN_RESULT, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup_set_password, container, false);
        if (getArguments() != null) {
            if (!getArguments().getBoolean("reset")) {
                ((Button) view.findViewById(R.id.signup_button)).setText(R.string.set_pwd);
            }
        }
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((EditText) view.findViewById(R.id.password_confirm_edit)).setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    signup();
                }
                return false;
            }
        });
        view.findViewById(R.id.signup_button).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                signup();
            }
        });
    }

    private void signup() {
        EditText passwordEdit = ((EditText) getView().findViewById(R.id.password_edit));
        String password = passwordEdit.getText().toString();

        if (TextUtils.isEmpty(password) || password.length() < 6 || password.length() > 32) {
            StringUtils.setError(passwordEdit, getString(R.string.password_illegal_hint));
            return;
        }

        EditText confirmEdit = ((EditText) getView().findViewById(R.id.password_confirm_edit));
        if (!password.equals(confirmEdit.getText().toString())) {
            StringUtils.setError(confirmEdit, getString(R.string.password_inconsistent_hint));
            return;
        }
        String url = APIUtils.resetPassword(mSessionId, password);
        JsonObjectRequest req = getRequest(url);
        Network.getInstance().addToRequestQueue(req);
    }

    @Override
    public void proccess(JSONObject data) {
        try {
            if (data.getInt("status") == 0) {
                UserInfo.store(data.getJSONObject("data"));
                if (getActivity() != null) {
                    Intent intent = new Intent();
                    getActivity().setResult(1359, intent);
                    getActivity().finish();
                }
            } else {
                setError(data.getString("msg"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
