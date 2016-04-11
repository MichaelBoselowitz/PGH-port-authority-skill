package com.maya.portAuthority;

import java.util.List;

import junit.framework.TestCase;

import com.maya.portAuthority.api.Message;
import com.maya.portAuthority.api.TrueTimeMessageParser;

/**
 * 
 */

/**
 * @author brown
 *
 */
public class APITest extends TestCase {
	private static final String API_URL="http://truetime.portauthority.org/bustime/api/v1/getpredictions";
	private static final String ACCESS_ID="cvTWAYXjbFEGcMSQbnv5tpteK";
	private static final String line="P1";
	private static final String station="8161";
	
	public List<Message> messages;
	
	//@Test
	public void testConnection() {
		try{
			//TrueTimePredictionParser tester = new TrueTimePredictionParser(API_URL+"?key="+ACCESS_ID+"&rt="+line+"&stpid="+station);
			TrueTimeMessageParser tester = new TrueTimeMessageParser("http://truetime.portauthority.org/bustime/api/v1/getpredictions?key=cvTWAYXjbFEGcMSQbnv5tpteK&stpid=8161&rt=P1");
			messages=tester.parse();
			assertTrue(messages.size() > 0);
			//assertGreaterThan ("Messages should equal 1", 1, messages.size());
		} catch (Exception e){
			fail("Exception thrown:"+e.getMessage());
		}
	}

}

	