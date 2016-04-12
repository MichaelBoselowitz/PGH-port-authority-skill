package com.maya.portAuthority.util;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author brown
 *
 */
public class DirectionHelper extends DataHelper {
	public static final String INTENT_NAME="DirectionBusIntent";
	public static final String NAME = "Direction";
	public static final String SPEECH ="Which direction are you travelling?";
	
	private static  Logger log = LoggerFactory.getLogger(DirectionHelper.class);

		private Session session;
	//private Intent intent;

	public DirectionHelper(Session s){
		this.session=s;
	}

	public void putValuesInSession(Intent intent){
		log.trace("putValuesInSession");

		String direction=getValueFromIntentSlot(intent);
		if (direction!=null){
			session.setAttribute(NAME, direction.toUpperCase()); 
		} else {
			//log
		}
	} 

	@Override
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
