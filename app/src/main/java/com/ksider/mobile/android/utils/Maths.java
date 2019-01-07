package com.ksider.mobile.android.utils;



public class Maths {
//	protected static final double EARTH_RADIUS = 6378.137;//地球半径公里
	protected static final double EARTH_RADIUS = 6367;//地球半径公里
	protected static double rad(double d){
		return d*Math.PI/180.0;
	}
	
	/**
	 * 
	 * @param srclat  第一个点维度
	 * @param srclng  第一个点经度
	 * @param dstlat  第二个点维度
	 * @param dstlng  第二个点经度
	 * @return  返回两点之间的距离，单位公里
	 */
	public static double GetDistance(double srclat, double srclng, double dstlat, double dstlng){
		double radsrclat = rad(srclat);   
	    double raddstlat = rad(dstlat);   
	    double a = radsrclat - raddstlat;   
	    double b = rad(srclng) - rad(dstlng);   
	    double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a/2),2) +   
	    		Math.cos(radsrclat)*Math.cos(raddstlat)*Math.pow(Math.sin(b/2),2)));   
	    s = s * EARTH_RADIUS;
	    s = Math.round(s*100)*1.0/100;
	    return s;
	}

	public static double getSelfDistance(double dstlat, double dstlng){
		Double lat = (double) 0;
		Double lng = (double) 0;
		String coord = Storage.sharedPref.getString("position", "116.403875,39.915168");
		if (coord != null) {
			String[] coords = coord.split(",");
			if (coords.length == 2) {
				lng = Double.parseDouble(coords[0]);
				lat = Double.parseDouble(coords[1]);
			}
		}
//		Log.v(Constants.LOG_TAG, "coords:"+coord+" dstlat:"+dstlat+" dstlng:"+dstlng);
		if(lat == 0 || lng == 0){
			return -1;
		}
		return GetDistance(lat, lng, dstlat, dstlng);
	}

}
