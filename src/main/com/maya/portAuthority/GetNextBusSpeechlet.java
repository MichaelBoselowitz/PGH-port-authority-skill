package com.maya.portAuthority;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

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
	//	private static final String PREDICTION_URL="http://truetime.portauthority.org/bustime/api/v1/getpredictions";
	//	private static final String STOPS_URL="http://truetime.portauthority.org/bustime/api/v1/getstops";
	//	private static final String ACCESS_ID="cvTWAYXjbFEGcMSQbnv5tpteK";
	//private static final String line="P1";
	//private static final String stationID="3158,4833";

	//	public static final String SESSION_DIRECTION = "Direction";
	//	public static final String SESSION_STATION = "StationName";	
	//	public static final String SESSION_BUSLINE = "Route";

	private static  String SPEECH_NO_SUCH_STATION="I can't find that station. Please say again.";


	//	private static  String SPEECH_INSTRUCTIONS=
	//			//"I can lead you through providing a bus line, direction, and "
	//			//+ "bus stop to get departure information, "
	//			//+ "or you can simply open Port Authroity and ask a question like, "
	//			//+ "when is the next outbound P1 leaving sixth and smithfield. "
	//			//+ "For a list of supported buslines, ask what bus lines are supported. "
	//			//+ 
	//			SPEECH_WHICH_BUSLINE;

	private static  String SPEECH_WELCOME="Welcome to Pittsburgh Port Authority ";//+ SPEECH_WHICH_BUSLINE;

	private Map<String, DataHelper> dataHelpers;


	/**PUBLIC METHODS******************************/

	public SpeechletResponse onLaunch( LaunchRequest request,  Session session)
			throws SpeechletException {
		BasicConfigurator.configure();
		log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(),
				session.getSessionId());
		return newAskResponse(SPEECH_WELCOME+RouteHelper.SPEECH, RouteHelper.SPEECH);
	}

	public void onSessionStarted(SessionStartedRequest request, Session session)
			throws SpeechletException {
		log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(),
				session.getSessionId());
		this.dataHelpers=new HashMap<String, DataHelper>();//createDataHelpers(session);
		dataHelpers.put(RouteHelper.INTENT_NAME, DataHelperFactory.getHelper(session, RouteHelper.NAME));
		dataHelpers.put(BusStopHelper.INTENT_NAME, DataHelperFactory.getHelper(session, BusStopHelper.NAME));
		dataHelpers.put(DirectionHelper.INTENT_NAME, DataHelperFactory.getHelper(session, DirectionHelper.NAME));
	}


	public SpeechletResponse onIntent(IntentRequest request, Session session)
			throws SpeechletException {
		//	log.info("onIntent requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());

		SpeechletResponse furtherQuestions;
		Intent intent = request.getIntent();
		try {
			if (intent.getName().equals("OneshotBusIntent")){
				Iterator<DataHelper> itr = dataHelpers.values().iterator();
				while (itr.hasNext()){
					DataHelper element=itr.next();
					element.putValuesInSession(intent);
				}
			} else { //DirectionBusIntent {Direction} || RouteBusIntent {Route} || StationBusIntent {StationName}
				DataHelper dataHelper = dataHelpers.get(intent.getName());
				dataHelper.putValuesInSession(intent);
			}

			if ((furtherQuestions=checkForAdditionalQuestions(session))!=null){
				return furtherQuestions;
			}
			
			// OK, the user has entered everything, now let's find their response
			Map<String, String> input = getInputValuesFromSession();
			
			
			List<Message> stops= getMatchedBusStops(input);
			//log.info("Found "+stops.size()+ "matching stops");

			//if 0 ask again
			if (stops==null||stops.isEmpty()){
				return newAskResponse("I cannot find a stop that matches. "+BusStopHelper.SPEECH,BusStopHelper.SPEECH);
			}
			
			if (stops.size()>1){
				String speechOutput = "I found several stops that match. try specifying a cross street.";
				return newAskResponse(speechOutput+BusStopHelper.SPEECH,BusStopHelper.SPEECH);
			}
			
			//if 1 find answer and respond
			String stationID=stops.get(0).getStopID();
			String stationName=stops.get(0).getStopName();
			log.info("Station Name "+stationName+ " matched "+stationID);
			return getAnswer(stationID, stationName, input.get(DirectionHelper.NAME), input.get(RouteHelper.NAME));

			// get prediction for all stops
			
		} catch (Exception e) {
			log.debug("speechletExcpetion:"+e.getMessage());
			throw new SpeechletException(e.getMessage());
		}

	}

	public void onSessionEnded(SessionEndedRequest request, Session session)
			throws SpeechletException {

		log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(),
				session.getSessionId());
		this.dataHelpers.clear();
	}


	private SpeechletResponse checkForAdditionalQuestions(Session session) {
		logSession(session, "checkingForAdditionalQuestions");

		// Do I have all the data I need?
		Iterator<DataHelper> itr=dataHelpers.values().iterator();	
		while (itr.hasNext()){
			DataHelper element=itr.next();

			if (element.getValueFromSession()==null){
				log.info(element.getName()+":"+element.getValueFromSession()+"==null");
				return newAskResponse(session, element.getSpeech(), element.getSpeech()); 	
			} else {
				log.info(element.getName()+":"+element.getValueFromSession()+"!=null");
			}
		}

		return null;
	}

	public List<Message> getMatchedBusStops(Map<String, String> input){
		String matchString=input.get(BusStopHelper.NAME);
		//TODO: Refactor
		String apiString= TrueTimeMessageParser.STOPS_URL+"?key="+TrueTimeMessageParser.ACCESS_ID+"&rt="+input.get(RouteHelper.NAME)+"&dir="+input.get(DirectionHelper.NAME);
		log.info("apiString="+apiString);

		TrueTimeMessageParser apiResponse = new TrueTimeMessageParser(apiString);
		List<Message> stops=null;

		try {
			stops = apiResponse.parse();


			//log.info("Number of "+direction+" stops for the "+busline+" line is "+stops.size());
			Iterator<Message> iterator = stops.iterator();
			while (iterator.hasNext()){
				Message element=(Message)iterator.next();
				log.info("Trying to Match: "+element.getStopName().toUpperCase() + "with "+matchString);
				if (element.getStopName().toUpperCase().contains(matchString)){
					log.info("found one");
				}else{
					iterator.remove();
				}
			}

		} catch (IOException | SAXException | ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return stops;

	}

	private SpeechletResponse getAnswer(String stationID, String stationName, String direction,
			String busline) {

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
			String apiString= TrueTimeMessageParser.PREDICTION_URL+"?key="+TrueTimeMessageParser.ACCESS_ID+"&rt="+busline+"&stpid="+stationID;
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
									" is arriving at " + stationName +" <break time=\"0.1s\" /> now ";
						} else {
							speechOutput="An "+direction+" "+busline+ 
									" will be arriving at " + stationName + " in "+when+" minutes ";
						}
					} else {
						speechOutput=speechOutput+" <break time=\"0.25s\" /> and another in "+when+" minutes";
					}
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
		this.logSession(session, "newAskResponse");
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
		outputSpeech.setSsml(stringOutput );

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
