import de.awilhelmer.alexaskill.mqtt.MqttSpeechlet;
import de.awilhelmer.alexaskill.mqtt.handler.MqttHandler;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Alexander Wilhelmer
 */
public class TestMqttConnection {
   private static MqttSpeechlet speechlet;
   @BeforeClass
   public static void init() {
       speechlet = new MqttSpeechlet();

   }

   @Test
   public void testMqttConnection() {
      MqttHandler.getInstance().publish("kueche_led", "power:0");
   }

}
