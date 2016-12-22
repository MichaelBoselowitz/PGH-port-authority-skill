package com.maya.portAuthority.util;

import com.amazon.speech.speechlet.Session;
import com.maya.portAuthority.InvalidInputException;
import com.amazon.speech.slu.Intent;
/**
 * @author brown
 *
 */
public abstract class DataHelper {	
	public static String SPEECH;

	public abstract String putValuesInSession(Session s, Intent intent) throws InvalidInputException;
	
	public abstract String getValueFromIntentSlot(Intent intent);
	
	public abstract String getValueFromSession(Session s);

	public abstract String getName();
	
	public abstract String getIntentName();
	
	public abstract String getSpeech();
}
