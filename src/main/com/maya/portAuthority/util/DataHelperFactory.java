package com.maya.portAuthority.util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.Session;

public class DataHelperFactory {
	private static  Logger log = LoggerFactory.getLogger(DataHelperFactory.class);

	
	public static DataHelper getHelper(Session session, String dataType){
		
		if (dataType.equals(BusStopHelper.NAME)) {
			log.info("Building BusStopHelper");
			return new BusStopHelper(session);
			
		} else if (dataType.equals(DirectionHelper.NAME)) {
			log.info("Building DirectionHelper");
			return new DirectionHelper(session);
			
		} else if (dataType.equals(RouteHelper.NAME)) {
			log.info("Building RouteHelper");
			return new RouteHelper(session);
			
		}
		return new RouteHelper(session);
	}
}