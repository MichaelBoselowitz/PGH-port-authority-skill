/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.maya.portAuthority.googleMaps;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.maya.portAuthority.InvalidInputException;
import com.maya.portAuthority.util.Coordinates;
import com.maya.portAuthority.util.Stop;

/**
 *
 * @author Adithya
 */
public class LocationTracker {

	private static Logger log = LoggerFactory.getLogger(LocationTracker.class);

    /**
     * Case 1: Request returns list of Coordinates
     * Proceed to Step 2
     * 
     * Case 2: Unable to understand source location
     * Ask user to try again.

     * 
     * @param json returned by striking the Google maps API
     * @param limit set limit to the number of places returned by the API
     * @return
     * @throws JSONException 
     */
    public static List<Coordinates> getLatLngDetails(JSONObject json, int limit) throws JSONException, InvalidInputException {
    	List<Coordinates> coordinates = new ArrayList<>();
        JSONArray results = json.getJSONArray("results");
        log.debug("JSON Results Size={}",results.length());
        if (results.length() == 0) {
            throw new InvalidInputException("No results from JSON","I did not understand the source location");
        }
        int numResultsToReturn=Math.min(limit, results.length());
        for (int i = 0; i < numResultsToReturn; i++) {
        	JSONObject location = results.getJSONObject(i).getJSONObject("geometry").getJSONObject("location");
        	double lat = location.getDouble("lat");
        	double lng = location.getDouble("lng");
        	Coordinates c = new Coordinates();
        	c.setLat(lat);
        	c.setLng(lng);
        	String address = results.getJSONObject(i).getString("formatted_address");
        	c.setAddress(address);
        	coordinates.add(c);
        }
        return coordinates;
    }
    
    public static List<Stop> getStopDetails(JSONObject json) throws JSONException {
    	List<Stop> stops = new ArrayList<>();
        JSONArray stopsResponse = json.getJSONObject("bustime-response").getJSONArray("stops");

        if (stopsResponse != null) {
            for (int i = 0; i < stopsResponse.length(); i++) {
                JSONObject stop = stopsResponse.getJSONObject(i);
                double lat = stop.getDouble("lat");
                double lon = stop.getDouble("lon");
                String stopID = stop.getString("stpid");
                String stpnm = stop.getString("stpnm");
                Stop s = new Stop(stopID, stpnm, lat, lon);
                stops.add(s);
            }
        }
        return stops;
    }
}
