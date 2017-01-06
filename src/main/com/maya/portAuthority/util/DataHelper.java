package com.maya.portAuthority.util;

import com.amazon.speech.speechlet.Session;
import com.maya.portAuthority.InvalidInputException;
import com.maya.portAuthority.api.Message;
import com.maya.portAuthority.api.TrueTimeAPI;
import com.maya.portAuthority.googleMaps.NearestStopLocator;
import com.maya.portAuthority.storage.PaInputData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
/**
 * @author brown
 *
 */
public class DataHelper {	
	private static Logger log = LoggerFactory.getLogger(DataHelper.class);
	
	public static final String SESSION_OBJECT_NAME="InputData";
	
	//INTENTS
	public static final String ONE_SHOT_INTENT_NAME="OneshotBusIntent";
	public static final String RESET_INTENT_NAME="ResetBusIntent";
	public static final String ALL_ROUTES_INTENT_NAME="AllBusRoutesIntent";
	public static final String ROUTE_INTENT_NAME="RouteBusIntent";
	public static final String LOCATION_INTENT_NAME="LocationBusIntent";
	public static final String DIRECTION_INTENT_NAME="DirectionBusIntent";
	
	//SLOTS
	public static final String ROUTE_NAME = "Route_Name";
	public static final String ROUTE_ID = "Route";
	//public static final String NAME = "Route";
	public static final String LOCATION = "Location";
	public static final String LAT = "lat";
	public static final String LONG = "long";
	public static final String ADDRESS = "address";
	public static final String DIRECTION = "Direction";
	
	//Voice
	public static String SPEECH_WELCOME = "Welcome to Pittsburgh Port Authority ";
	public static final String ROUTE_PROMPT ="Which bus line would you like arrival information for?";
	public static final String LOCATION_PROMPT="Where are you now?";
	public static final String DIRECTION_PROMPT ="In which direction are you <w role=\"ivona:NN\">traveling</w>?";
	
	//private ArrayList<String> validIntents = null;
	
	public static ArrayList<String> getValidIntents(){
		//		if (validIntents!=null){
		//			return validIntents;
		//		} else {
		ArrayList<String> validIntents  = new ArrayList<String>();
		validIntents.add(ONE_SHOT_INTENT_NAME);
		validIntents.add(RESET_INTENT_NAME);
		validIntents.add(ROUTE_INTENT_NAME);
		validIntents.add(LOCATION_INTENT_NAME);
		validIntents.add(DIRECTION_INTENT_NAME);
		return validIntents;
	}
	
	public static boolean isValidIntent(String intentName){
		return (getValidIntents().contains(intentName));
	}
	
	public static String getValueFromIntentSlot(Intent intent, String name) {
		log.trace("getValueFromIntentSlot"+intent.getName());
		Slot slot = intent.getSlot(name);
		return (slot!=null) ? slot.getValue() : null;
	}
	
	public static String getValueFromSession(Session session, String name){
		log.trace("getValuesFromSession");
		if (session.getAttributes().containsKey(name)) {
			return (String) session.getAttribute(name);
		} else {
			return null;
		}
	}
	
	
	public static String putDirectionValuesInSession(Session session,Intent intent) throws InvalidInputException{
		log.trace("putDirectionValuesInSession"+intent.getName());

		String direction=getValueFromIntentSlot(intent, DIRECTION);
		if (direction == null){
			if (intent.getName().equals(ONE_SHOT_INTENT_NAME)) {
				//For OneShotBusIntent, this is an acceptable condition. 
				log.info("Intent:"+intent.getName()+" direction is null");
				return "";
			} else{
				log.info("Intent:"+intent.getName()+" direction is null");
				throw new InvalidInputException("No Direction in Intent", "Please repeat your direction. " + DIRECTION_PROMPT);
			}
		}		
		session.setAttribute(DIRECTION, translateDirection(direction));

		return "";
	} 
	
	private static String translateDirection(String spoken){
		if (spoken.equalsIgnoreCase("away")){
			return "OUTBOUND";
		} else if (spoken.equalsIgnoreCase("towards")){
			return "INBOUND";
		} else if (spoken.equalsIgnoreCase("southbound")){
			return "OUTBOUND";
		} else {
			return spoken.toUpperCase();
		}
	}
	
	/**
	 * The location held in the intent's slot might contain an address or a landmark or business name. 
	 * Here we call the Google Maps API to translate that to a street address and put it in session. 
	 */
	public static String putLocationValuesInSession(Session session, Intent intent) throws InvalidInputException {
		log.trace("putLocationValuesInSession" + intent.getName());
		String location = getValueFromIntentSlot(intent, LOCATION);
		
		//Handle Null Location
		if (location == null){
			if (intent.getName().equals(ONE_SHOT_INTENT_NAME)) {
				//For OneShotBusIntent, this is an acceptable condition. 
				log.info("Intent:"+intent.getName()+" location is null");
				return "";
			} else{
				log.info("Intent:"+intent.getName()+" location is null");
				throw new InvalidInputException("No Location in Intent", "Please repeat your location. " + LOCATION_PROMPT);
			}
		}
		
		//Find address for location 
		try {
			log.debug("putting value in session Slot Location:" + location);
			session.setAttribute(LOCATION, location.toUpperCase());
			Coordinates c = NearestStopLocator.getSourceLocation(location);
			String streetAddress = simplifyAddress(c.getAddress());
			session.setAttribute(LAT, c.getLat());
			session.setAttribute(LONG, c.getLng());
			session.setAttribute(ADDRESS, streetAddress);
			if (!location.equalsIgnoreCase(streetAddress)){
				return "I found "+location+" at "+streetAddress+".";
			} else {
				return "";
			}

		} catch (JSONException jsonE) {
			throw new InvalidInputException("No Location in Intent", jsonE, "Please repeat your location. " + LOCATION_PROMPT);
		} catch (IOException ioE) {
			throw new InvalidInputException("Cannot reach Google Maps ", ioE, "Please repeat your location. " + LOCATION_PROMPT);
		}
	}
	
	public static String simplifyAddress(String address){
		String[] addressLines = address.split(",");
		return addressLines[0];
	}
	
	///Route
	public static String putRouteValuesInSession(Session session, Intent intent) throws InvalidInputException {
		log.trace("putRouteValuesInSession" + intent.getName());

		String routeID = getValueFromIntentSlot(intent,ROUTE_ID);
		
		//Handle Null routeID
		if (routeID == null){
			if (intent.getName().equals(ONE_SHOT_INTENT_NAME)) {
				//For OneShotBusIntent, this is an acceptable condition. 
				log.info("Intent:"+intent.getName()+" routeID is null");
				return "";
			} else{
				log.info("Intent:"+intent.getName()+" routeID is null");
				throw new InvalidInputException("No routeID in Intent", "Please repeat your bus line. " + ROUTE_PROMPT);
			}
		}

		routeID = routeID.replaceAll("\\s+", "");
		Message route = getMatchedRoute(routeID.toUpperCase());

		if (route == null) {
			log.error("putValuesInSession:" + intent.getName() + " route is null");
			throw new InvalidInputException("Route does not match API",
					"Could not find the bus line " + routeID + "." + ROUTE_PROMPT);
		}

		session.setAttribute(ROUTE_ID, route.getRouteID());
		session.setAttribute(ROUTE_NAME, route.getRouteName());
		return route.getRouteID() + "," + route.getRouteName();

	}
	
	private static Message getMatchedRoute(String routeID){
		List<Message> routes = TrueTimeAPI.getRoutes();
		Iterator<Message> iterator = routes.iterator();
		while (iterator.hasNext()){
			Message element=(Message)iterator.next();
			if (element.getMessageType().equalsIgnoreCase("error")){
				return null;
			}
			if (element.getMessageType().equalsIgnoreCase("route")){		
				if (routeID.equalsIgnoreCase(element.getRouteID())){
					return element;
				}
			}
		}
		return null;
	}
	
	
}
