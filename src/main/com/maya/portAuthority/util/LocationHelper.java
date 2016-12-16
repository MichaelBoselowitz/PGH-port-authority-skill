package com.maya.portAuthority.util;


import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.Session;

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
	private Session session;

	public LocationHelper(Session s){
		log.info("constructor");
		this.session=s;
	}

	public void putValuesInSession(Intent intent){
		log.info("putValuesInSession"+intent.getName());
		String location=getValueFromIntentSlot(intent);
		if (location!=null){
			log.debug("putting value in session Slot Location:"+location);
			session.setAttribute(NAME, location.toUpperCase()); 
		} else {
			log.error("location is null");
		}
	} 
	
	public String getValueFromIntentSlot(Intent i){
		log.info("getValueFromIntentSlot:"+i.getName());
		Slot slot = i.getSlot(NAME);
		return (slot!=null) ? slot.getValue() : null;
	}
	
	public String getValueFromSession(){
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
