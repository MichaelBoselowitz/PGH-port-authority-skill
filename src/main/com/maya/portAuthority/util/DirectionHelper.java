package com.maya.portAuthority.util;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.Session;
import com.maya.portAuthority.InvalidInputException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author brown
 *
 */
public class DirectionHelper extends DataHelper {
	public static final String INTENT_NAME="DirectionBusIntent";
	public static final String NAME = "Direction";
	public static final String SPEECH ="Which direction are you <w role=\"ivona:NN\">traveling</w>, inbound or outbound ?";
	
	private static  Logger log = LoggerFactory.getLogger(DirectionHelper.class);

		//private Session session;
	//private Intent intent;

	public DirectionHelper(){
		log.info("constructor");
		//this.session=s;
	}

	public String putValuesInSession(Session session,Intent intent) throws InvalidInputException{
		log.info("putValuesInSession"+intent.getName());

		String direction=getValueFromIntentSlot(intent);
		if (direction!=null){
			session.setAttribute(NAME, direction.toUpperCase());
		} else {
			log.error("stationName is null");
			throw new InvalidInputException("StationName is null", "I didn't hear that."+SPEECH);
		}
		return "";
	} 

	@Override
	public String getValueFromIntentSlot(Intent i){
		log.info("getValuesFromIntentSlot:"+i.getName());
		Slot slot = i.getSlot(NAME);
		return (slot!=null) ? slot.getValue() : null;
	}
	
	public String getValueFromSession(Session session){
		log.info("getValuesFromSession");
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
