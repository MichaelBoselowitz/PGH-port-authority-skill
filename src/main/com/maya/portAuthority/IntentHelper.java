package com.maya.portAuthority;

import com.amazon.speech.speechlet.Session;

public interface IntentHelper {	
	public void putValuesInSession(Session session)throws Exception;
	
	public String getValueFromIntentSlot(String slotName);
}
