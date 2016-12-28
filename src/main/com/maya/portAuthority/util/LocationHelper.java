package com.maya.portAuthority.util;


import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.Session;
import com.maya.portAuthority.InvalidInputException;
import com.maya.portAuthority.util.Coordinates;
import com.maya.portAuthority.googleMaps.NearestStopLocator;

import java.io.IOException;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author brown@maya.com
 *
 */
public class LocationHelper extends DataHelper {
	public static final String INTENT_NAME="LocationBusIntent";
	public static final String NAME = "Location";
	public static final String SPEECH="Where are you now?";
	
	private static  Logger log = LoggerFactory.getLogger(LocationHelper.class);


	//private Intent intent;
	//private Session session;

	public LocationHelper(){
		log.trace("constructor");
		//this.session=s;
	}

	/**
	 * The location held in the intent's slot might contain an address or a landmark or business name. 
	 * Here we call the Google Maps API to translate that to a street address and put it in session. 
	 */
	public String putValuesInSession(Session session, Intent intent) throws InvalidInputException {
		log.trace("putValuesInSession" + intent.getName());
		String location = getValueFromIntentSlot(intent);
		
		//Handle Null Location
		if (location == null){
			if (intent.getName().equals("OneShotBusIntent")) {
				//For OneShotBusIntent, this is an acceptable condition. 
				log.info("Intent:"+intent.getName()+" location is null");
				return "";
			} else{
				log.info("Intent:"+intent.getName()+" location is null");
				throw new InvalidInputException("No Location in Intent", "Please repeat your location. " + SPEECH);
			}
		}
		
		//Find address for location 
		try {
			log.debug("putting value in session Slot Location:" + location);
			session.setAttribute(NAME, location.toUpperCase());
			Coordinates c = NearestStopLocator.getSourceLocation(location);
			String streetAddress = simplifyAddress(c.getAddress());
			session.setAttribute("lat", c.getLat());
			session.setAttribute("long", c.getLng());
			session.setAttribute("address", streetAddress);
			if (!location.equalsIgnoreCase(streetAddress)){
				return "I found "+location+" at "+streetAddress+".";
			} else {
				return "";
			}

		} catch (JSONException jsonE) {
			throw new InvalidInputException("No Location in Intent", jsonE, "Please repeat your location. " + SPEECH);
		} catch (IOException ioE) {
			throw new InvalidInputException("Cannot reach Google Maps ", ioE, "Please repeat your location. " + SPEECH);
		}
	}
	
	public static String simplifyAddress(String address){
		String[] addressLines = address.split(",");
		return addressLines[0];
	}
	
	public String getValueFromIntentSlot(Intent i){
		log.info("getValueFromIntentSlot:"+i.getName());
		Slot slot = i.getSlot(NAME);
		return (slot!=null) ? slot.getValue() : null;
	}
	
	public String getValueFromSession(Session session){
		log.info("getValueFromSession");
		if (session.getAttributes().containsKey(NAME)) {
			return (String) session.getAttribute(NAME);
		} else {
			return null;
		}
	}

	public String getName(){
		return NAME;
	}
	
	public  String getIntentName(){
		return INTENT_NAME;
	}
	
	public  String getSpeech(){
		return SPEECH;
	}
}
