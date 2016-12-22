package com.maya.portAuthority;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.SsmlOutputSpeech;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import com.maya.portAuthority.api.Message;
import com.maya.portAuthority.api.TrueTimeMessageParser;
import com.maya.portAuthority.storage.PaDao;
import com.maya.portAuthority.storage.PaDynamoDbClient;
import com.maya.portAuthority.storage.PaInput;
import com.maya.portAuthority.storage.PaInputData;
import com.maya.portAuthority.util.*;
import com.maya.portAuthority.googleMaps.*;

public class GetNextBusSpeechlet implements Speechlet {
	private static Logger log = LoggerFactory.getLogger(GetNextBusSpeechlet.class);


	private static String SPEECH_INSTRUCTIONS = "I can lead you through providing a bus line, direction, and "
			+ "bus stop to get departure information, "
			+ "or you can simply open Port Authroity and ask a question like, "
			+ "when is the next outbound P1 leaving sixth and smithfield. "
			+ "For a list of supported buslines, ask what bus lines are supported. ";

	private static String SPEECH_WELCOME = "Welcome to Pittsburgh Port Authority ";

	private static String AUDIO_WELCOME = "<audio src=\"https://s3.amazonaws.com/maya-audio/ppa_welcome.mp3\" />";
	private static String AUDIO_FAILURE = "<audio src=\"https://s3.amazonaws.com/maya-audio/ppa_failure.mp3\" />";
	private static String AUDIO_SUCCESS = "<audio src=\"https://s3.amazonaws.com/maya-audio/ppa_success.mp3\" />";
	private static String INCLUDE_ROUTE="IncludeRoute";
	private SkillContext skillContext;

	private Map<String, DataHelper> dataHelpers;
	
	private AmazonDynamoDBClient amazonDynamoDBClient;
	private PaDynamoDbClient dynamoDbClient;
	private PaDao inputDao;
	
	AnalyticsManager analytics;



	/** PUBLIC METHODS ******************************/
	/**
	 * called when the skill is first requested and no intent is provided
	 */
	public SpeechletResponse onLaunch(LaunchRequest request, Session session) throws SpeechletException {
		BasicConfigurator.configure();
		log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
		skillContext = new SkillContext();

		if (amazonDynamoDBClient == null) {
			amazonDynamoDBClient = new AmazonDynamoDBClient();

		}

		dynamoDbClient = new PaDynamoDbClient(amazonDynamoDBClient);
		inputDao = new PaDao(dynamoDbClient);
		PaInput input = inputDao.getPaInput(session);
		if ((input != null) && input.hasAllData()){
			analytics.postEvent(AnalyticsManager.CATEGORY_LAUNCH, "Return Saved");
			return buildAllRoutesResponse(input.getData());
		} else {
			analytics.postEvent(AnalyticsManager.CATEGORY_LAUNCH, "Welcome");
			return newAskResponse(AUDIO_WELCOME + SPEECH_WELCOME + RouteHelper.SPEECH, RouteHelper.SPEECH);
		}
	}

	/**
	 * Called when an intent is first received, before handing to onIntent.
	 * Establishes which
	 */
	public void onSessionStarted(SessionStartedRequest request, Session session) throws SpeechletException {
		log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
		
		analytics = new AnalyticsManager();
		analytics.setUserId(session.getUser().getUserId());
		analytics.postSessionEvent(AnalyticsManager.ACTION_SESSION_START);
		
		// TODO: Not a HASHMAP
		this.dataHelpers = new HashMap<String, DataHelper>();// createDataHelpers(session);
		dataHelpers.put(RouteHelper.INTENT_NAME, DataHelperFactory.getHelper(RouteHelper.NAME));
		dataHelpers.put(LocationHelper.INTENT_NAME, DataHelperFactory.getHelper(LocationHelper.NAME));
		dataHelpers.put(DirectionHelper.INTENT_NAME, DataHelperFactory.getHelper(DirectionHelper.NAME));
	}

	/**
	 * Called when the user invokes an intent.
	 */
	public SpeechletResponse onIntent(IntentRequest request, Session session) throws SpeechletException {
	     log.info("onIntent intent={}, requestId={}, sessionId={}", request.getIntent().getName(), request.getRequestId(), session.getSessionId());
	 	String feedbackText = "";
		try {
			Intent intent = request.getIntent();
			analytics.postEvent(AnalyticsManager.CATEGORY_INTENT, intent.getName());
			if (intent.getName().equals("OneshotBusIntent")){
				Iterator<DataHelper> itr = dataHelpers.values().iterator();
				while (itr.hasNext()){
					DataHelper dataHelper=itr.next();
					feedbackText+=dataHelper.putValuesInSession(session,intent);
				}

			} else { //DirectionBusIntent {Direction} || RouteBusIntent {Route} || StationBusIntent {StationName}
				DataHelper dataHelper = dataHelpers.get(intent.getName());
				feedbackText=dataHelper.putValuesInSession(session,intent);
			}
		} catch (InvalidInputException e) {
			analytics.postException(e.getMessage(), false);
			return newAskResponse(session, e.getSpeech(), e.getSpeech()); 
		}

		
		SpeechletResponse furtherQuestions;
		if ((furtherQuestions=checkForAdditionalQuestions(session, feedbackText))!=null){
			return furtherQuestions;
		} else if (log.isInfoEnabled()) {
			logSession(session, "Returning response for:");
		}
		// OK, the user has entered everything, save their entries
		analytics.postEvent(AnalyticsManager.CATEGORY_INTENT, "Collected all input" );
		
		try {
			
			//TODO: Make Session Data be a PaInput
			//Map<String,String> sessionData= getInputValuesFromSession();
			Map<String,Object> sessionData= session.getAttributes();
			PaInputData inputData=PaInputData.newInstance();
			inputData.setDirection(sessionData.get(DirectionHelper.NAME).toString());
			inputData.setRouteID(sessionData.get(RouteHelper.NAME).toString());
			inputData.setRouteName(sessionData.get(RouteHelper.ROUTE_NAME).toString());
			inputData.setLocationName(sessionData.get(LocationHelper.NAME).toString());
			inputData.setLocationAddress(sessionData.get("address").toString());
			inputData.setLocationLat(sessionData.get("lat").toString());
			inputData.setLocationLong(sessionData.get("long").toString());

			Coordinates c=new Coordinates();
			c.setAddress(inputData.getLocationAddress() );
			c.setLat(new Double(inputData.getLocationLat()).doubleValue() );
			c.setLng(new Double(inputData.getLocationLong()).doubleValue() );

			Stop nearestStop=NearestStopLocator.process(c, inputData.getRouteID(),inputData.getDirection());
			inputData.setStop(nearestStop);

			saveInputToDB(PaInput.newInstance(session, inputData));

			// get speech response for all stops
			return buildResponse(inputData);
			
		} catch (Exception e) {
			//TODO: Handle this excpetion appropriately. 
			e.printStackTrace();
			analytics.postException(e.getMessage(), false);
			return newAskResponse(session, e.getMessage(), e.getMessage()); 
		}
	}
	

		

	public void onSessionEnded(SessionEndedRequest request, Session session) throws SpeechletException {
		log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
		this.dataHelpers.clear();
		analytics.postSessionEvent(AnalyticsManager.ACTION_SESSION_END);
	}

	//////////////////////////////PRIVATE METHODS////////////////////////////
	private SpeechletResponse checkForAdditionalQuestions(Session session) {
		return checkForAdditionalQuestions(session, "");
	}
	
	private SpeechletResponse checkForAdditionalQuestions(Session session, String feedbackText) {
		if (log.isInfoEnabled()) {
			logSession(session, "checkingForAdditionalQuestions: feedbackText={}"+feedbackText);
		}
		// Do I have all the data I need?
		Iterator<DataHelper> itr = dataHelpers.values().iterator();
		while (itr.hasNext()) {
			DataHelper element = itr.next();

			if (element.getValueFromSession(session) == null) {
				log.trace(element.getName() + ":" + element.getValueFromSession(session) + "==null");
				return newAskResponse(session, feedbackText+","+element.getSpeech(), element.getSpeech());
			} else {
				log.trace(element.getName() + ":" + element.getValueFromSession(session) + "!=null");
			}
		}

		return null;
	}


	private SpeechletResponse buildAllRoutesResponse(PaInputData inputData) {
		String speechOutput = "";
		String textOutput = "";
		String locationText;
		SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
		
		try{
			List<Message> messages = new ArrayList<Message>();
			messages = TrueTimeMessageParser.getPredictions(inputData.getStopID());

			if (messages.size() == 0) {
				log.info("No Messages");

				textOutput = " No " + inputData.getDirection() + ", busses are expected at " + inputData.getStopName()
				+ " in the next 30 minutes  ";
				speechOutput = AUDIO_FAILURE + textOutput;
				analytics.postEvent(AnalyticsManager.CATEGORY_RESPONSE, "No Result", textOutput, messages.size());
				outputSpeech.setSsml("<speak> " + speechOutput + "</speak>");
				return SpeechletResponse.newTellResponse(outputSpeech, buildCard(textOutput));
			}
			
			if ((messages.size() == 1) && (messages.get(0).getMessageType().equals(Message.ERROR))) {
				log.error("1 error message:" + messages.get(0).getError());
				textOutput = " No " + inputData.getDirection() + ", busses are expected at " + inputData.getStopName()
				+ " in the next 30 minutes  ";
				speechOutput = AUDIO_FAILURE +textOutput;
				analytics.postEvent(AnalyticsManager.CATEGORY_RESPONSE, "No Result", textOutput, messages.size());
				outputSpeech.setSsml("<speak> " + speechOutput + "</speak>");
				return SpeechletResponse.newTellResponse(outputSpeech, buildCard(textOutput));

			} 
			ArrayList<Result> results=new ArrayList<Result>();
			for (int i = 0; i < messages.size(); i++) {				
				results.add(new Result(messages.get(i).getRouteID(), messages.get(i).getEstimate()));
			}
			//TODO: Collect Route responses together, but Return the first bus first. 
			Collections.sort(results);
			
			locationText="At " +inputData.getStopName()+",";
			textOutput=locationText;
			speechOutput = AUDIO_SUCCESS + locationText+"<break time=\"0.1s\" />";
			int when;
			String routeID;
			String lastRouteID=null;
			for (int i = 0; i < results.size(); i++) {
				
				routeID=results.get(i).getRoute();
				when = results.get(i).getEstimate();
			
				if (i==0){
					textOutput += " The " + routeID + " will be arriving in " + when + " minutes ";
					speechOutput += "The " + routeID + " will be arriving in " + when + " minutes ";
				} else if (!routeID.equals(lastRouteID)){
					textOutput += ".\n The " + routeID + " will be arriving in " + when + " minutes ";
					speechOutput += "<break time=\"0.25s\" /> The " + routeID + " will be arriving in " + when + " minutes ";
				} else {
					textOutput += " and "+when+ " minutes ";
					speechOutput += " and "+when+ " minutes ";
				}
				lastRouteID=routeID;
			}
			outputSpeech.setSsml("<speak> " + speechOutput + "</speak>");
			analytics.postEvent(AnalyticsManager.CATEGORY_RESPONSE, "Success", textOutput, messages.size());
			return SpeechletResponse.newTellResponse(outputSpeech, buildCard(textOutput));
		} catch (Exception e) {
			analytics.postException(e.getMessage(), true);
			e.printStackTrace();
		}
		return null;
	}
	
	private SpeechletResponse buildResponse(PaInputData inputData) {
		String speechOutput = "";
		String textOutput = "";

		
		List<Message> messages = new ArrayList<Message>();
		messages = TrueTimeMessageParser.getPredictions(inputData.getRouteID(), inputData.getStopID());
		log.info("getAnswer... with " + messages.size() + "messages");

		try {
			SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
			int when;
			String locationOutput="The nearest stop to "+  inputData.getLocationName() +" is " + inputData.getStopName()+".";

			//speechOutput="The nearest stop to "+  inputData.getLocationName() + " is " + inputData.getStopName()+".";

			if (messages.size() == 0) {
				log.info("No Messages");

				textOutput = locationOutput+" No " + inputData.getDirection() + ", " + inputData.getRouteID() + " is expected at " + inputData.getStopName()
				+ " in the next 30 minutes  ";
				speechOutput = AUDIO_FAILURE + textOutput;
				analytics.postEvent(AnalyticsManager.CATEGORY_RESPONSE, "No Result", textOutput, messages.size());
				outputSpeech.setSsml("<speak> " + speechOutput + "</speak>");
				return SpeechletResponse.newTellResponse(outputSpeech, buildCard(textOutput));
			}
			if ((messages.size() == 1) && (messages.get(0).getMessageType().equals(Message.ERROR))) {
				log.error("1 error message:" + messages.get(0).getError());
				textOutput = " No " + inputData.getDirection() + ", " + inputData.getRouteID() + " is expected at " + inputData.getStopName()
				+ " in the next 30 minutes  ";
				speechOutput = AUDIO_FAILURE + locationOutput+"<break time=\"0.1s\" />"+textOutput;
				textOutput = locationOutput+textOutput;
				speechOutput = AUDIO_FAILURE + textOutput;
				analytics.postEvent(AnalyticsManager.CATEGORY_RESPONSE, "No Result", textOutput, messages.size());
				outputSpeech.setSsml("<speak> " + speechOutput + "</speak>");
				return SpeechletResponse.newTellResponse(outputSpeech, buildCard(textOutput));
			}

			for (int i = 0; i < messages.size(); i++) {
				log.trace("Message[" + i + "]= " + messages.get(i).getMessageType());
				when = messages.get(i).getEstimate();
				if (i == 0) {
					if (when < 3) {
						textOutput = " An " + inputData.getDirection() + " " + inputData.getRouteID() + " is arriving at now ";
						speechOutput = AUDIO_SUCCESS + locationOutput+ "<break time=\"0.1s\" /> An " + inputData.getDirection() + " " + inputData.getRouteID() + " is arriving <break time=\"0.1s\" /> now ";
						textOutput= locationOutput + textOutput;
					} else {
						textOutput = " An " + inputData.getDirection() + " " + inputData.getRouteID() + " will be arriving in " + when + " minutes ";
						speechOutput += AUDIO_SUCCESS + locationOutput+ "<break time=\"0.1s\" />"+ textOutput;
						textOutput= locationOutput + textOutput;

					}
				} else {
					textOutput = textOutput + " ... and in " + when + " minutes";
					speechOutput = speechOutput + " <break time=\"0.25s\" /> and in " + when + " minutes";
				}
			}
			String endText="<break time=\"0.25s\" />  to hear predictions for all routes that stop at "+inputData.getStopName() + "say Alexa, launch Pittsburgh Bus";
			outputSpeech.setSsml("<speak> " + speechOutput + endText+"</speak>");
			analytics.postEvent(AnalyticsManager.CATEGORY_RESPONSE, "Success", textOutput, messages.size());
			return SpeechletResponse.newTellResponse(outputSpeech, buildCard(textOutput));
		} catch (Exception e) {
			analytics.postException(e.getMessage(), true);
			e.printStackTrace();
		}
		return null;

	}
	
	private SimpleCard buildCard(String s){
		SimpleCard card=new SimpleCard();
		card.setTitle("Pittsburgh Port Authority");
		card.setContent(s);
		return card;
	}

	private SpeechletResponse newAskResponse(Session session, String output, String reprompt) {
		if (log.isTraceEnabled()) {
			this.logSession(session, "newAskResponse");
		}
		return newAskResponse(output, reprompt);
	}

	/**
	 * Wrapper for creating the Ask response from the input strings.
	 * 
	 * @param stringOutput
	 *            the output to be spoken
	 * @param repromptText
	 *            the reprompt for if the user doesn't reply or is
	 *            misunderstood.
	 * @return SpeechletResponse the speechlet response
	 */
	private SpeechletResponse newAskResponse(String stringOutput, String repromptText) {
		// PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
		// outputSpeech.setText(stringOutput);
		SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
		outputSpeech.setSsml("<speak> " + stringOutput + " </speak>");

		PlainTextOutputSpeech repromptOutputSpeech = new PlainTextOutputSpeech();
		repromptOutputSpeech.setText(repromptText);
		// SsmlOutputSpeech repromptOutputSpeech = new SsmlOutputSpeech();
		// repromptOutputSpeech.setSsml(repromptText);

		Reprompt reprompt = new Reprompt();
		reprompt.setOutputSpeech(repromptOutputSpeech);
		return SpeechletResponse.newAskResponse(outputSpeech, reprompt);
	}

	/**
	 * Helper method to log the data currently stored in session.
	 * 
	 * @param session
	 * @param intro
	 */
	private void logSession(Session session, String intro) {
//		Iterator<DataHelper> itr = dataHelpers.values().iterator();
//		while (itr.hasNext()) {
//			DataHelper element = itr.next();
//			log.info(intro + "Session:" + element.getName() + ":" + element.getValueFromSession());
//		}
//		log.info(intro+"Session:address:"+session.getAttribute("address") );
//		log.info(session.toString());
		Map <String, Object> attributes=session.getAttributes();
		Set<String> set= attributes.keySet();
		Iterator<String> itr=set.iterator();
		while (itr.hasNext()) {
			String element=itr.next();
			log.info(intro + "Session:" + element+ ":" + session.getAttribute(element));
		}
	}

	/**
	 * Matches numerics to Strings, too.
	 * 
	 * @return
	 */
	private boolean match(String s1, String s2) {
		if (s1.toUpperCase().contains(s2.toUpperCase())) {
			return true;
		}
		// replace numbers with words
		if (StringUtils.isAlphanumericSpace(s1) && !StringUtils.isAlphaSpace(s1)) {
			s1 = replaceNumWithOrdinalWord(s1);
		}
		if (StringUtils.isAlphanumericSpace(s2) && !StringUtils.isAlphaSpace(s2)) {
			s2 = replaceNumWithOrdinalWord(s2);
		}
		if (s1.toUpperCase().contains(s2.toUpperCase())) {
			return true;
		}
		return false;
	}

	private String replaceNumWithOrdinalWord(String inputString) {
		log.debug("replaceNumWithOrdinalWord input:" + inputString);
		StringBuffer output = new StringBuffer(inputString.length());
		String digitStr = "";

		for (int i = 0; i < inputString.length(); i++) {
			if (Character.isDigit(inputString.charAt(i))) {
				digitStr += inputString.charAt(i);
			} else if (Character.isAlphabetic(inputString.charAt(i)) && !digitStr.isEmpty()) {
				// ignore alphabetics that are juxtaposed with digits
			} else if (digitStr.isEmpty()) {
				output.append(inputString.charAt(i));
			} else {
				// translate the digits and move them over
				output.append(NumberMaps.num2OrdWordMap.get(Integer.parseInt(digitStr)));
				digitStr = "";
			}
		}
		if (!digitStr.isEmpty()) {
			// translate the digits and move them over
			output.append(NumberMaps.num2OrdWordMap.get(Integer.parseInt(digitStr)));
			digitStr = "";
		}
		String returnValue = new String(output);
		log.debug("replaceNumWithOrdinalWord returning:" + returnValue);
		return returnValue;
	}
	

		
	private void saveInputToDB(PaInput input){
		if (amazonDynamoDBClient == null) {amazonDynamoDBClient = new AmazonDynamoDBClient();}
		if (dynamoDbClient == null) { dynamoDbClient = new PaDynamoDbClient(amazonDynamoDBClient);}
		if (inputDao==null) {inputDao = new PaDao(dynamoDbClient);}
		inputDao.savePaInput(input);

	}
	
	public class Result implements Comparable<Result>{
		String route;
		int estimate;
		
		Result (String route, int prediction){
			this.route=route;
			this.estimate=prediction;
		}
		
		public int compareTo(Result r){
			if (route.compareTo(r.route)>0)  return 1;
			if (route.equalsIgnoreCase(r.route)){  
				if (estimate>r.estimate) return 1;
				if (estimate==r.estimate) return 0;
			}
			return -1;
		}
		
		public String getRoute(){
			return this.route;
		}
		
		public int getEstimate(){
			return this.estimate;
		}
	}
}

