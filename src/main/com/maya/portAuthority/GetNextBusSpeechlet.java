package com.maya.portAuthority;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

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
import com.amazon.speech.ui.SsmlOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import com.maya.portAuthority.api.Message;
import com.maya.portAuthority.api.TrueTimeMessageParser;
import com.maya.portAuthority.util.*;

public class GetNextBusSpeechlet implements Speechlet {
	private static  Logger log = LoggerFactory.getLogger(GetNextBusSpeechlet.class);

	private static  String SPEECH_NO_SUCH_STATION="I can't find that station. Please say again.";

	private static  String SPEECH_INSTRUCTIONS=
			"I can lead you through providing a bus line, direction, and "
					+ "bus stop to get departure information, "
					+ "or you can simply open Port Authroity and ask a question like, "
					+ "when is the next outbound P1 leaving sixth and smithfield. "
					+ "For a list of supported buslines, ask what bus lines are supported. ";

	private static  String SPEECH_WELCOME="Welcome to Pittsburgh Port Authority ";
	
	private static String AUDIO_WELCOME="<audio src=\"https://s3.amazonaws.com/maya-audio/ppa_welcome.mp3\" />";
	private static String AUDIO_FAILURE="<audio src=\"https://s3.amazonaws.com/maya-audio/ppa_failure.mp3\" />";
	private static String AUDIO_SUCCESS="<audio src=\"https://s3.amazonaws.com/maya-audio/ppa_success.mp3\" />";
	
	private Map<String, DataHelper> dataHelpers;


	/**PUBLIC METHODS******************************/
/**
 * called when the skill is first requested and no intent is provided
 */
	public SpeechletResponse onLaunch( LaunchRequest request,  Session session)
			throws SpeechletException {
		BasicConfigurator.configure();
		log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(),
				session.getSessionId());
		return newAskResponse(AUDIO_WELCOME+SPEECH_WELCOME+RouteHelper.SPEECH, RouteHelper.SPEECH);
	}
/**
 * Called when an intent is first received, before handing to onIntent. Establishes which 
 */
	public void onSessionStarted(SessionStartedRequest request, Session session)
			throws SpeechletException {
		log.trace("onSessionStarted requestId={}, sessionId={}", request.getRequestId(),
				session.getSessionId());
		//TODO: Not a HASHMAP
		this.dataHelpers=new HashMap<String, DataHelper>();//createDataHelpers(session);
		dataHelpers.put(RouteHelper.INTENT_NAME, DataHelperFactory.getHelper(session, RouteHelper.NAME));
		dataHelpers.put(BusStopHelper.INTENT_NAME, DataHelperFactory.getHelper(session, BusStopHelper.NAME));
		dataHelpers.put(DirectionHelper.INTENT_NAME, DataHelperFactory.getHelper(session, DirectionHelper.NAME));
	}

/**
 * Called when the user invokes an intent. 
 */
	public SpeechletResponse onIntent(IntentRequest request, Session session)
			throws SpeechletException {
		//	log.trace("onIntent requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());

		try {
			Intent intent = request.getIntent();

			if (intent.getName().equals("OneshotBusIntent")){
				Iterator<DataHelper> itr = dataHelpers.values().iterator();
				while (itr.hasNext()){
					DataHelper dataHelper=itr.next();
					log.info(dataHelper.getIntentName()+":"+dataHelper.getValueFromIntentSlot(intent));
					dataHelper.putValuesInSession(intent);
				}

			} else { //DirectionBusIntent {Direction} || RouteBusIntent {Route} || StationBusIntent {StationName}
				DataHelper dataHelper = dataHelpers.get(intent.getName());
				log.info(dataHelper.getIntentName()+":"+dataHelper.getValueFromIntentSlot(intent));
				//validate input
				dataHelper.putValuesInSession(intent);
			}
		} catch (InvalidInputException e) {
			
			return newAskResponse(session, e.getSpeech(), e.getSpeech()); 
		}

		
		SpeechletResponse furtherQuestions;
		if ((furtherQuestions=checkForAdditionalQuestions(session))!=null){
			return furtherQuestions;
		} else if (log.isInfoEnabled()){
			logSession(session, "Returning response for:");
		}
		


		// OK, the user has entered everything, now let's find their response
		Map<String, String> input = getInputValuesFromSession();


		List<Message> stops= getMatchedBusStops(input);
		//log.trace("Found "+stops.size()+ "matching stops");

		//if 0 ask again
		if (stops==null||stops.isEmpty()){
			return newAskResponse(AUDIO_FAILURE+"I cannot find a stop that matches. "+input.get(BusStopHelper.NAME)+
					" <break time=\"0.1s\" /> for "+input.get(DirectionHelper.NAME)+" "+input.get(RouteHelper.NAME) + 
					" <break time=\"0.1s\" /> "+BusStopHelper.SPEECH,
					BusStopHelper.SPEECH);
		}

//		if (stops.size()>1){
//			String speechOutput = "I found several stops that match. try specifying a cross street.";
//			return newAskResponse(speechOutput+BusStopHelper.SPEECH,BusStopHelper.SPEECH);
//		}

		//if 1 find answer and respond
		List<Message> messages= new ArrayList<Message>();
		String stationID=stops.get(0).getStopID();
		String stationName=stops.get(0).getStopName();
		log.trace("Station Name "+stationName+ " matched "+stationID);
		messages=TrueTimeMessageParser.getPredictions(input.get(RouteHelper.NAME), stationID);

		// get speech response for all stops
		return getAnswer (messages, input.get(RouteHelper.NAME), stationName, input.get(DirectionHelper.NAME));
	}

	public void onSessionEnded(SessionEndedRequest request, Session session)
			throws SpeechletException {

		log.trace("onSessionEnded requestId={}, sessionId={}", request.getRequestId(),
				session.getSessionId());
		this.dataHelpers.clear();
	}


	private SpeechletResponse checkForAdditionalQuestions(Session session) {
		if (log.isTraceEnabled()){
			logSession(session, "checkingForAdditionalQuestions");
		}
		// Do I have all the data I need?
		Iterator<DataHelper> itr=dataHelpers.values().iterator();	
		while (itr.hasNext()){
			DataHelper element=itr.next();

			if (element.getValueFromSession()==null){
				log.trace(element.getName()+":"+element.getValueFromSession()+"==null");
				return newAskResponse(session, element.getSpeech(), element.getSpeech()); 	
			} else {
				log.trace(element.getName()+":"+element.getValueFromSession()+"!=null");
			}
		}

		return null;
	}

	private List<Message> getMatchedBusStops(Map<String, String> input){
		String matchString=input.get(BusStopHelper.NAME);
		List<Message> stops = TrueTimeMessageParser.getStops(input.get(RouteHelper.NAME),input.get(DirectionHelper.NAME));
		Iterator<Message> iterator = stops.iterator();
		while (iterator.hasNext()){
			Message element=(Message)iterator.next();
			if (element.getMessageType().equalsIgnoreCase("error")){
				log.error(element.getError()+ ": probably no stops found on "+ input.get(BusStopHelper.NAME)+" for "+ input.get(DirectionHelper.NAME) + " "+input.get(RouteHelper.NAME));
				return null;
			} else if (element.getMessageType().equalsIgnoreCase("stop")){
				log.debug("Trying to Match: "+element.getStopName().toUpperCase() + "with "+matchString);
				//if (element.getStopName().toUpperCase().contains(matchString)){
				if (!match(element.getStopName().toUpperCase(), matchString)){
					iterator.remove();
				}
			} else {
				iterator.remove();
			}
		}
	return stops;

	}

	private SpeechletResponse getAnswer(List<Message> messages, String busline , String stationName, String direction) {
		SsmlOutputSpeech outputSpeech= new SsmlOutputSpeech();
		//PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
		SimpleCard card = new SimpleCard();
		int when;
		log.trace("getAnswer... with "+messages.size()+ "messages");
		

		try { 
			//Define speech output
			String speechOutput = "";

			if (messages.size()==0){
				log.info("No Messages");
				speechOutput=AUDIO_FAILURE+" No "+direction+", "+ busline +" is expected at " + stationName + " in the next 30 minutes  ";

			} else {
				if ((messages.size()==1)&&(messages.get(0).getMessageType().equals(Message.ERROR))){
					log.info("1 error message:"+ messages.get(0).getError());
					speechOutput=AUDIO_FAILURE+" No "+direction+", "+ busline +" is expected at " + stationName + " in the next 30 minutes  ";
				} else {
					log.info(messages.size()+" messages");
					for (int i=0;i<messages.size();i++){
						log.trace("Message["+i+"]= "+messages.get(i).getMessageType() );
						when=messages.get(i).getEstimate();
						if (i==0){ 
							if (when < 3){
								speechOutput=AUDIO_SUCCESS+"An "+direction+" "+busline+ 
										" is arriving at " + stationName +" <break time=\"0.1s\" /> now ";
							} else {
								speechOutput=AUDIO_SUCCESS+"An "+direction+" "+busline+ 
										" will be arriving at " + stationName + " in "+when+" minutes ";
							}
						} else {
							speechOutput=speechOutput+" <break time=\"0.25s\" /> and another in "+when+" minutes";
						}
					}
				}
			}

			// Create the Simple card content.

			card.setTitle("Pittsburgh Port Authority");
			card.setContent(speechOutput);

			// Create the plain text output
			//outputSpeech.setText(speechOutput);
			outputSpeech.setSsml("<speak> "+speechOutput+"</speak>");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return SpeechletResponse.newTellResponse(outputSpeech, card);
	}

	private SpeechletResponse newAskResponse (Session session, String output, String reprompt){
		if (log.isTraceEnabled()){
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
	 *            the reprompt for if the user doesn't reply or is misunderstood.
	 * @return SpeechletResponse the speechlet response
	 */
	private SpeechletResponse newAskResponse(String stringOutput, String repromptText) {
		//PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
		//outputSpeech.setText(stringOutput);
		SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
		outputSpeech.setSsml("<speak> "+stringOutput+" </speak>");
		
		PlainTextOutputSpeech repromptOutputSpeech = new PlainTextOutputSpeech();
		repromptOutputSpeech.setText(repromptText);
		//SsmlOutputSpeech repromptOutputSpeech = new SsmlOutputSpeech();
		//repromptOutputSpeech.setSsml(repromptText);

		Reprompt reprompt = new Reprompt();
		reprompt.setOutputSpeech(repromptOutputSpeech);
		return SpeechletResponse.newAskResponse(outputSpeech, reprompt);
	}
	/**
	 * Helper method to log the data currently stored in session.
	 * @param session
	 * @param intro
	 */
	private void logSession(Session session, String intro){
		Iterator<DataHelper> itr = dataHelpers.values().iterator();
		while (itr.hasNext()){
			DataHelper element=itr.next();
			log.info(intro + "Session:"+element.getName()+":"+element.getValueFromSession());
		}
	}
	
	/**
	 * Matches numerics to Strings, too.
	 * @return
	 */
	private boolean match (String s1, String s2){
		if (s1.toUpperCase().contains(s2.toUpperCase())){
			return true;
		}
		//replace numbers with words
		if (StringUtils.isAlphanumericSpace(s1)&&!StringUtils.isAlphaSpace(s1)) {
			s1=replaceNumWithOrdinalWord(s1);
		}
		if (StringUtils.isAlphanumericSpace(s2)&&!StringUtils.isAlphaSpace(s2)) {
			s2=replaceNumWithOrdinalWord(s2);
		}
		if (s1.toUpperCase().contains(s2.toUpperCase())){
			return true;
		}
		return false;
	}
	
	private String replaceNumWithOrdinalWord(String inputString){
		log.debug("replaceNumWithOrdinalWord input:"+inputString);
		StringBuffer output = new StringBuffer(inputString.length());
		String digitStr ="";

		for (int i = 0; i < inputString.length(); i++) {
			if (Character.isDigit(inputString.charAt(i))) {
				digitStr += inputString.charAt(i);
			} else if (Character.isAlphabetic(inputString.charAt(i))&&!digitStr.isEmpty()){
				//ignore alphabetics that are juxtaposed with digits
			} else if (digitStr.isEmpty()) {
				output.append(inputString.charAt(i));
			} else {
				//translate the digits and move them over
				output.append(NumberMaps.num2OrdWordMap.get(Integer.parseInt(digitStr))) ;
				digitStr = "";
			}
		}
		if (!digitStr.isEmpty()) {
			//translate the digits and move them over
			output.append(NumberMaps.num2OrdWordMap.get(Integer.parseInt(digitStr))) ;
			digitStr = "";
		}
		String returnValue= new String (output);
		log.debug("replaceNumWithOrdinalWord returning:"+returnValue);
		return returnValue;
	}
	/**
	 * 
	 * **/
	private Map<String, String> getInputValuesFromSession(){
		Map<String, String> input = new HashMap<String, String>();
		Iterator<DataHelper> itr = dataHelpers.values().iterator();
		while (itr.hasNext()){
			DataHelper element=itr.next();
			input.put(element.getName(), element.getValueFromSession());
		}
		return input;
	}
}
