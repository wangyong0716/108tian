package com.ksider.mobile.android.activity.fragment.signup;

import android.support.v4.app.DialogFragment;
import android.widget.Toast;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONObject;

public abstract class NetworkFragment extends DialogFragment {

	protected void setError(String error) {
		if(getActivity() != null && error != null) {
			Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
		}
	}

	protected JsonObjectRequest getRequest(String url) {

		return new JsonObjectRequest(url, null, new Listener<JSONObject>() {
			@Override
			public void onResponse(JSONObject response) {
				proccess(response);
			}
		}, new ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				setError("网络访问失败!");
			}
		});
	}

	public abstract void proccess(JSONObject data);
}
