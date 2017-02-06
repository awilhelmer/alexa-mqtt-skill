package de.awilhelmer.alexaskill.mqtt.model;

/**
 * Created by WilhelmerA on 06.02.2017.
 */
public class Command {
   private String name;
   private String command;
   private Boolean numberValue;
   private DeviceType type;

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getCommand() {
      return command;
   }

   public void setCommand(String command) {
      this.command = command;
   }

   /**
    * Define that the MQTT-Command map a number value
    * @return
    */
   public Boolean getNumberValue() {
      return numberValue;
   }

   public void setNumberValue(Boolean numberValue) {
      this.numberValue = numberValue;
   }

   public DeviceType getType() {
      return type;
   }

   public void setType(DeviceType type) {
      this.type = type;
   }
}
