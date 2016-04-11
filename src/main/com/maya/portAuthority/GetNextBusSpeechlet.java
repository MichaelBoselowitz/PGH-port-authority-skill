package com.maya.portAuthority;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
//import com.amazon.speech.ui.SsmlOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import com.maya.portAuthority.api.Message;
import com.maya.portAuthority.api.TrueTimeMessageParser;

public class GetNextBusSpeechlet implements Speechlet {
	private static  Logger log = LoggerFactory.getLogger(GetNextBusSpeechlet.class);
	private static final String PREDICTION_URL="http://truetime.portauthority.org/bustime/api/v1/getpredictions";
	private static final String STOPS_URL="http://truetime.portauthority.org/bustime/api/v1/getstops";
	private static final String ACCESS_ID="cvTWAYXjbFEGcMSQbnv5tpteK";
	//private static final String line="P1";
	//private static final String stationID="3158,4833";
	
	public static final String SESSION_DIRECTION = "Direction";
	public static final String SESSION_STATION = "StationName";	
	public static final String SESSION_BUSLINE = "Route";

	private static  String SPEECH_WHICH_BUSLINE ="Which bus line would you like arrival information for?";
	private static  String SPEECH_WHICH_DIRECTION ="Which direction are you travelling?";
	private static  String SPEECH_WHICH_STATION ="Where do you get on the bus?";
	private static  String SPEECH_NO_SUCH_STATION="I can't find that station. Please say again.";


	private static  String SPEECH_INSTRUCTIONS=
			//"I can lead you through providing a bus line, direction, and "
			//+ "bus stop to get departure information, "
			//+ "or you can simply open Port Authroity and ask a question like, "
			//+ "when is the next outbound P1 leaving sixth and smithfield. "
			//+ "For a list of supported buslines, ask what bus lines are supported. "
			//+ 
			SPEECH_WHICH_BUSLINE;

	private static  String SPEECH_WELCOME="Welcome to Pittsburgh Port Authority "+ SPEECH_WHICH_BUSLINE;
	
	private static List<IntentHelper> dataHelpers;


	/**PUBLIC METHODS******************************/

	public SpeechletResponse onLaunch( LaunchRequest request,  Session session)
			throws SpeechletException {
		BasicConfigurator.configure();
		log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(),
				session.getSessionId());
		return newAskResponse(SPEECH_WELCOME, SPEECH_WHICH_BUSLINE);
	}

	public void onSessionStarted(SessionStartedRequest request, Session session)
			throws SpeechletException {
		log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(),
				session.getSessionId());
		// TODO any initialization logic goes here
		// Create Helpers HERE

	}
	
	public SpeechletResponse onIntent( IntentRequest request, Session session)
			throws SpeechletException {
	//	log.info("onIntent requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());

		Intent intent = request.getIntent();
		IntentHelper intentHelper = IntentHelperFactory.getIntentHelper(intent);
		try {
				logSession(session, intent.getName()+" before");
				
				intentHelper.putValuesInSession(session);
				
				SpeechletResponse response= handleDialog(session);

				logSession(session, "after");

				return response;
				
			} catch (Exception e) {
				log.debug("speechletExcpetion:"+e.getMessage());
				throw new SpeechletException(e.getMessage());
			}

	}

	@Override
	public void onSessionEnded(SessionEndedRequest request, Session session)
			throws SpeechletException {

		log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(),
				session.getSessionId());
		// TODO cleanup goes here.
	}

//	private SpeechletResponse getNextBusSpeechletResponse(Intent intent,  Session session) {
//		log.info("getNextBusSpeechletResponse() handling intent: "+intent.getName()+"...");
//
//		//input from intent
//
//		String stationName = (intent.getSlot(SLOT_STATION) != null) ? intent.getSlot(SLOT_STATION).getValue(): null; 
//		String directionName = (intent.getSlot(SLOT_DIRECTION) != null) ? intent.getSlot(SLOT_DIRECTION).getValue(): null; 
//		String buslineName = (intent.getSlot(SLOT_BUSLINE) != null) ? intent.getSlot(SLOT_BUSLINE).getValue(): null;
//		log.info("... with station:"+stationName+", direction:"+directionName+", busline:"+buslineName);
//		//output to speech
//		String outputText="";
//		String repromptText="";
//
//		//BusStation busStation=null;
//
//		// If I have all the information
//		if ((stationName!=null)&&(directionName!=null)&&(buslineName!=null)){
//			try {
//				//busStation = new BusStation(stationName);
//			} catch (Exception e) {
//				outputText="I don't know station " +stationName;
//				repromptText="What Station?";
//				//return newAskResponse (outputText, repromptText);
//				return newAskResponse(SPEECH_NO_SUCH_STATION+ SPEECH_WHICH_STATION, SPEECH_WHICH_STATION);
//			}
//			return getAnswerFromStationName(stationName, directionName, buslineName);
//
//			//Otherwise save what information I have and ask for more
//		} else {
//			if (stationName==null){
//				// initialize Station Question
//				outputText=SPEECH_WHICH_STATION;
//				repromptText=SPEECH_WHICH_STATION;
//			} else {
//				session.setAttribute(SESSION_STATION, stationName); 
//			}
//
//			if (directionName==null){
//				outputText=SPEECH_WHICH_DIRECTION;
//				repromptText=SPEECH_WHICH_DIRECTION;
//			} else {
//				session.setAttribute(SESSION_DIRECTION, directionName); 
//			}
//
//			if (buslineName==null){
//				outputText=SPEECH_WHICH_BUSLINE;
//				repromptText=SPEECH_WHICH_BUSLINE;
//			} else {
//				session.setAttribute(SESSION_BUSLINE, buslineName); 
//			}
//
//			return newAskResponse (outputText, repromptText); 
//
//		}
//
//
//
//	}



	private String getValueFromSession( Session session,  String name){
		if (session.getAttributes().containsKey(name)) {
			return (String) session.getAttribute(name);
		} else {
			return null;
		}
	}

//	private void putValuesInSession(Intent intent, Session session) throws Exception{
//		log.debug("putValuesInSession" );
//		//user supplied station
//		String stationName=getValueFromIntentSlot(intent,SLOT_STATION);
//		String direction=getValueFromIntentSlot(intent,SLOT_DIRECTION);
//		String busline=getValueFromIntentSlot(intent,SLOT_BUSLINE);
//		BusStation station=null;
//		log.debug("... with Slot station:"+stationName+", direction:"+direction+", busline:"+busline);
//
////!! stationName--- should be checking to see if the Intent is station.
//		if (stationName!=null){
//			station=new BusStation(stationName);
//			log.debug("putting value in session Slot station:"+stationName);
//			session.setAttribute(SESSION_STATION, station.name.toUpperCase()); 
//			session.setAttribute(SESSION_STATION_ID, station.ID);	
//		} 
//		if (direction!=null){
//			log.debug("putting value in sesion Slot direction:"+direction);
//			session.setAttribute(SESSION_DIRECTION, direction.toUpperCase());
//		}
//		if (busline!=null){
//			log.debug("putting value in sesion Slot busline:"+busline);
//			//TODO: remove whitepace
//			busline=busline.replaceAll("\\s+","");
//			session.setAttribute(SESSION_BUSLINE,busline.toUpperCase());
//		}
//
//		String sessionStationName=getValueFromSession(session,SESSION_STATION);
//		String sessionStationID=getValueFromSession(session,SESSION_STATION_ID);
//		String sessionDirection=getValueFromSession(session,SESSION_DIRECTION);
//		String sessionBusline=getValueFromSession(session,SESSION_BUSLINE);
//
//		log.debug("...existing with Session station:"+sessionStationName+", direction:"+sessionDirection+", busline:"+sessionBusline);
//	}

	private SpeechletResponse handleDialog(Session session) {
		log.trace("handling dialog() ...");

		String stationName=getValueFromSession(session,SESSION_STATION);
		//String stationID=getValueFromSession(session,SESSION_STATION_ID);
		String direction=getValueFromSession(session,SESSION_DIRECTION);
		String busline=getValueFromSession(session,SESSION_BUSLINE);

		if (stationName==null){//||(stationID==null)) {
			log.debug("Looking for Station");
			return newAskResponse(session, SPEECH_WHICH_STATION, SPEECH_WHICH_STATION);
		} else if (direction==null) {
			log.debug("Looking for Direction");
			return newAskResponse(session, SPEECH_WHICH_DIRECTION, SPEECH_WHICH_DIRECTION);
		} else if (busline==null) {
			log.debug("Looking for Busline");
			return newAskResponse(session, SPEECH_WHICH_BUSLINE, SPEECH_WHICH_BUSLINE);
		} else {
			try {
				log.info("Looking for Answer");
				return getAnswerFromStationName(stationName, direction, busline);
				//return getAnswer(BusStation.getBusStation(stationName), direction, busline);
			} catch (Exception e) {
				String outputText="I don't know station in session" + e.getMessage();
				return newAskResponse (session, outputText+ SPEECH_WHICH_STATION, SPEECH_WHICH_STATION); 			
			}
		}
	}

	public SpeechletResponse getAnswerFromStationName(String stationName, String direction, String busline){
		String stationID;
		try{

			CharSequence matchString=stationName.toUpperCase();
			int numMatches=0;

			String apiString= STOPS_URL+"?key="+ACCESS_ID+"&rt="+busline+"&dir="+direction;
			log.info("apiString="+apiString);

			TrueTimeMessageParser apiResponse = new TrueTimeMessageParser(apiString);
			List<Message> stops=apiResponse.parse();

			log.info("Number of "+direction+" stops for the "+busline+" line is "+stops.size());
			Iterator<Message> iterator = stops.iterator();
			while (iterator.hasNext()){
				Message element=(Message)iterator.next();
				log.info("Trying to Match: "+element.getStopName().toUpperCase() + "with "+matchString);
				if (element.getStopName().toUpperCase().contains(matchString)){
					log.info("found one");
					numMatches+=1;
				}else{
					iterator.remove();
				}
			}

			if (numMatches==0){
				String speechOutput = "For which station would you like bus arrival times?";
				return newAskResponse("I cannot find station. "+speechOutput,speechOutput);
				//				TODO: Handle Multiple Station Matches
				//				} else if (numMatches>1) {
				//					String speechOutput = "I can't figure out which station you want.";
				//					return newAskResponse("I found more than one station. "+speechOutput,speechOutput);
			} else {
				stationID=stops.get(0).getStopID();
				log.info("Station Name "+stationName+ " matched "+stationID);
				stationName=stops.get(0).getStopName();
			}
			
		}catch (Exception e){
			//e.printStackTrace();
			String speechOutput = "For which station would you like bus arrival times?";
			return newAskResponse("I cannot find station"+e.getMessage() +". "+speechOutput,speechOutput);
		}
		return getAnswer(stationID, stationName, direction, busline);
	}

	 
	private SpeechletResponse getAnswer(String stationID, String stationName, String direction,
			String busline) {
		
		//TODO: Handle bring back station names
		
		
		SimpleCard card = new SimpleCard();
		List<Message> messages;
		int when;
		log.info("getAnswer... with station:");
		//SsmlOutputSpeech outputSpeech= new SsmlOutputSpeech();
		PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();


		if ((stationID==null)||(direction==null)||(busline==null)){
			//outputSpeech.setSsml("I forgot what you told me");
			outputSpeech.setText("I forgot what you told me");
			return SpeechletResponse.newTellResponse(outputSpeech, new SimpleCard());
		}
		log.info(stationID+", direction:"+direction+", busline:"+busline);

		try { 
			String apiString= PREDICTION_URL+"?key="+ACCESS_ID+"&rt="+busline+"&stpid="+stationID;
			log.info("apiString="+apiString);

			TrueTimeMessageParser tester = new TrueTimeMessageParser(apiString);
			messages=tester.parse();
			log.info("messages size"+messages.size());

			//Define speech output
			String speechOutput = "";

			if (messages.size()==0){
				speechOutput=" No "+direction+", "+ busline +" is expected at " + stationName + " in the next 30 minutes  ";
				
			} else {

				for (int i=0;i<messages.size();i++){
					when=messages.get(i).getEstimate();
					if (i==0){ 
						if (when < 3){
							speechOutput="An "+direction+" "+busline+ 
									" is arriving at " + stationName + "now ";
						} else {
							speechOutput="An "+direction+" "+busline+ 
									" will be arriving at " + stationName + " in "+when+" minutes ";
						}
					} else {
						speechOutput=speechOutput+" and another in"+when+" minutes";
					}
					//TODO: Add Ssml
					//speechOutput=speechOutput+" <break time=1s> ";
				}
			}

			// Create the Simple card content.

			card.setTitle("Pittsburgh Port Authority");
			card.setContent(speechOutput);

			// Create the plain text output
			outputSpeech.setText(speechOutput);
			//outputSpeech.setSsml(speechOutput);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return SpeechletResponse.newTellResponse(outputSpeech, card);
	}

	private SpeechletResponse newAskResponse (Session session, String output, String reprompt){
		String stationName=getValueFromSession(session,SESSION_STATION);
		//String stationID=getValueFromSession(session,SESSION_STATION_ID);
		String direction=getValueFromSession(session,SESSION_DIRECTION);
		String busline=getValueFromSession(session,SESSION_BUSLINE);
		log.debug("Asking Response with Session station:"+stationName+", direction:"+direction+", busline:"+busline);
		return newAskResponse(output, reprompt);
	}


	/**
	 * Wrapper for creating the Ask response from the input strings.
	 * 
	 * @param stringOutput
	 *            the output to be spoken
	 * @param repromptText
	 *            the reprompt for if the user doesn't reply or is misunderstood.
	 * @return SpeechletResponse the speechlet response
	 */
	private SpeechletResponse newAskResponse(String stringOutput, String repromptText) {
		PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
		outputSpeech.setText(stringOutput);
		//SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
		//outputSpeech.setSsml(stringOutput);
		
		PlainTextOutputSpeech repromptOutputSpeech = new PlainTextOutputSpeech();
		repromptOutputSpeech.setText(repromptText);
		//SsmlOutputSpeech repromptOutputSpeech = new SsmlOutputSpeech();
		//repromptOutputSpeech.setSsml(repromptText);
		
		Reprompt reprompt = new Reprompt();
		reprompt.setOutputSpeech(repromptOutputSpeech);
		return SpeechletResponse.newAskResponse(outputSpeech, reprompt);
	}

	private void logSession(Session session, String intro){
		String stationName=getValueFromSession(session,SESSION_STATION);
		//String stationID=getValueFromSession(session,SESSION_STATION_ID);
		String direction=getValueFromSession(session,SESSION_DIRECTION);
		String busline=getValueFromSession(session,SESSION_BUSLINE);

		log.info(intro+"... with Session station:"+stationName+",direction:"+direction+", busline:"+busline);
	}
}
