package com.maya.portAuthority.util;

import com.amazon.speech.speechlet.Session;
/**
 * @deprecated
 * @author brown
 *
 */
public interface IntentHelper {	
	public void putValuesInSession(Session session)throws Exception;
	
	public String getValueFromIntentSlot(String slotName);
}
