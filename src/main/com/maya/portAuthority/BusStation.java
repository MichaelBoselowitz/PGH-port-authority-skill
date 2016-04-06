package com.maya.portAuthority;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BusStation {
	/**STATIC************/

	private static  Logger log = LoggerFactory.getLogger(BusStation.class);


	public static BusStation getBusStation(String name) throws Exception {
		log.info("getbusStation("+name+")");
		return new BusStation (name);
	}

	
	/**INSTANCE**************************************/
	public final String name;
	public final String ID;
		
	public BusStation (String name) {// throws Exception {
		//name=name.toUpperCase();
//		if (STATIONS.containsKey(name)) {
//			this.name=name;
//			this.ID= STATIONS.get(name);
//		} else {
//			throw new Exception (name);
//		}
		this.name="smithfield";
		ID=""; //or 4833
	}
	
	
	
}
