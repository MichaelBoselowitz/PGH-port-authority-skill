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
public class RouteHelper extends DataHelper {
	public static final String INTENT_NAME="RouteBusIntent";
	public static final String NAME = "Route";
	public static final String SPEECH ="Which bus line would you like arrival information for?";

	private static Logger log = LoggerFactory.getLogger(RouteHelper.class);
	
	//private Intent intent;
	private Session session;

	public RouteHelper(Session s){
		log.trace("constructor");
		this.session=s;
	}

	public void putValuesInSession(Intent intent){
		log.trace("putValuesInSession"+intent.getName());

		String route=getValueFromIntentSlot(intent);

		if (route!=null){
			route=route.replaceAll("\\s+","");
			session.setAttribute(NAME, route.toUpperCase()); 
		} else {
			log.error("putValuesInSession:"+intent.getName()+" route is null");
		}
	} 

	public String getValueFromIntentSlot(Intent intent){
		log.trace("getValuesInSession"+intent.getName());
		Slot slot = intent.getSlot(NAME);
		return (slot!=null) ? slot.getValue() : null;
	}
	
	public String getValueFromSession(){
		log.trace("getValueFromSession");
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

