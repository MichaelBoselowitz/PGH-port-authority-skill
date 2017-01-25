/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.maya.portAuthority.googleMaps;

import com.maya.portAuthority.googleMaps.model.Legs;
import com.maya.portAuthority.googleMaps.model.Path;
import com.maya.portAuthority.googleMaps.model.Point;
import com.maya.portAuthority.googleMaps.model.Direction;
import com.maya.portAuthority.util.Location;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Adithya
 */
public class Instructions {
    //Example: SDLC Partners to Highmark
    //Good Example: https://maps.googleapis.com/maps/api/directions/json?origin=40.4413962,-80.0035603&destination=40.4332551,-79.9257867&mode=walk&transit_mode=walking&key=AIzaSyBzW19DGDOi_20t46SazRquCLw9UNp_C8s
    
<<<<<<< HEAD
   //String tag = "Instructions";
=======
   String tag = "Instructions";
>>>>>>> origin/master
   private final static Logger LOGGER = LoggerFactory.getLogger("Instructions");


	/**
	 * Receives a JSONObject and returns a Direction
	 * 
	 * @param jsonObject
	 * @return The Direction retrieved by the JSON Object
	 */
<<<<<<< HEAD
	public static List<Direction> parse(JSONObject jsonObject) throws Exception{
=======
	public List<Direction> parse(JSONObject jsonObject) throws Exception{
>>>>>>> origin/master
		List<Direction> directionsList = null; //returned direction
		Direction currentGDirection = null; //current direction
		List<Legs> legsList = null;	//legs
		Legs currentLeg = null; //current leg
		List<Path> pathsList = null;//paths
		Path currentPath = null;//current path
                
                //JSON parts:
		JSONArray routes = null;
		JSONObject route = null;
		JSONObject bound = null;
		JSONArray legs = null;
		JSONObject leg = null;
		JSONArray steps = null;
		JSONObject step = null;
		String polyline = "";
                
                
                
		try {
			routes = jsonObject.getJSONArray("routes");
<<<<<<< HEAD
			LOGGER.info("routes found : " + routes.length());
=======
			LOGGER.info(tag, "routes found : " + routes.length());
>>>>>>> origin/master
			directionsList = new ArrayList<Direction>();
			//traverse routes
			for (int i = 0; i < routes.length(); i++) {
				route=(JSONObject) routes.get(i);
				legs = route.getJSONArray("legs");
<<<<<<< HEAD
				LOGGER.info("route[" + i + "]contains " + legs.length() + " legs");
=======
				LOGGER.info(tag, "route[" + i + "]contains " + legs.length() + " legs");
>>>>>>> origin/master
				//traverse legs
				legsList = new ArrayList<Legs>();
				for (int j = 0; j < legs.length(); j++) {
					leg=(JSONObject) legs.get(j);
					steps = leg.getJSONArray("steps");
<<<<<<< HEAD
					LOGGER.info( "route[" + i + "]:leg[" + j + "] contains "+ steps.length() + " steps" );
=======
					LOGGER.info(tag, "route[" + i + "]:leg[" + j + "] contains "+ steps.length() + " steps" );
>>>>>>> origin/master
					//traverse all steps
					pathsList = new ArrayList<Path>();
					for (int k = 0; k < steps.length(); k++) {
						step = (JSONObject) steps.get(k);
						polyline = (String) ((JSONObject) (step).get("polyline")).get("points");
						// Build the List of GDPoint that define the path
						List<Point> list = decodePoly(polyline);
						// Create the GDPath
						currentPath = new Path(list);
						currentPath.setDistance(((JSONObject)step.get("distance")).getInt("value"));
						currentPath.setDuration(((JSONObject)step.get("duration")).getInt("value"));
						currentPath.setHtmlText(step.getString("html_instructions"));
						currentPath.setTravelMode(step.getString("travel_mode"));
<<<<<<< HEAD
						LOGGER.info("routes[" + i + "]:legs[" + j + "]:Step[" + k + "] contains " + list.size() + " points");
=======
						LOGGER.info(tag, "routes[" + i + "]:legs[" + j + "]:Step[" + k + "] contains " + list.size() + " points");
>>>>>>> origin/master
						// Add it to the list of Path of the Direction
						pathsList.add(currentPath);
					}
					// 
					currentLeg = new Legs(pathsList);
					currentLeg.setDistance(((JSONObject)leg.get("distance")).getInt("value"));
					currentLeg.setmDuration(((JSONObject)leg.get("duration")).getInt("value"));
					currentLeg.setEndAddr(leg.getString("end_address"));
					currentLeg.setStartAddr(leg.getString("start_address"));
					legsList.add(currentLeg);
					
<<<<<<< HEAD
					LOGGER.info("Added a new Path and paths size is : " + pathsList.size());
=======
					LOGGER.info(tag, "Added a new Path and paths size is : " + pathsList.size());
>>>>>>> origin/master
				}
				// Build the GDirection using the paths found
				currentGDirection = new Direction(legsList);
				bound=(JSONObject)route.get("bounds");
				currentGDirection.setNorthEastBound(new Location(
						((JSONObject)bound.get("northeast")).getDouble("lat"),
						((JSONObject)bound.get("northeast")).getDouble("lng")));
				currentGDirection.setmSouthWestBound(new Location(
						((JSONObject)bound.get("southwest")).getDouble("lat"),
						((JSONObject)bound.get("southwest")).getDouble("lng")));
				currentGDirection.setCopyrights(route.getString("copyrights"));
				directionsList.add(currentGDirection);
			}

		} catch (JSONException e) {
<<<<<<< HEAD
			LOGGER.error("Parsing JSon from GoogleDirection Api failed, see stack trace below:", e);
                        throw new Exception("Parsing JSon from GoogleDirection Api failed");
		} catch (Exception e) {
			LOGGER.error("Parsing JSon from GoogleDirection Api failed, see stack trace below:", e);
=======
			LOGGER.error(tag, "Parsing JSon from GoogleDirection Api failed, see stack trace below:", e);
                        throw new Exception("Parsing JSon from GoogleDirection Api failed");
		} catch (Exception e) {
			LOGGER.error(tag, "Parsing JSon from GoogleDirection Api failed, see stack trace below:", e);
>>>>>>> origin/master
                        throw new Exception("Parsing JSon from GoogleDirection Api failed");
		}
		return directionsList;
	}
        
<<<<<<< HEAD
        public static String getInstructions(JSONObject obj) throws Exception{
=======
        public String getInstructions(JSONObject obj) throws Exception{
>>>>>>> origin/master
            List<Direction> listOfDirections = new ArrayList<>();
                listOfDirections = parse(obj);
                StringBuilder sb = new StringBuilder();
                for(Direction dir : listOfDirections){
                    sb.append(dir.toString());
                }
                return sb.toString();
        }

	/**
	 * Method to decode polyline points
	 * Source: http://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
	 */
<<<<<<< HEAD
	private static List<Point> decodePoly(String encoded) {
=======
	private List<Point> decodePoly(String encoded) {
>>>>>>> origin/master

		List<Point> poly = new ArrayList<Point>();
		int index = 0, len = encoded.length();
		int lat = 0, lng = 0;

		while (index < len) {
			int b, shift = 0, result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lat += dlat;

			shift = 0;
			result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lng += dlng;
			poly.add(new Point((double) lat / 1E5, (double) lng / 1E5));
		}

		return poly;
	}
        //test
        public static void main(String[] args) throws Exception{
<<<<<<< HEAD
           // Instructions instructions = new Instructions();
=======
            Instructions instructions = new Instructions();
>>>>>>> origin/master
            String jsonData = "";
		BufferedReader br = null;
		try {
			String line;
                        //To test: Store JSON by striking
                        //https://maps.googleapis.com/maps/api/directions/json?origin=40.4413962,-80.0035603&destination=40.4332551,-79.9257867&mode=walk&transit_mode=walking&key=AIzaSyBzW19DGDOi_20t46SazRquCLw9UNp_C8s
                        //in a file, and provide file location below:
			br = new BufferedReader(new FileReader("C:\\Users\\Adithya\\Desktop\\testjson.txt"));
			while ((line = br.readLine()) != null) {
				jsonData += line + "\n";
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		// System.out.println("File Content: \n" + jsonData);
		JSONObject obj = new JSONObject(jsonData);
<<<<<<< HEAD
                System.out.println(Instructions.getInstructions(obj));
=======
                System.out.println(instructions.getInstructions(obj));
>>>>>>> origin/master
        }
}
