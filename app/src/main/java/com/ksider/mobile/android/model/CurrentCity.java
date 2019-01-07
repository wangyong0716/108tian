package com.ksider.mobile.android.model;

import java.io.Serializable;


public class CurrentCity implements Serializable {
    private static final long serialVersionUID = -5176864360371908901L;
	protected int mCityId;
	protected String mCityName;
	public CurrentCity(int cityId, String cityName){
		mCityId = cityId;
		mCityName = cityName;
	}
	
	public int getCityId() {
		return mCityId;
	}
	
	public void setCityId(int mCityId) {
		this.mCityId = mCityId;
	}
	
	public String getCityName() {
		return mCityName;
	}
	
	public void setCityName(String mCityName) {
		this.mCityName = mCityName;
	}
}
