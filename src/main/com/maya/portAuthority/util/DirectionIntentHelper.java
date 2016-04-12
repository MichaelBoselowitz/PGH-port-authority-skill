package com.maya.portAuthority.util;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @deprecated
 * @author brown
 *
 */
public class DirectionIntentHelper implements IntentHelper {
	public static final String INTENT_NAME="DirectionBusIntent";
	public static final String SLOT_NAME = "Direction";
	public static final String SESSION_NAME = "Direction";
	
	private static  Logger log = LoggerFactory.getLogger(DirectionIntentHelper.class);

	private Intent intent;

	public DirectionIntentHelper(Intent i){
		this.intent=i;
	}

	//throws null pointer exception if slot is empty
	public void putValuesInSession(Session session) throws Exception{
		log.trace("putValuesInSession");
		//user supplied direction
		String direction=getValueFromIntentSlot(SLOT_NAME);

		//log.debug("putting value in session Slot direction:"+direction.toUpperCase());
		session.setAttribute(SESSION_NAME, direction.toUpperCase()); 
	} 

	@Override
	public String getValueFromIntentSlot(String slotName){
		Slot slot = intent.getSlot(slotName);
		return (slot!=null) ? slot.getValue() : null;
	}

}
