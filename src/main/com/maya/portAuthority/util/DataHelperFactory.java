package com.maya.portAuthority.util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.Session;

public class DataHelperFactory {
	private static  Logger log = LoggerFactory.getLogger(DataHelperFactory.class);

	
	public static DataHelper getHelper(String dataType){
		log.trace("getHelper:"+dataType);
		if (dataType.equals(BusStopHelper.NAME)) {
			log.debug("Building BusStopHelper");
			return new BusStopHelper();
			
		} else if (dataType.equals(DirectionHelper.NAME)) {
			log.debug("Building DirectionHelper");
			return new DirectionHelper();
			
		} else if (dataType.equals(RouteHelper.NAME)) {
			log.debug("Building RouteHelper");
			return new RouteHelper();
			
		} else if (dataType.equals(LocationHelper.NAME)) {
			log.debug("Building LocationHelper");
			return new LocationHelper();
		} 
		return null;
	}
}