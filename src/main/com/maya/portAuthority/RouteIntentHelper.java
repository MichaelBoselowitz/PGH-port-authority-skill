package com.maya.portAuthority;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RouteIntentHelper implements IntentHelper {
	public static final String INTENT_NAME="RouteBusIntent";
	public static final String SLOT_NAME = "Route";
	public static final String SESSION_NAME = "Route";
	
	private static Logger log = LoggerFactory.getLogger(RouteIntentHelper.class);
	
	private Intent intent;

	public RouteIntentHelper(Intent i){
		this.intent=i;
	}

	//throws null pointer exception if slot is empty
	public void putValuesInSession(Session session) throws Exception{
		log.trace("putValuesInSession");
		//user supplied Route
		String route=getValueFromIntentSlot(SLOT_NAME);

		//log.debug("putting value in session Slot Route:"+Route.toUpperCase());
		route=route.replaceAll("\\s+","");
		session.setAttribute(SESSION_NAME, route.toUpperCase()); 
	} 

	@Override
	public String getValueFromIntentSlot(String slotName){
		Slot slot = intent.getSlot(slotName);
		return (slot!=null) ? slot.getValue() : null;
	}

}
