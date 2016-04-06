package com.maya.portAuthority.api;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

public class TrueTimePredictionParser extends BaseAPIParser {
                  
  public TrueTimePredictionParser(String urlString) {
		super(urlString);
		// TODO Auto-generated constructor stub
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
