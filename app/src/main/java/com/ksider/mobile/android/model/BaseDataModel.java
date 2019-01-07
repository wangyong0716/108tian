package com.ksider.mobile.android.model;

import java.io.Serializable;

public class BaseDataModel implements Serializable   {
	private static final long serialVersionUID = 76385405;
	public static String id_name="BaseData_id";
	public static String imgUrl_name="BaseData_imgUrl";
	public static String title_name="BaseData_title";
	public static String subTitle_name="BaseData_subTitle";
	public static String description_name="BaseData_description";
	public static String type_name="BaseData_type";

	public String id;
	public String imgUrl;
	public String title;
	public String subTitle;
	public String description;
	public String type;
}
