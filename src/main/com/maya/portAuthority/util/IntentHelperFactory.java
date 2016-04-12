package com.maya.portAuthority.util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.slu.Intent;

public class IntentHelperFactory {
	private static  Logger log = LoggerFactory.getLogger(IntentHelperFactory.class);

	
	public static IntentHelper getIntentHelper(Intent intent){
		
		if (intent.getName().equals(BusStopIntentHelper.INTENT_NAME)) {
			log.info("Building BusStopIntentHelper");
			return new BusStopIntentHelper(intent);
			
		} else if (intent.getName().equals(DirectionIntentHelper.INTENT_NAME)) {
			log.info("Building DirectionIntentHelper");
			return new DirectionIntentHelper(intent);
			
		} else if (intent.getName().equals(RouteIntentHelper.INTENT_NAME)) {
			log.info("Building RouteIntentHelper");
			return new RouteIntentHelper(intent);
			
		}
		log.info("Building OneShotIntentHelper");
		return new OneShotIntentHelper(intent);
	}
}

