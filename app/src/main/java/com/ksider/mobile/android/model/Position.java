package com.ksider.mobile.android.model;

import java.io.Serializable;

public class Position implements Serializable {
    private static final long serialVersionUID = -5160641505731688019L;
	public double latitude;
	public double longitude;
	public Position(){
		
	}
	public Position(double lat, double lng){
		latitude = lat;
		longitude = lng;
	}
}
