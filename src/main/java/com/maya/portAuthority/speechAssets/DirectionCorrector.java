
package com.maya.portAuthority.speechAssets;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Adithya
 */
public class DirectionCorrector {

    private static final Map<String, String> expectedInputs = new HashMap<>(200);

    static {
        expectedInputs.put("INBOUND", "INBOUND");
        expectedInputs.put("INBONE", "INBOUND");
        expectedInputs.put("INBALL", "INBOUND");
        expectedInputs.put("OUTBOUND", "OUTBOUND");
        expectedInputs.put("ALBUM", "OUTBOUND");
        expectedInputs.put("ALBON", "OUTBOUND");
        expectedInputs.put("TOWARDS", "INBOUND");
        expectedInputs.put("AWAY", "OUTBOUND");
    }

    public static String getDirection(String inputDirection) {
        if (expectedInputs.get(inputDirection) != null) {
            return expectedInputs.get(inputDirection);
        } else {
            return "Cannot understand the direction " + inputDirection + ". Please try again.";
        }
    }
}
