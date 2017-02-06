import de.awilhelmer.alexaskill.mqtt.MqttSpeechlet;
import de.awilhelmer.alexaskill.mqtt.handler.MqttHandler;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

/**
 * @author Alexander Wilhelmer
 */
public class TestMqttConnection {
   private static MqttSpeechlet speechlet;
   @BeforeClass
   public static void init() {
       speechlet = new MqttSpeechlet();

   }

   /**
    * Test if Let's Encrypt is valid. Update your JDK if it isn't
    */
   @Test
   public void testLetsEncrypt()  {
      boolean error = true;
      try {
         new URL("https://letsencrypt.org/").openConnection().connect();
         error = false;
      }
      catch (IOException e) {
         e.printStackTrace();
      }
      assert (!error);
   }


   @Test
   public void testMqttConnection() {
      MqttHandler.getInstance().publish("kueche_led", "power:1");
   }

}
