package com.ksider.mobile.android.activity.fragment.signup;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.utils.APIUtils;
import com.ksider.mobile.android.utils.Network;
import com.ksider.mobile.android.utils.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class BindPhoneFragment extends DialogFragment {
	private boolean isResendEnable = false;
	protected Button resendButton;
	protected Button verifyCodeButton;
	private CountDownTimer countDownTimer;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		View layout = getActivity().getLayoutInflater().inflate(R.layout.fragment_bind_phone, null);
		init(layout);
		builder.setView(layout);
		return builder.create();
	}

	protected void init(View view) {
		final EditText phone = (EditText) view.findViewById(R.id.phone_edit);
		phone.setOnEditorActionListener(new TextView.OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView arg0, int actionId, KeyEvent event) {
				if (EditorInfo.IME_ACTION_DONE == actionId) {
					return true;
				}
				return false;
			}
		});
		
		resendButton = (Button) view.findViewById(R.id.resend_code_button);
		verifyCodeButton = (Button) view.findViewById(R.id.verify_code_button);
		
		phone.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void afterTextChanged(Editable editable) {
				if (editable.length() == 11) {
					resendButton.setEnabled(true);
				}
			}
		});
		resendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				requestCode(phone);
			}
		});
		final EditText verify_code_edit = (EditText) view.findViewById(R.id.verify_code_edit);
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
				if (getView() != null) {
					verifyCodeButton.setEnabled(editable.length() > 2);
				}
			}
		});
		verifyCodeButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View view) {
				String pNo = phone.getText().toString();
				String code = verify_code_edit.getText().toString();
				 String url = APIUtils.bindPhone(pNo, code);
				 JsonObjectRequest req =  new JsonObjectRequest(url, null,new Listener<JSONObject>() {
						@Override
						public void onResponse(JSONObject response) {
							try {
								if(response.getInt("status") == 0){
									getDialog().cancel();
								}else{
									Toast.makeText(getActivity(), response.getString("msg"), Toast.LENGTH_LONG).show();
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					}, new ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError error) {
							Toast.makeText(getActivity(), "网络访问失败！", Toast.LENGTH_LONG).show();
						}
					});
				 Network.getInstance().addToRequestQueue(req);
			}
		});
	}
	protected void resend(){
		if (countDownTimer == null) {
			countDownTimer = new ResendCountDownTimer(60000, 1000).start();
		}
		resendButton.setEnabled(false);
		countDownTimer.start();
		verifyCodeButton.setEnabled(true);
	}

	private void requestCode(EditText phone_edit) {
		String phone = phone_edit.getText().toString();
		if (StringUtils.checkMobilePhone(phone)) {
			 String url = APIUtils.getBindCode(phone);
			 JsonObjectRequest req =  new JsonObjectRequest(url, null,new Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						try {
							if(response.getInt("status") == 0){
								resend();
							}else{
								if(getActivity() != null && response.getString("msg") != null) {
									Toast.makeText(getActivity(), response.getString("msg"), Toast.LENGTH_LONG).show();
								}
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}, new ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						if(getActivity() != null ) {
							Toast.makeText(getActivity(), "网络访问失败！", Toast.LENGTH_LONG).show();
						}
					}
				});
			Network.getInstance().addToRequestQueue(req);
		}
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
}
