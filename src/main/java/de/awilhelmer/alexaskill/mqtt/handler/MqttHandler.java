package de.awilhelmer.alexaskill.mqtt.handler;

import de.awilhelmer.alexaskill.mqtt.config.Config;
import org.fusesource.mqtt.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;

/**
 * Created by WilhelmerA on 06.02.2017.
 */
public class MqttHandler {
   private static final Logger LOG = LoggerFactory.getLogger(MqttHandler.class);

   private static MqttHandler INSTANCE;

   private MQTT mqtt;

   private boolean init = false;

   private BlockingConnection connection;

   private MqttHandler() {
      this.mqtt = new MQTT();
   }

   public static MqttHandler getInstance() {
      if (INSTANCE == null) {
         LOG.info("Created new MqttHandler... ");
         INSTANCE = new MqttHandler();
      }
      return INSTANCE;
   }

   public synchronized void initMqtt(Config config) {
      if (!init) {
         mqtt.setClientId("Alexa-Skill-Client");
         try {
            mqtt.setHost(config.mqtt_skill.mqtt_host.hostname);

            if (config.mqtt_skill.mqtt_host.username != null && config.mqtt_skill.mqtt_host.username.length() > 0) {
               mqtt.setUserName(config.mqtt_skill.mqtt_host.username);
               mqtt.setPassword(config.mqtt_skill.mqtt_host.password);
            }

            connection = mqtt.blockingConnection();
            init = true;
         }
         catch (URISyntaxException e) {
            LOG.error("Cannot resolve Host: ", e);
         }
      }
   }

   public boolean publish(String topic, String command) {
      if (isInit()) {
         if (!this.connection.isConnected()) {
            LOG.info(String.format("Establish MQTT Connection to host %s and user %s ", this.mqtt.getHost(), this.mqtt.getUserName()));
            try {
               LOG.info("Waiting for MQTT Connection ... ");
               this.connection.connect();
            }
            catch (Exception e) {
               LOG.error("Error reestablishing connection to MQTT Broker", e);
            }
         }

         if (this.connection.isConnected()) {
            try {
               this.connection.publish(topic, command.getBytes(), QoS.AT_LEAST_ONCE, false);
               return true;

            }
            catch (Exception e) {
               LOG.error("Error on publish msg: ", e);
            }
         } else {
            LOG.error("Connection failed, Command not send...");
         }
      } else {
         LOG.error("MQTT Init failed ...");
      }

      return false;
   }

   public boolean isInit() {
      return init;
   }

}
