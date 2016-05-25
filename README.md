# Pittsburgh Port Authority Alexa Skill
The Pittsburgh Port Authority Alexa Skill (PPAAS)  provides a voice interface to the Port Authority of Allegheny County’s TrueTime℠ System. TrueTime provides real-time vehicle tracking for Port Authority busses and trains and a publicly available API. 

## Sample Invocation
The wake word for the Kiva Echo is currently set to “Echo” and the Invocation Name for the PPAAS is set to “NextBus”. So in order to find out when the next “outbound P1” bus is leaving Smithfield and Sixth streets, you might say:
 “Echo, ask NextBus”
and follow the prompts. 

### Input
In order to determine when your bus will arrive, the skill needs three pieces of information: 
* Route (bus line)
* Direction (inbound or outbound), and
* StationName (bus stop) where you will get on the bus. 

The skill allows the user to say all of this information at once (OneshotBusIntent) with a statement like: 
* when is the {Direction} {Route} arriving at {StationName} Avenue
If the skill does not have all the information, or if it does not understand certain information, it will ask questions until it can provide an answer. Answers to these questions are individual intents triggered by statements like:
* RouteBusIntent when is the next {Route} bus
* StationBusIntent {StationName} Street, or 
* DirectionBusIntent {Direction}
The user can also specify the StationName by triggering a CrossStreetBusIntent providing cross streets with a statement like: 
*	{MainStreet} Street and {CrossStreet} Avenue

Note: The current implementation of the CrossStreetBusIntent ignores the MainStreet and attempts to match the CrossStreet. It works on the assumption that a regular rider will naturally specify the more general street first (e.g. if the bus runs down fifth street and stops at each cross, the regular rider has a tendancy to express the intersection as “Fifth Street and Morewood Avenue,” rather than “Morewood Avenue and Fifth Street”). 

### Output
The primary output is voice specified as Speech Synthesis Markup Language (SSML). This format allows the skill to specify audio clips (here music that implies success or failure) and specify how the voice expresses the string response (e.g. inserting silence).

A secondary output is an output card

## Deployment

### S3 Audio
In order to function, the PPAA skill requires three audio files to be available via AWS S3:
* https://s3.amazonaws.com/maya-audio/ppa_welcome.mp3 
* https://s3.amazonaws.com/maya-audio/ppa_failure.mp3
* https://s3.amazonaws.com/maya-audio/ppa_success.mp3
If these are not available, contact the skill’s author. 

### AWS Lambda
AWS Lambda is a serverless compute service that runs your code in response to events and automatically manages the underlying compute resources for you. The source code for an Alexa Skill is deployed to Lambda after you create an AWS Lambda function for a custom skill. 
After the AWS Lambda function is deployed and linked to the ASK skill, you can view the logs here in Lambda. 

### Alexa Skills Kit 
Configuring the ASK skill primarily occurs in the Amazon Developer’s Console for ASK. Here you define and test the interaction model and configuration of the skill. When it is ready for prime time, this is where the controls reside for publishing the skill. 

Interaction Model
The files needed to configure the interaction model reside in:
* /main/com/maya/portAuthority/speechAssets, and
* /main/com/maya/portAuthority/speechAssets/customSlotTypes
you can copy and paste the relevant information from those files and paste into the appropriate fields in the Developer Console: 
* Intent Schema
* Custom Slot Type: LIST_OF_BUS_ROUTES
* Custom Slot Type: LIST_OF_DIRECTIONS
* Customer Slot Type: LIST_OF_STOPS
* Sample Utterances


##Alexa Skills Kit Documentation
The documentation for the Alexa Skills Kit is available on the [Amazon Apps and Services Developer Portal](https://developer.amazon.com/appsandservices/solutions/alexa/alexa-skills-kit/).

## Questions
Please send questions to jonathan.h.brown@gmail.com
