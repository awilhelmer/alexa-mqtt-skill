/**
 * Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at
 * <p>
 * http://aws.amazon.com/apache2.0/
 * <p>
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package de.awilhelmer.alexaskill.mqtt;

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
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import com.typesafe.config.ConfigFactory;
import de.awilhelmer.alexaskill.mqtt.config.Config;
import de.awilhelmer.alexaskill.mqtt.handler.MqttHandler;
import de.awilhelmer.alexaskill.mqtt.model.Command;
import de.awilhelmer.alexaskill.mqtt.model.Device;
import de.awilhelmer.alexaskill.mqtt.model.DeviceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * This sample shows how to create a simple speechlet for handling speechlet requests.
 */
public class MqttSpeechlet implements Speechlet {
   private static final Logger LOG = LoggerFactory.getLogger(MqttSpeechlet.class);

   // TODO Many static inits here, cause the container will instantiate this class. Refactor it to an initializer class

   private static final String COLOR_KEY = "COLOR";

   private static final String COLOR_SLOT = "Color";

   private static final String DEVICE_KEY = "DEVICE";

   private static final String DEVICE_SLOT = "Device";

   private static final String COMMAND_KEY = "COMMAND";

   private static final String COMMAND_SLOT = "Command";

   private static final String VALUE_KEY = "VALUE";

   private static final String VALUE_SLOT = "Value";

   private static final Config CONFIG = new Config(ConfigFactory.load("config").resolve());

   private static final Map<String, Device> DEVICES = new HashMap<>();

   private static final Map<String, Command> COMMANDS = new HashMap<>();

   private ResourceBundle texts;

   static {
      LOG.info("Start static loading...");
      // Init MQTT
      MqttHandler.getInstance().initMqtt(CONFIG);
      // Init Configs
      initDevices();
      initCommands();

      LOG.info("Ended statc loading...");
   }

   private static void initCommands() {
      for (Config.Mqtt_skill.Commands.List$Elm list$Elm : CONFIG.mqtt_skill.commands.list) {
         Command command = new Command();
         command.setName(list$Elm.command.toUpperCase());
         command.setCommand(list$Elm.mqtt_command);
         command.setNumberValue(list$Elm.value);
         if (list$Elm.type != null) {
            command.setType(DeviceType.valueOf(list$Elm.type.toUpperCase()));
         }
         COMMANDS.put(command.getName(), command);
      }

   }

   private static void initDevices() {
      for (Config.Mqtt_skill.Devices.List$Elm2 deviceEntry : CONFIG.mqtt_skill.devices.list) {
         Device device = new Device();
         device.setName(deviceEntry.name.toUpperCase());
         device.setTopic(deviceEntry.topic);
         if (deviceEntry.type != null) {
            device.setType(DeviceType.valueOf(deviceEntry.type));
         }
         DEVICES.put(device.getName(), device);
      }

   }

   @Override
   public void onSessionStarted(final SessionStartedRequest request, final Session session) throws SpeechletException {
      LOG.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
      this.texts = ResourceBundle.getBundle("texts", request.getLocale());
      // any initialization logic goes here
   }

   @Override
   public SpeechletResponse onLaunch(final LaunchRequest request, final Session session) throws SpeechletException {
      LOG.info("onLaunch requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
      return getWelcomeResponse();
   }

   @Override
   public SpeechletResponse onIntent(final IntentRequest request, final Session session) throws SpeechletException {
      LOG.info("onIntent requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());

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

   @Override
   public void onSessionEnded(final SessionEndedRequest request, final Session session) throws SpeechletException {
      LOG.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
      // any cleanup logic goes here
   }

   private SpeechletResponse processMqttCommand(final Intent intent, final Session session) {
      String speechText;

      // Get the slots from the intent.
      Map<String, Slot> slots = intent.getSlots();

      // Get the slot information.
      Slot device = slots.get(DEVICE_SLOT);
      Slot command = slots.get(COMMAND_SLOT);

      speechText = checkValidation(device, command);
      if (speechText == null) {

         if (DEVICES.containsKey(device.getValue().toUpperCase())) {
            if (COMMANDS.containsKey(command.getValue().toUpperCase())) {
               Device deviceObj = DEVICES.get(device.getValue().toUpperCase());
               Command commandObj = COMMANDS.get(command.getValue().toUpperCase());
               if (commandObj.getType() != null && commandObj.getType() != deviceObj.getType()) {
                  speechText =  texts.getString("wrong-device");
               } else {
                  String mqttCommand = handleDeviceCommand(slots, commandObj);
                  if (mqttCommand != null) {
                     if (MqttHandler.getInstance().publish(deviceObj.getTopic(), mqttCommand)) {
                        speechText = texts.getString("okay");
                     } else {
                        speechText = texts.getString("tec-error");
                     }
                  } else {
                     speechText = texts.getString("command-notvalid");
                  }
               }

            } else {
               speechText = texts.getString("no-command");
            }
         } else {
            speechText = texts.getString("no-device");
         }

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

   private String handleDeviceCommand(Map<String, Slot> slots, Command commandObj) {
      String mqttCommand = String.format("%s", commandObj.getCommand());

      if (mqttCommand.equals("rgb()")) {
         mqttCommand = null;
         // Color command
         Slot color = slots.get(COLOR_SLOT);
         if (color != null && color.getValue() != null) {
            mqttCommand = getColorCommand(color.getValue());
         }
      } else {
         if (commandObj.getNumberValue() != null && commandObj.getNumberValue()) {
            Slot number = slots.get(VALUE_SLOT);
            if (number != null && number.getValue() != null && number.getValue().matches("\\d+")) {
               mqttCommand = mqttCommand.replace(":0", String.format(":%s", number.getValue()));
            } else {
               LOG.error(String.format("Cannot convert number %s", number != null ? number.getValue() : null));
               mqttCommand = null;
            }
         }
      }
      return mqttCommand;
   }

   private String getColorCommand(String rgbValue) {

      if (rgbValue.equals(texts.getString("green"))) {
         return "rgb(0,255,0)";
      }else if (rgbValue.equals(texts.getString("blue"))) {
         return "rgb(0,0,255)";
      }else if (rgbValue.equals(texts.getString("purple"))) {
         return "rgb(128,0,128)";
      }else if (rgbValue.equals(texts.getString("red"))) {
         return "rgb(255,0,0)";
      }else if (rgbValue.equals(texts.getString("orange"))) {
         return "rgb(255,69,0)";
      }else if (rgbValue.equals(texts.getString("yellow"))) {
         return "rgb(255,255,0)";
      }else if (rgbValue.equals(texts.getString("white"))) {
         return "rgb(255,255,255)";
      }else if (rgbValue.equals(texts.getString("warm"))) {
         return "rgb(255,244,229)";
      }
      return null;
   }

   private String checkValidation(Slot device, Slot command) {
      String result = null;
      if (device == null || device.getValue() == null) {
         result = texts.getString("no-device");
      } else if (command == null || command.getValue() == null) {
         result = String.format("%s %s?",texts.getString("what-device"), device.getValue());
      }
      return result;
   }

   /**
    * Creates and returns a {@code SpeechletResponse} with a welcome message.
    *
    * @return SpeechletResponse spoken and visual response for the given intent
    */
   private SpeechletResponse getWelcomeResponse() {
      String speechText = texts.getString("welcome");

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

      String speechText = texts.getString("help");
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
