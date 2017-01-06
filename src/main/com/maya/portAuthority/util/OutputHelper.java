package com.maya.portAuthority.util;

import java.util.ArrayList;
import java.util.Collections;

import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import com.amazon.speech.ui.SsmlOutputSpeech;
import com.maya.portAuthority.GetNextBusSpeechlet;
import com.maya.portAuthority.storage.PaInputData;

public class OutputHelper {
	private static String SPEECH_WELCOME = "Welcome to Pittsburgh Port Authority ";

	public static String AUDIO_WELCOME = "<audio src=\"https://s3.amazonaws.com/maya-audio/ppa_welcome.mp3\" />";
	private static String AUDIO_FAILURE = "<audio src=\"https://s3.amazonaws.com/maya-audio/ppa_failure.mp3\" />";
	private static String AUDIO_SUCCESS = "<audio src=\"https://s3.amazonaws.com/maya-audio/ppa_success.mp3\" />";


	//	public static SpeechletResponse getNoResponse(PaInputData inputData) {
	//		return getNoResponse(inputData, "");
	//	}

	public static SpeechletResponse getWelcome(){
		String output=AUDIO_WELCOME+" "+SPEECH_WELCOME + DataHelper.ROUTE_PROMPT;
		return newAskResponse(output, DataHelper.ROUTE_PROMPT);
	}

	public static SpeechletResponse getNoResponse(PaInputData inputData, SkillContext c) {

		final String locationOutput="The nearest stop to "+  inputData.getLocationName() +" is " + inputData.getStopName()+".";
		final String allRoutesOutput=" No " + inputData.getDirection() + ", busses are expected at " + inputData.getStopName() + " in the next 30 minutes ";
		final String singleRoutesOutput=" No " + inputData.getDirection() + ", " + inputData.getRouteID() + " is expected at " + inputData.getStopName() + " in the next 30 minutes  ";
		final String allRoutesHelpText=" <break time=\"0.25s\" />  to hear predictions for all routes that stop there, say <break time=\"0.25s\" /> Alexa, ask "+GetNextBusSpeechlet.INVOCATION_NAME+" for All Routes";

		String textOutput="";
		if (c.needsLocation()){
			textOutput=locationOutput;
		}

		if (c.isAllRoutes()){
			textOutput+=allRoutesOutput;
		} else {
			textOutput+=singleRoutesOutput;
		}

		if ((c.needsMoreHelp())&&(!c.isAllRoutes())){
			textOutput+=allRoutesHelpText;
		}

		SsmlOutputSpeech outputSpeech=new SsmlOutputSpeech();
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

		final String locationOutput="The nearest stop to "+  inputData.getLocationName() +" is " + inputData.getStopName()+" . ";
		final String stopOutput=" At " +inputData.getStopName()+", ";
		final String allRoutesHelpText=" <break time=\"0.25s\" />  to hear predictions for all routes that stop "+stopOutput+  ", say <break time=\"0.25s\" /> Alexa, ask "+GetNextBusSpeechlet.INVOCATION_NAME+" for All Routes";


		if (c.needsLocation()){
			textOutput=locationOutput;
		} else {
			textOutput=stopOutput;
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
				textOutput += " The " + routeID + " will be arriving in " + when + " minutes ";
				speechOutput += "The " + routeID + " will be arriving in " + when + " minutes ";
			} else if (routeID.equals(prevRouteID)){
				textOutput += " and "+when+ " minutes ";
				speechOutput += " and "+when+ " minutes ";
			} else {
				textOutput += ".\n The " + routeID + " will be arriving in " + when + " minutes ";
				speechOutput += "<break time=\"0.25s\" /> The " + routeID + " will be arriving in " + when + " minutes ";
			}
			prevRouteID=routeID;
		}

		if ((c.needsMoreHelp())&&(!c.isAllRoutes())){
			textOutput+=allRoutesHelpText;
		}
		outputSpeech.setSsml("<speak> " + AUDIO_SUCCESS + speechOutput + "</speak>");
		return SpeechletResponse.newTellResponse(outputSpeech, buildCard(textOutput));
	}
	/**
	 * @param s
	 * @return
	 */
	private static SimpleCard buildCard(String s){
		SimpleCard card=new SimpleCard();
		card.setTitle("Pittsburgh Port Authority");
		card.setContent(s);
		return card;
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
