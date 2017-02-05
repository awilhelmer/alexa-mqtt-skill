/**
    Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.

    Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at

        http://aws.amazon.com/apache2.0/

    or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package de.awilhelmer.alexaskill.mqtt;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.*;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import com.typesafe.config.ConfigFactory;
import de.awilhelmer.alexaskill.mqtt.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * This sample shows how to create a simple speechlet for handling speechlet requests.
 */
public class MqttSpeechlet implements Speechlet {
    private static final Logger log = LoggerFactory.getLogger(MqttSpeechlet.class);

    private static final String COLOR_KEY = "COLOR";
    private static final String COLOR_SLOT = "Color";

    private static final String DEVICE_KEY = "DEVICE";
    private static final String DEVICE_SLOT = "Device";

    private static final String COMMAND_KEY = "COMMAND";
    private static final String COMMAND_SLOT = "Command";

    private static final String VALUE_KEY = "VALUE";
    private static final String VALUE_SLOT = "Value";

    private static final Config CONFIG = new Config(ConfigFactory.load("config").resolve());

    @Override
    public void onSessionStarted(final SessionStartedRequest request, final Session session)
            throws SpeechletException {
        log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());

        // any initialization logic goes here
    }

    @Override
    public SpeechletResponse onLaunch(final LaunchRequest request, final Session session)
            throws SpeechletException {
        log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        return getWelcomeResponse();
    }

    @Override
    public SpeechletResponse onIntent(final IntentRequest request, final Session session)
            throws SpeechletException {
        log.info("onIntent requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());

        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;

        if ("MqttIntent".equals(intentName)) {
            return processMqttCommand(intent, session);
        } else if ("AMAZON.HelpIntent".equals(intentName)) {
            return getHelpResponse();
        } else {
            throw new SpeechletException("Invalid Intent");
        }
    }

    private SpeechletResponse processMqttCommand(final Intent intent, final Session session) {
        String speechText = null;

        // Get the slots from the intent.
        Map<String, Slot> slots = intent.getSlots();

        // Get the color slot from the list of slots.
        Slot device = slots.get(DEVICE_SLOT);
        Slot command = slots.get(COMMAND_SLOT);

        speechText = checkValidation(device, command);
        if (speechText == null) {



            speechText = "Okay";
        }

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Mqtt-Command");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        return SpeechletResponse.newTellResponse(speech, card);

    }

    private String checkValidation(Slot device, Slot command) {
        String result = null;
        if (device == null || device.getValue() == null) {
            result = "Never heard about that device!";
        }
        if (command == null || command.getValue() == null) {
            result = "I can't find your command. Sorry!";
        }
        return result;
    }

    @Override
    public void onSessionEnded(final SessionEndedRequest request, final Session session)
            throws SpeechletException {
        log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        // any cleanup logic goes here
    }

    /**
     * Creates and returns a {@code SpeechletResponse} with a welcome message.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getWelcomeResponse() {
        String speechText = "Welcome to Mqtt Command control. Please tell me a device and a command.";

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("MQTT");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        // Create reprompt
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }


    /**
     * Creates a {@code SpeechletResponse} for the help intent.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getHelpResponse() {
        String speechText = "You can say: Send device command. Optional you can say a command value like a number or color.";

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Mqtt Help");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        // Create reprompt
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }
}
