package com.maya.portAuthority;

import java.util.HashMap;

public class BusLine {
	/**STATIC************/
	private static final HashMap<String, String> LINES = new HashMap<String,String>();
	static {
		LINES.put("p one", "East Busway");
		LINES.put("p two", "East Busway Short");
		LINES.put("p three", "East Busway Oakland");
		//LINES.put("smithfield", 7280);
	}

	public static BusLine getBusLine(String name) throws Exception {
		return new BusLine (name);

	}
	
	/**INSTANCE**************************************/
	
	
	/**route Child element of the root element. Encapsulates a route serviced by
	the system.
	**/
	
	/**
	 * API Requires this field 
	 * rt- Child element of the route element. Alphanumeric designator of a route (ex. “20” or “X20”).
	**/
	public final String rt;
	
	
	/**rtnm Child element of the route element. Common name of the route
	(ex. “Madison” for the 20 route).**/
	public final String rtnm;
	
	/**rtclr Child element of the route element. Color of the route line used in
	map (ex. "#ffffff")**/
	
	/**rtdd 
	Child element of the route element. Language-specific route
designator meant for display.**/
	
	public BusLine (String name) throws Exception {
		//name=name.toUpperCase();
		if (LINES.containsKey(name)) {
			this.rt=name;
			this.rtnm= LINES.get(name);
		} else {
			throw new Exception (name);
		}
		
	}
	
}

