package com.ksider.mobile.android.WebView;

import android.os.Bundle;
import com.ksider.mobile.android.model.TrafficInfoModel;

public class TrafficInfoActivity extends BaseActivity {
	TrafficInfoModel mInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_traffic_info);
		customActionBar("交通信息");
		mInfo = (TrafficInfoModel) getIntent().getExtras().getSerializable("data");
		if (mInfo != null) {
			setTextView(R.id.address, mInfo.address);
			setTextView(R.id.carLines, mInfo.carLines);
			setTextView(R.id.busLines, mInfo.busLines);
		}
	}
}
