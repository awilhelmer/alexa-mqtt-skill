package de.awilhelmer.alexaskill.mqtt.handler;

import de.awilhelmer.alexaskill.mqtt.config.Config;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by WilhelmerA on 06.02.2017.
 */
public class MqttHandler {
   private static final Logger LOG = LoggerFactory.getLogger(MqttHandler.class);

   private static MqttHandler INSTANCE;

   private MqttClient mqtt;

   MqttConnectOptions options;

   private boolean init = false;

   private MqttHandler() {

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
         try {
            this.mqtt = new MqttClient(config.mqtt_skill.mqtt_host.hostname, "Alexa-Skill-Client", new MemoryPersistence());

            options = new MqttConnectOptions();

            if (config.mqtt_skill.mqtt_host.username != null && config.mqtt_skill.mqtt_host.username.length() > 0) {
               options.setUserName((config.mqtt_skill.mqtt_host.username));
               options.setPassword(config.mqtt_skill.mqtt_host.password.toCharArray());

            }

            mqtt.connect(options);
            init = true;
         }
         catch (Exception e) {
            LOG.error("Cannot resolve Host: ", e);
         }
      }
   }

   public synchronized boolean publish(String topic, String command) {
      if (isInit()) {
         if (!mqtt.isConnected()) {
            LOG.info(String.format("Establish MQTT Connection to host %s ", this.mqtt.getServerURI()));
            try {
               LOG.info("Waiting for MQTT Connection ... ");
               mqtt.connect(options);
            }
            catch (Exception e) {
               LOG.error("Error reestablishing connection to MQTT Broker", e);
            }
         }

         if (this.mqtt.isConnected()) {
            try {
               mqtt.publish(topic, command.getBytes(), 0, false);
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
