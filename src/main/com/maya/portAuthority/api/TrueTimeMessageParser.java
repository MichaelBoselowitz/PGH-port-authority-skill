package com.maya.portAuthority.api;

//import GetNextBusSpeechlet;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class TrueTimeMessageParser extends BaseAPIParser {
	private static  Logger LOGGER = LoggerFactory.getLogger(TrueTimeMessageParser.class);
	
  	public static final String PREDICTION_URL="http://truetime.portauthority.org/bustime/api/v1/getpredictions";
	public static final String STOPS_URL="http://truetime.portauthority.org/bustime/api/v1/getstops";
	public static final String ACCESS_ID="cvTWAYXjbFEGcMSQbnv5tpteK";
  	
  public TrueTimeMessageParser(String urlString) {
		super(urlString);
		LOGGER.info("constructor");
	}

  public List<Message> parse() 
		  throws IOException, SAXException, ParserConfigurationException {
          
           //Create a "parser factory" for creating SAX parsers
           SAXParserFactory spfac = SAXParserFactory.newInstance();

           //Now use the parser factory to create a SAXParser object
           SAXParser sp = spfac.newSAXParser();

           //Create an instance of this class; it defines all the handler methods
           TrueTimeHandler handler = new TrueTimeHandler();

           //Finally, tell the parser to parse the input and notify the handler
           sp.parse(super.getInputStream(), handler);

           return handler.getMessages();

    }


}
