/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.maya.portAuthority.googleMaps;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.maya.portAuthority.InvalidInputException;

/**
 *
 * @author Adithya
 */
public class NearestStopLocator {

	private static Logger log = LoggerFactory.getLogger(NearestStopLocator.class);
    //LocationTracker track = null;

    //public NearestStopLocator() {
        //track = new LocationTracker();
    //}
   
    public static Stop process(Coordinates source, String routeID, String direction) throws IOException, JSONException, InvalidInputException{
    	log.info("Process: source={}, routeId={}, direction={}", source, routeID, direction);
    	if ((source==null)||(routeID==null)||(direction==null)){
    		log.error("Bad Input");
    		throw new InvalidInputException("Null input: source="+source+",routeID="+routeID+",direction="+direction,"I forgot something");
    	}

        //Get list of stops of the route# returned by truetime:
        List<Stop> listOfStops = getStops(routeID, direction);
        
        //find nearest stop from the source location:
        Stop nearestStop = findNearestStop(source.getLat(), source.getLng(), listOfStops);
        
        return nearestStop;
        
    }
    
    public static Coordinates getSourceLocation(String source)throws IOException, JSONException, InvalidInputException {
    	List<Coordinates> sourceLocation = getSourceLocationCoordinates(source);
        
        //get the first location returned:
       return(sourceLocation.get(0));
       
    }
    
    /**
     * Pads "Pittsburgh" to input and requests the Google location API which returns list of location details.
     * 
     * @param source name / Landmark / Any keyword/s to identify source location
     * @return List of top 5 location details. (1st being the most relevant)
     * @throws IOException
     * @throws JSONException 
     */
    public static List<Coordinates> getSourceLocationCoordinates(String source) throws IOException, JSONException, InvalidInputException {
    	log.info("getSourceLocationCoordinates: source={}", source);
    	JSONObject currentLocationDetails = null;
        List<Coordinates> listOfLocationCoordinates = null;
        String currLocation = (source + " Pittsburgh").replaceAll("\\s", "+");
        String currLocationURL = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=" + currLocation + "&sensor=false&key=AIzaSyBzW19DGDOi_20t46SazRquCLw9UNp_C8s";
        currentLocationDetails = readJsonFromUrl(currLocationURL);
        if (currentLocationDetails != null) {
            listOfLocationCoordinates = LocationTracker.getLatLngDetails(currentLocationDetails, 5);
        }
        return listOfLocationCoordinates;
    }
    
    /**
     * Gets list of stops for a route#
     * @param routeID
     * @param direction
     * @return
     * @throws IOException
     * @throws JSONException 
     */
    public static List<Stop> getStops(String routeID, String direction) throws IOException, JSONException{
    	log.info("getStops: routeId={}, direction={}", routeID, direction);
    	String url =  "http://truetime.portauthority.org/bustime/api/v2/getstops?key=929FvbAPSEeyexCex5a7aDuus&rt="+routeID+"&dir="+direction.toUpperCase()+"&format=json";
       JSONObject stopsJSON = null;
       List<Stop> listOfStops = null;
       stopsJSON = readJsonFromUrl(url);
       listOfStops = LocationTracker.getStopDetails(stopsJSON);
       return listOfStops;
    }
    
    /**
     * Finds nearest bus stop by determining the shortest distance between source location and 
     * list of bus stop locations returned by truetime API.
     * @param sourceLat
     * @param sourceLon
     * @param stops
     * @return
     * @throws JSONException
     * @throws IOException 
     */
    public static Stop findNearestStop(double sourceLat, double sourceLon, List<Stop> stops) throws JSONException, IOException{
    	log.info("findNearestStop: sourceLat={}, sourceLon={}, stops={}", sourceLat, sourceLon, stops);
    	Stop nearestStop = null;
        double shortestDistance = Double.MAX_VALUE;
        double distance;
        for(Stop s : stops){
            distance = calculateDistance(sourceLat, sourceLon, s.getLatitude(), s.getLongitude());
            if( distance < shortestDistance){
                shortestDistance = distance;
                nearestStop = s;
            }
        }
        return nearestStop;
    }
    /**
     * Calculates walking distance (in ft) between source and destination locations using Google distancematrix API
     * @param sourceLat
     * @param sourceLon
     * @param destLat
     * @param destLon
     * @return 
     */
    public static double calculateDistance(double sourceLat, double sourceLon, double destLat, double destLon) throws JSONException, IOException{
    	log.info("calculateDistance: sourceLat={}, sourceLon={}, destLat={}, destLon={}", sourceLat, sourceLon, destLat,destLon );
    	String url =  "https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial&origins="+sourceLat+","+sourceLon+"&destinations="+destLat+","+destLon+"&mode=walk&transit_mode=walking&key=AIzaSyBzW19DGDOi_20t46SazRquCLw9UNp_C8s";
       JSONObject distanceJSON = null;
       String distance = "";
       distanceJSON = readJsonFromUrl(url);
       distance = distanceJSON.getJSONArray("rows").getJSONObject(0).getJSONArray("elements").getJSONObject(0).getJSONObject("distance").getString("text");
       return convertMileToFeet(distance);
    }
    
    /***************************************Util methods***********************************************/

    public static double convertMileToFeet(String distance){
    	log.info("convertMileToFeet: distance={}", distance);
    	
        double result = 0.0;
        if(distance.contains("mi")){
            result = Double.parseDouble(distance.substring(0, distance.length() - 2))*5280.0;
        }else if(distance.contains("ft")){
            result = Double.parseDouble(distance.substring(0, distance.length() - 2));
        }else{
            result = 0.0;
        }
        return result;
    }
    
    private static String readAll(Reader rd) throws IOException {
    	log.info("readAll: reader={}", rd);
    	
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
    	log.info("readJsonFromUrl: url={}", url);
    	
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            return json;
        } finally {
            is.close();
        }
    }
    
    
}
