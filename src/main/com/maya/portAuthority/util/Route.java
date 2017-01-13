package com.maya.portAuthority.util;

public class Route {
	/**
	 * rt- Alphanumeric designator of a route (ex. "20" or "X20").
	 */
	private String id;
	/**
	 * rtnm- Common name of the route (ex. "Madison" for the 20 route).
	 */
	private String name;

	/**
	 * rtclr -Color of the route line used in map (ex. "#ffffff")
	 */
	private String color;

	/**
	 * rtdd- Language-specific route designator meant for display.
	 */
	private String designator;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getDesignator() {
		return designator;
	}

	public void setDesignator(String designator) {
		this.designator = designator;
	}

	@Override
	public String toString() {
		return "Route [id=" + id + ", name=" + name + "]";
	}

}
