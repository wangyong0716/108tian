package com.ksider.mobile.android.model;

import java.util.List;

public class DetailHeaderDataModel extends DetailDataModel {

	private static final long serialVersionUID = 3741136411425222163L;
	public String location;    		//位置距离
	public String collection;  		//收藏数目
	public Boolean hasFavorator; 	//是否已经收收藏
	public List<String> album;		//是否已经收收藏
	public boolean expire;			//是否过期
	public String price;			//价格
}
