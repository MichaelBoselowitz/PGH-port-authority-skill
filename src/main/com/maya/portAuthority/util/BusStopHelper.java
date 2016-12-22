
package com.maya.portAuthority.util;


import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.Session;
import com.maya.portAuthority.InvalidInputException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author brown@maya.com
 *
 */
public class BusStopHelper extends DataHelper {
	public static final String INTENT_NAME="StationBusIntent";
	public static final String NAME = "StationName";
	public static final String SPEECH="Where do you get on the bus?";
	
	private static  Logger log = LoggerFactory.getLogger(BusStopHelper.class);

	private static  String SESSION_STATION_ID= "StationID";


	public BusStopHelper(){
		log.info("constructor");
	}

	/**
	 * @return Feedback Text for user. 
	 */
	public String putValuesInSession(Session session,Intent intent) throws InvalidInputException{
		log.trace("putValuesInSession"+intent.getName() );
		String stationName=getValueFromIntentSlot(intent);
		if (stationName!=null){
			log.debug("putting value in session Slot station:"+stationName);
			session.setAttribute(NAME, stationName.toUpperCase()); 
		} else {
			log.error("stationName is null");
			throw new InvalidInputException("StationName is null", "I didn't hear that."+SPEECH);
		}
		return "";
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
