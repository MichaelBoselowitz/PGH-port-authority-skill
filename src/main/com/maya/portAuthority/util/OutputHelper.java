package com.maya.portAuthority.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.Card;
import com.amazon.speech.ui.Image;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import com.amazon.speech.ui.SsmlOutputSpeech;
import com.amazon.speech.ui.StandardCard;
import com.maya.portAuthority.GetNextBusSpeechlet;
import com.maya.portAuthority.googleMaps.Instructions;
import com.maya.portAuthority.googleMaps.NearestStopLocator;
import com.maya.portAuthority.storage.PaInputData;
import org.json.JSONObject;


public class OutputHelper {
	private final static Logger LOGGER = LoggerFactory.getLogger("OutputHelper");
	
	private static String SPEECH_WELCOME = "Welcome to "+GetNextBusSpeechlet.INVOCATION_NAME;

	public static final String AUDIO_WELCOME = "<audio src=\"https://s3.amazonaws.com/maya-audio/ppa_welcome.mp3\" />";
	private static final String AUDIO_FAILURE = "<audio src=\"https://s3.amazonaws.com/maya-audio/ppa_failure.mp3\" />";
	private static final String AUDIO_SUCCESS = "<audio src=\"https://s3.amazonaws.com/maya-audio/ppa_success.mp3\" />";
	
	//TODO: add markers into conversation
	private static final String CHANGE_MARKER=" <break time=\"0.5s\" /> By the way, ";
	private static final String SUCCESS_MARKER="okay, ";
	private static final String FAILED_MARKER="oh, ";
	
	/**
	 * Location Name, StopName
	 */
	private static final String LOCATION_SPEECH="The nearest stop to %s is %s. ";
	/**
	 * StopName
	 */
	private static final String BUSSTOP_SPEECH=" At %s, ";
	
	
	////RESULTS////
	/**
	 * Speech fragment if there are no prediction results for an "All Routes" request
	 * Format with Direction, BusStopName
	 */
	private static final String NO_ALL_ROUTES_SPEECH=" No %s busses are expected at %s in the next 30 minutes. ";
	
	/**
	 * Speech fragment if there are no prediction results for an "All Routes" request
	 * Format with Direction, RouteID, and BusStopName
	 */
	private static final String NO_SINGLE_ROUTE_SPEECH=" No %s, %s is expected at %s in the next 30 minutes. ";
	
	/**
	 * Speech fragment for first prediction result
	 * Format with RouteID, Prediction Time
	 */
	private static final String FIRST_RESULT_SPEECH=" The %s will be arriving in %s minutes ";
	
	/**
	 * Speech fragment for additional prediction result
	 * Format with Prediction Time
	 */
	private static final String MORE_RESULTS_SPEECH=" and %s minutes ";
	
	/**
	 * Speech fragment with instructions to hear all routes.
	 */
	private static final String HELP_ALL_ROUTES_SPEECH=CHANGE_MARKER+"to hear predictions for all routes that stop there, say <break time=\"0.25s\" /> Alexa, ask "+GetNextBusSpeechlet.INVOCATION_NAME+" for All Routes";
       

	//	public static SpeechletResponse getNoResponse(PaInputData inputData) {
	//		return getNoResponse(inputData, "");
	//	}

	public static SpeechletResponse getWelcome(){
		String output=AUDIO_WELCOME+" "+SPEECH_WELCOME + DataHelper.ROUTE_PROMPT;
		
		return newAskResponse(output, DataHelper.ROUTE_PROMPT);
	}

	public static SpeechletResponse getNoResponse(PaInputData inputData, SkillContext c) {
		SsmlOutputSpeech outputSpeech=new SsmlOutputSpeech();
		String textOutput="";
		if (c.needsLocation()){
			textOutput=String.format(LOCATION_SPEECH, inputData.getLocationName(), inputData.getStopName()); 
		}

		if (c.isAllRoutes()){
			textOutput+=String.format(NO_ALL_ROUTES_SPEECH, inputData.getDirection(), inputData.getStopName());
		} else {
			textOutput+=String.format(NO_SINGLE_ROUTE_SPEECH, inputData.getDirection(), inputData.getRouteID() , inputData.getStopName());
		}

		if ((c.needsMoreHelp())&&(!c.isAllRoutes())){
			textOutput+=HELP_ALL_ROUTES_SPEECH;
		}

		
		outputSpeech.setSsml("<speak> " + AUDIO_FAILURE + textOutput + "</speak>");
		return SpeechletResponse.newTellResponse(outputSpeech, buildCard(textOutput));

	}

	//	public static SpeechletResponse getResponse(PaInputData inputData, ArrayList<Result> results) {	
	//		return getResponse(inputData, results, "");
	//	}

	public static SpeechletResponse getResponse(PaInputData inputData, ArrayList<Result> results, SkillContext c) {	
		SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
		String textOutput;
		String speechOutput;

		//final String locationOutput="The nearest stop to "+  inputData.getLocationName() +" is " + inputData.getStopName()+". ";
		//final String stopOutput=" At " +inputData.getStopName()+", ";
		//final String allRoutesHelpText=" <break time=\"0.25s\" />  to hear predictions for all routes that stop "+BUSSTOP_SPEECH+  ", say <break time=\"0.25s\" /> Alexa, ask "+GetNextBusSpeechlet.INVOCATION_NAME+" for All Routes";


		if (c.needsLocation()){
			textOutput=String.format(LOCATION_SPEECH, inputData.getLocationName(), inputData.getStopName());
		} else {
			textOutput=String.format(BUSSTOP_SPEECH, inputData.getStopName()) ;
		}
		speechOutput = textOutput+"<break time=\"0.1s\" />";


		int when;
		String routeID;
		String prevRouteID=null;
		//TODO: Collect Route responses together, but Return the first bus first. 
		Collections.sort(results);

		for (int i = 0; i < results.size(); i++) {

			routeID=results.get(i).getRoute();
			when = results.get(i).getEstimate();

			if (i==0){
				textOutput += String.format(FIRST_RESULT_SPEECH,routeID,when);
				speechOutput += String.format(FIRST_RESULT_SPEECH,routeID,when);
			} else if (routeID.equals(prevRouteID)){
				textOutput += String.format(MORE_RESULTS_SPEECH, when);
				speechOutput += String.format(MORE_RESULTS_SPEECH, when);
			} else {
				textOutput += ".\n "+String.format(FIRST_RESULT_SPEECH,routeID,when);
				speechOutput += "<break time=\"0.25s\" /> "+String.format(FIRST_RESULT_SPEECH,routeID,when);
			}
			prevRouteID=routeID;
		}

//		if ((c.needsMoreHelp())&&(!c.isAllRoutes())){
//			speechOutput+=HELP_ALL_ROUTES_SPEECH;
//		}
		outputSpeech.setSsml("<speak> " + AUDIO_SUCCESS + speechOutput + "</speak>");
		Card card;
		
		try {
			card = buildCard(textOutput, inputData.getLocationLat(), inputData.getLocationLong(), inputData.getStopLat(), inputData.getStopLon());
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			card= buildCard(textOutput);
		}
		
		return SpeechletResponse.newTellResponse(outputSpeech, card);
	}
        
	//card for error output
	private static SimpleCard buildCard(String s){
		SimpleCard card=new SimpleCard();
		card.setTitle(GetNextBusSpeechlet.INVOCATION_NAME);
		card.setContent(s);
		return card;
	}
        //card with image for successful output
	private static StandardCard buildCard(String text, String locationLat, String locationLong, double stopLat, double stopLon) throws IOException, JSONException, Exception {
            StandardCard card = new StandardCard();
            Navigation navigation = buildNavigation(locationLat, locationLong, stopLat, stopLon);
            card.setTitle(GetNextBusSpeechlet.INVOCATION_NAME);
            card.setText(text+"\n"+navigation.getInstructions());
            Image image = new Image();
            image.setLargeImageUrl(navigation.getImage());
            card.setImage(image);
            return card;
        }

    private static Navigation buildNavigation(String locationLat, String locationLon, double stopLat, double stopLon) throws IOException, JSONException, Exception{	
    	Navigation navigation = new Navigation();
    	JSONObject json = NearestStopLocator.getDirections(locationLat, locationLon, stopLat, stopLon);
        String instructions = Instructions.getInstructions(json);
        String image = NearestStopLocator.buildImage(locationLat, locationLon, stopLat, stopLon) + Instructions.printWayPoints(json);
        image = image.substring(0, image.length() -1); //Remove the last '|'
        navigation.setInstructions(instructions);
        navigation.setImage(image);
        //LOGGER.info("IMAGE URL={}",image);
        return navigation;
    }


	/**
	 * Wrapper for creating the Ask response from the input strings.

	 * @param stringOutput
	 *            the output to be spoken
	 * @param repromptText
	 *            the reprompt for if the user doesn't reply or is
	 *            misunderstood.
	 * @return SpeechletResponse the speechlet response
	 */
	public static SpeechletResponse newAskResponse(String stringOutput, String repromptText) {
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

	public static SpeechletResponse newTellResponse(String message) {
		SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
		outputSpeech.setSsml("<speak> " + message + " </speak>");
		return SpeechletResponse.newTellResponse(outputSpeech);
	}
}
