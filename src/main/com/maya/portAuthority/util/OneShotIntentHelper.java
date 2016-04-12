package com.maya.portAuthority.util;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OneShotIntentHelper implements IntentHelper {
	public static final String INTENT_NAME="OneShotBusIntent";
	
	private static Logger log = LoggerFactory.getLogger(RouteIntentHelper.class);
	
	private Intent intent;

	public OneShotIntentHelper(Intent i){
		this.intent=i;
	}

	//throws null pointer exception if slot is empty
	public void putValuesInSession(Session session) throws Exception{
		log.trace("putValuesInSession");
		//user supplied Route
		String route=getValueFromIntentSlot(RouteIntentHelper.SLOT_NAME);
		String direction=getValueFromIntentSlot(DirectionIntentHelper.SLOT_NAME);
		String busStop=getValueFromIntentSlot(BusStopIntentHelper.SLOT_NAME);
		
		session.setAttribute(RouteIntentHelper.SESSION_NAME, route.toUpperCase()); 
		session.setAttribute(DirectionIntentHelper.SESSION_NAME, direction.toUpperCase()); 
		session.setAttribute(BusStopIntentHelper.SESSION_NAME, busStop.toUpperCase()); 
	} 

	@Override
	public String getValueFromIntentSlot(String slotName){
		Slot slot = intent.getSlot(slotName);
		return (slot!=null) ? slot.getValue() : null;
	}

}
