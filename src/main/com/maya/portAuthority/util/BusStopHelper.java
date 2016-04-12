
package com.maya.portAuthority.util;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.Session;
import com.maya.portAuthority.api.Message;
import com.maya.portAuthority.api.TrueTimeMessageParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
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

	//private Intent intent;
	private Session session;

	public BusStopHelper(Session s){
		this.session=s;
	}

	public void putValuesInSession(Intent intent){
		log.trace("putValuesInSession" );
		String stationName=getValueFromIntentSlot(intent);
		if (stationName!=null){
			log.debug("putting value in session Slot station:"+stationName);
			session.setAttribute(NAME, stationName.toUpperCase()); 
		} else {
			log.debug("stationName is null");
		}
	} 
	
	public String getValueFromIntentSlot(Intent i){
		Slot slot = i.getSlot(NAME);
		return (slot!=null) ? slot.getValue() : null;
	}
	
	public String getValueFromSession(){
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
