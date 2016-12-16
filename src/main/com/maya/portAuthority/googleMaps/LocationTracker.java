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

import com.maya.portAuthority.storage.PaInput;

/**
 *
 * @author Adithya
 */
public class LocationTracker {

	private static Logger log = LoggerFactory.getLogger(LocationTracker.class);

    List<Coordinates> coordinates = null;
    List<Stop> stops = null;
    ErrorMessage e = null;
    /**
     * Case 1: Request returns list of Coordinates
     * Proceed to Step 2
     * 
     * Case 2: Unable to understand source location
     * Ask user to try again.
     * 
     * Case 3: Source location very generic
     * Ask user to be more specific.
     * 
     * @param json returned by striking the Google maps API
     * @param limit set limit to the number of places returned by the API
     * @return
     * @throws JSONException 
     */
    public List<Coordinates> getLatLngDetails(JSONObject json, int limit) throws JSONException {
        coordinates = new ArrayList<>();
        e = new ErrorMessage();
        JSONArray results = json.getJSONArray("results");
        log.info("JSON Results Size={}",results.length());
        if (results != null) {
            if (results.length() != 0){// && results.length() <= limit) {
                for (int i = 0; i < results.length(); i++) {
                    JSONObject location = results.getJSONObject(i).getJSONObject("geometry").getJSONObject("location");
                    double lat = location.getDouble("lat");
                    double lng = location.getDouble("lng");
                    Coordinates c = new Coordinates();
                    c.setLat(lat);
                    c.setLng(lng);
                    coordinates.add(c);
                }
            } else if (results.length() == 0) {
                e.setError("I did not understand the source location.");
            //} else if (results.length() > limit) {
            //    e.setError("Could you please be more specific?");
            }
        }
        return coordinates;
    }
    
    public List<Stop> getStopDetails(JSONObject json) throws JSONException {
        stops = new ArrayList<>();
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
