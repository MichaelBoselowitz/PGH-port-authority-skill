
package com.maya.portAuthority.util;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @deprecated
 * @author brown@maya.com
 *
 */
public class BusStopIntentHelper implements IntentHelper {
	public static final String INTENT_NAME="StationBusIntent";
	public static final String SLOT_NAME = "StationName";
	public static final String SESSION_NAME = "StationName";
	
	private static  Logger log = LoggerFactory.getLogger(BusStopIntentHelper.class);

	private static  String SESSION_STATION_ID= "StationID";

	private Intent intent;

	public BusStopIntentHelper(Intent i){
		this.intent=i;
	}

	//throws null pointer exception if slot is empty
	public void putValuesInSession(Session session) throws Exception{
		log.trace("putValuesInSession" );
		//user supplied station
		String stationName=getValueFromIntentSlot(SLOT_NAME);

		//station=new BusStation(stationName);
		log.debug("putting value in session Slot station:"+stationName);
		session.setAttribute(SESSION_NAME, stationName.toUpperCase()); 
		//session.setAttribute(SESSION_STATION, station.name.toUpperCase()); 
		//session.setAttribute(SESSION_STATION_ID, station.ID);	
	} 

	@Override
	public String getValueFromIntentSlot(String slotName){
		Slot slot = intent.getSlot(slotName);
		return (slot!=null) ? slot.getValue() : null;
	}

}
