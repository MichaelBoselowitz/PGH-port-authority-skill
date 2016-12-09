package com.maya.portAuthority.storage;



/**
 * 
 */
public class PaInputData {
    private String location;
    private String busstop;
    private String route;
    private String direction;

    public PaInputData() {
        // public no-arg constructor required for DynamoDBMapper marshalling
    }

    /**
     * Creates a new instance of {@link PaInputData} with initialized but empty player and
     * score information.
     * 
     * @return
     */
    public static PaInputData newInstance() {
        PaInputData newInstance = new PaInputData();
        //newInstance.setPlayers(new ArrayList<String>());
        //newInstance.setScores(new HashMap<String, Long>());
        return newInstance;
    }

    public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getBusstop() {
		return busstop;
	}

	public void setBusstop(String busstop) {
		this.busstop = busstop;
	}

	public String getRoute() {
		return route;
	}

	public void setRoute(String route) {
		this.route = route;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public String toString() {
		return "PaInputData [location=" + location + ", busstop=" + busstop + ", route=" + route + ", direction="
				+ direction + "]";
	}
}
