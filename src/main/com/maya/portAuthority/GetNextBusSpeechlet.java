package com.maya.portAuthority;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.lambda.runtime.Context; 
import com.amazonaws.services.lambda.runtime.LambdaLogger;
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
import com.maya.portAuthority.api.TrueTimePredictionParser;

public class GetNextBusSpeechlet implements Speechlet {
	private static  Logger log = LoggerFactory.getLogger(GetNextBusSpeechlet.class);
	private static final String API_URL="http://truetime.portauthority.org/bustime/api/v1/getpredictions";
	private static final String ACCESS_ID="cvTWAYXjbFEGcMSQbnv5tpteK";
	private static final String line="P1";
	private static final String stationID="8161";

	private static  String SLOT_STATION = "StationName";
	private static  String SLOT_DIRECTION = "Direction";
	private static  String SLOT_BUSLINE = "BusLine";

	private static  String SESSION_STATION = "StationName";
	private static  String SESSION_STATION_ID= "StationID";
	private static  String SESSION_DIRECTION = "Direction";
	private static  String SESSION_BUSLINE = "BusLine";

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

	private static  String SPEECH_WELCOME="Welcome to Pittsburgh Port Authority <break time=\"1s\" />"+ SPEECH_WHICH_BUSLINE;


	/**PUBLIC METHODS******************************/

	@Override
	public SpeechletResponse onLaunch( LaunchRequest request,  Session session)
			throws SpeechletException {
		log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(),
				session.getSessionId());
		return newAskResponse(SPEECH_WELCOME, SPEECH_WHICH_BUSLINE);
	}

	@Override
	public void onSessionStarted(SessionStartedRequest request, Session session)
			throws SpeechletException {
		log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(),
				session.getSessionId());
		// TODO any initialization logic goes here

	}
	@Override
	public SpeechletResponse onIntent( IntentRequest request, Session session)
			throws SpeechletException {
		//log.info("onIntent requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());

		Intent intent = request.getIntent();
		String intentName = (intent != null) ? intent.getName() : null; 
		log.info("onIntent:"+intent.getName());

		//TODO Replace with Factory?
		//if ("OneshotBusIntent".equals(intentName)) {
		// 	return getNextBusSpeechletResponse(intent, session);
		// } else 
		if (("OneshotBusIntent".equals(intentName))||
				("DirectionBusIntent".equals(intentName)||
						"BusLineBusIntent".equals(intentName)||
						"StationBusIntent".equals(intentName))) {
			try {
				String stationName=getValueFromSession(session,SESSION_STATION);
				String stationID=getValueFromSession(session,SESSION_STATION_ID);
				String direction=getValueFromSession(session,SESSION_DIRECTION);
				String busline=getValueFromSession(session,SESSION_BUSLINE);

				log.debug("... with Session station:"+stationName+", stationID:"+stationID+",direction:"+direction+", busline:"+busline);

				putValuesInSession(intent, session);
				SpeechletResponse response= handleDialog(session);

				stationName=getValueFromSession(session,SESSION_STATION);
				stationID=getValueFromSession(session,SESSION_STATION_ID);
				direction=getValueFromSession(session,SESSION_DIRECTION);
				busline=getValueFromSession(session,SESSION_BUSLINE);

				log.debug("... returning response with Session station:"+stationName+", stationID:"+stationID+",direction:"+direction+", busline:"+busline);


				return response;
			} catch (Exception e) {
				log.debug("speechletExcpetion:"+e.getMessage());
				throw new SpeechletException(e.getMessage());
			}
		} else {
			log.debug("invalidIntent:");
			throw new SpeechletException("Invalid Intent");
		}
	}
	/*
            } else {
            	if ("SupportedLinesIntent".equals(intentName)) {
                	return getSupportedLinesResponse(session);
                } else {
                	if ("HelpIntent".equals(intentName)) {
                		return getHelpResponse(session);
                	} else {
                		throw new SpeechletException("Invalid Intent");
                	}
                }
            }

        }
        //return getWelcomeResponse();
	}*/

	@Override
	public void onSessionEnded(SessionEndedRequest request, Session session)
			throws SpeechletException {

		log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(),
				session.getSessionId());
		// TODO cleanup goes here.
	}

	private SpeechletResponse getNextBusSpeechletResponse( Intent intent,  Session session) {
		log.info("getNextBusSpeechletResponse() handling intent: "+intent.getName()+"...");
		//input from intent

		String stationName = (intent.getSlot(SLOT_STATION) != null) ? intent.getSlot(SLOT_STATION).getValue(): null; 
		String directionName = (intent.getSlot(SLOT_DIRECTION) != null) ? intent.getSlot(SLOT_DIRECTION).getValue(): null; 
		String buslineName = (intent.getSlot(SLOT_BUSLINE) != null) ? intent.getSlot(SLOT_BUSLINE).getValue(): null;
		log.info("... with station:"+stationName+", direction:"+directionName+", busline:"+buslineName);
		//output to speech
		String outputText="";
		String repromptText="";

		//BusStation busStation=null;

		// If I have all the information
		if ((stationName!=null)&&(directionName!=null)&&(buslineName!=null)){
			try {
				//busStation = new BusStation(stationName);
			} catch (Exception e) {
				outputText="I don't know station " +stationName;
				repromptText="What Station?";
				//return newAskResponse (outputText, repromptText);
				return newAskResponse(SPEECH_NO_SUCH_STATION+ SPEECH_WHICH_STATION, SPEECH_WHICH_STATION);
			}
			return getAnswer(stationName, directionName, buslineName);

			//Otherwise save what information I have and ask for more
		} else {
			if (stationName==null){
				// initialize Station Question
				outputText=SPEECH_WHICH_STATION;
				repromptText=SPEECH_WHICH_STATION;
			} else {
				session.setAttribute(SESSION_STATION, stationName); 
			}

			if (directionName==null){
				outputText=SPEECH_WHICH_DIRECTION;
				repromptText=SPEECH_WHICH_DIRECTION;
			} else {
				session.setAttribute(SESSION_DIRECTION, directionName); 
			}

			if (buslineName==null){
				outputText=SPEECH_WHICH_BUSLINE;
				repromptText=SPEECH_WHICH_BUSLINE;
			} else {
				session.setAttribute(SESSION_BUSLINE, buslineName); 
			}

			return newAskResponse (outputText, repromptText); 

		}



	}

	private String getValueFromIntentSlot( Intent intent,  String slotName){
		Slot slot = intent.getSlot(slotName);
		return (slot!=null) ? slot.getValue() : null;
	}

	private String getValueFromSession( Session session,  String name){
		if (session.getAttributes().containsKey(name)) {
			return (String) session.getAttribute(name);
		} else {
			return null;
		}
	}

	private void putValuesInSession(Intent intent, Session session) throws Exception{
		log.debug("putValuesInSession" );
		//user supplied station
		String stationName=getValueFromIntentSlot(intent,SLOT_STATION);
		String direction=getValueFromIntentSlot(intent,SLOT_DIRECTION);
		String busline=getValueFromIntentSlot(intent,SLOT_BUSLINE);
		BusStation station=null;
		log.debug("... with Slot station:"+stationName+", direction:"+direction+", busline:"+busline);


		if (stationName!=null){
			station=new BusStation(stationName);
			log.debug("putting value in session Slot station:"+stationName);
			session.setAttribute(SESSION_STATION, station.name); 
			session.setAttribute(SESSION_STATION_ID, station.ID);	
		} 
		if (direction!=null){
			log.debug("putting value in sesion Slot direction:"+direction);
			session.setAttribute(SESSION_DIRECTION, direction);
		}
		if (busline!=null){
			log.debug("putting value in sesion Slot busline:"+busline);
			session.setAttribute(SESSION_BUSLINE,busline);
		}

		String sessionStationName=getValueFromSession(session,SESSION_STATION);
		String sessionStationID=getValueFromSession(session,SESSION_STATION_ID);
		String sessionDirection=getValueFromSession(session,SESSION_DIRECTION);
		String sessionBusline=getValueFromSession(session,SESSION_BUSLINE);

		log.debug("...existing with Session station:"+sessionStationName+", direction:"+sessionDirection+", busline:"+sessionBusline);
	}

	private SpeechletResponse handleDialog(Session session) {
		log.info("handling dialog() ...");

		String stationName=getValueFromSession(session,SESSION_STATION);
		String stationID=getValueFromSession(session,SESSION_STATION_ID);
		String direction=getValueFromSession(session,SESSION_DIRECTION);
		String busline=getValueFromSession(session,SESSION_BUSLINE);

		log.debug("... with Session station:"+stationName+", stationID:"+stationID+", direction:"+direction+", busline:"+busline);

		/////
		if ((stationName==null)||(stationID==null)) {
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
				log.debug("Looking for Answer");
				return getAnswer(stationName, direction, busline);
				//return getAnswer(BusStation.getBusStation(stationName), direction, busline);
			} catch (Exception e) {
				String outputText="I don't know station in session" + e.getMessage();
				return newAskResponse (session, outputText+ SPEECH_WHICH_STATION, SPEECH_WHICH_STATION); 			
			}
		}
	}

	/*
		///
		void station getStationFromName(String stationName){
		if (stationName!=null){
			try{
				station=new BusStation(stationName);
				session.setAttribute(SESSION_STATION, station.name); 
				session.setAttribute(SESSION_STATION_ID, station.ID);
				//if direction is not in session, query for direction
				//if busline is not in session, query for busline
				//otherwise- getanswer
			}catch (Exception e){
				//e.printStackTrace();
				String speechOutput = "For which station would you like bus arrival times?";
				return newAskResponse("I cannot find station"+e.getMessage() +". "+speechOutput,speechOutput);
			}
		}


		}



		return getAnswer(station.ID, direction, busline);
	}
	 */
	private SpeechletResponse getAnswer(String station, String direction,
			String busline) {
		SimpleCard card = new SimpleCard();
		List<Message> messages;
		int when;
		log.info("getAnswer... with station:");
		//SsmlOutputSpeech outputSpeech= new SsmlOutputSpeech();
		PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();


		if ((station==null)||(direction==null)||(busline==null)){
			//outputSpeech.setSsml("I forgot what you told me");
			outputSpeech.setText("I forgot what you told me");
			return SpeechletResponse.newTellResponse(outputSpeech, new SimpleCard());
		}
		log.info(station+", direction:"+direction+", busline:"+busline);

		try { 
			String apiString= API_URL+"?key="+ACCESS_ID+"&rt="+line+"&stpid="+stationID;
			log.debug("apitString="+apiString);

			TrueTimePredictionParser tester = new TrueTimePredictionParser(apiString);
			messages=tester.parse();
			log.debug("messages size"+messages.size());

			//Define speech output
			String speechOutput = "";

			if (messages.size()==0){
				speechOutput=" No "+direction+", "+ busline +" is expected at " + station + " in the next 30 minutes  ";
				
			} else { 

				for (int i=0;i<messages.size();i++){
					busline=messages.get(i).getRoute();
					when=messages.get(i).getEstimate();
					if (when < 3){
						speechOutput=speechOutput+"An "+direction+" "+busline+ 
								" is arriving at " + station + "now ";
					} else {
						speechOutput=speechOutput+"An "+direction+" "+busline+ 
								" will be arriving at " + station + " in "+when+" minutes ";
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

}
