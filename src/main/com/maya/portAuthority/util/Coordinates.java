package com.maya.portAuthority.util;

/**
 *
 * @author Adithya
 */
public class Coordinates {

    
    private double lat;
    private double lng;
    private String address;
    
    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@Override
	public String toString() {
		return "Coordinates [lat=" + lat + ", lng=" + lng + ", address=" + address + "]";
	}


    
}
