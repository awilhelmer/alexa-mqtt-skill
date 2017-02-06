package de.awilhelmer.alexaskill.mqtt.model;

/**
 * Created by WilhelmerA on 06.02.2017.
 */
public class Device {
   private String name;
   private String topic;
   private DeviceType type;

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getTopic() {
      return topic;
   }

   public void setTopic(String topic) {
      this.topic = topic;
   }

   public DeviceType getType() {
      return type;
   }

   public void setType(DeviceType type) {
      this.type = type;
   }
}
