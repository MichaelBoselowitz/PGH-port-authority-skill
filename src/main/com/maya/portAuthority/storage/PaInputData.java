package com.maya.portAuthority.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.maya.portAuthority.util.Location;
import com.maya.portAuthority.util.Stop;

/**
 * 
 */
public class PaInputData {
	private static Logger log = LoggerFactory.getLogger(PaInputData.class);
    

	private String locationName;
	private String locationAddress;
    private String locationLat;
    private String locationLong;
    
    private String stopID;//Stop ID
    private String stopName;//Description of stop
    private double stopLat; //latitude
    private double stopLon; //longitude
    
    private String routeID;
    private String routeName;
    
    private String direction;

    public PaInputData() {
        // public no-arg constructor required for DynamoDBMapper marshalling
    }

    /**
     * Creates a new instance of {@link PaInputData} with initialized but empty player and
     * score information.
     * 
     * @return
     */
    public static PaInputData newInstance() {
        PaInputData newInstance = new PaInputData();
        //newInstance.setPlayers(new ArrayList<String>());
        //newInstance.setScores(new HashMap<String, Long>());
        return newInstance;
    }

	public String getLocationName() {
		return locationName;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

	public String getLocationLat() {
		return locationLat;
	}

	public void setLocationLat(String locationLat) {
		this.locationLat = locationLat;
	}

	public String getLocationLong() {
		return locationLong;
	}

	public void setLocationLong(String locationLong) {
		this.locationLong = locationLong;
	}

	public String getStopID() {
		return stopID;
	}

	public void setStopID(String stopID) {
		this.stopID = stopID;
	}

	public String getStopName() {
		return stopName;
	}

	public void setStopName(String stopName) {
		this.stopName = stopName;
	}

	public double getStopLat() {
		return stopLat;
	}

	public void setStopLat(double stopLat) {
		this.stopLat = stopLat;
	}

	public double getStopLon() {
		return stopLon;
	}

	public void setStopLon(double stopLon) {
		this.stopLon = stopLon;
	}

	public String getRouteID() {
		return routeID;
	}

	public void setRouteID(String routeID) {
		this.routeID = routeID;
	}

	public String getRouteName() {
		return routeName;
	}

	public void setRouteName(String routeName) {
		this.routeName = routeName;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}
	
	public String getLocationAddress() {
		return locationAddress;
	}

	public void setLocationAddress(String locationAddress) {
		this.locationAddress = locationAddress;
	}

	public void setStop(Stop stop){
		setStopName(stop.getStopName());
		setStopID(stop.getStopID());
		setStopLat(stop.getLatitude());
		setStopLon(stop.getLongitude());
	}
	
	public void setLocation( Location c){
		setLocationAddress(c.getAddress());
		setLocationLat(c.getLat() + "");
		setLocationLong(c.getLng() + "");
	}
	
	public String toString() {
		return "PaInputData [locationName=" + locationName + ", locationLat=" + locationLat + ", locationLong="
				+ locationLong + ", stopID=" + stopID + ", stopName=" + stopName + ", stopLat=" + stopLat + ", stopLon="
				+ stopLon + ", routeID=" + routeID + ", routeName=" + routeName + ", direction=" + direction + "]";
	}
    
}
