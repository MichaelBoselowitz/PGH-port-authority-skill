package com.maya.portAuthority.util;

import com.amazon.speech.speechlet.Session;
import com.amazon.speech.slu.Intent;
/**
 * @author brown
 *
 */
public abstract class DataHelper {	
	public static String SPEECH;

	public abstract void putValuesInSession(Intent intent);
	
	public abstract String getValueFromIntentSlot(Intent intent);
	
	public abstract String getValueFromSession();

	public abstract String getName();
	
	public abstract String getIntentName();
	
	public abstract String getSpeech();
}
