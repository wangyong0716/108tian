package com.ksider.mobile.android.activity.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.share.IShare;
import com.ksider.mobile.android.share.ShareEntity;
import com.ksider.mobile.android.share.ShareFactory;

public class ShareDiagLogFragment extends DialogFragment {

	public ShareDiagLogFragment() {

	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.getWindow().setType(R.style.ShareDialog);
		dialog.getWindow().setGravity(Gravity.BOTTOM);
		dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.share_layout);
		dialog.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		OnClickListener listener = new OnClickListener() {
			@Override
			public void onClick(View view) {
				IShare share = null;
				switch (view.getId()) {
					case R.id.share_friend:
						break;
					case R.id.share_weibo:
						share = ShareFactory.create(getActivity(),  ShareEntity.WEIBO);
						break;
					case R.id.share_weixin:
						break;
					case R.id.share_zone:
						break;
					default:
						break;
				}
				if (share != null) {
					share.share("http://108tian.com", "测试", null);
				}
			}
		};
		dialog.findViewById(R.id.share_weibo).setOnClickListener(listener);
		return dialog;
	}

	/*
	 * @Override public View onCreateView(LayoutInflater inflater, ViewGroup
	 * container, Bundle savedInstanceState) { View v =
	 * inflater.inflate(R.layout.share_layout, container, false); return null; }
	 */
}
