# MQTT AWS Lambda function for Alexa

A simple [AWS Lambda](http://aws.amazon.com/lambda) function that sends commands via MQTT to your devices. Configurable MQTT Commands in https://github.com/awilhelmer/alexa-mqtt-skill/blob/master/src/main/resources/config.conf

### THIS IS A PERSONAL DEVELOPER SKILL. DO NOT SUBMIT THIS TO AMAZON FOR CERTIFICATION

## Concepts
This simple skill use the ask schema from amazon alexa. This isn't a smart home skill yet, but feel free to migrate it if u have an idea to link a
MQTT Broker with Account linking.


## Setup
To run this example skill you need to do two things. The first is to deploy the example code in lambda, and the second is to configure the Alexa skill to use Lambda.

### AWS Lambda Setup
1. Go to the AWS Console and click on the Lambda link. Note: ensure you are in us-east or you wont be able to use Alexa with Lambda.
2. Click on the Create a Lambda Function or Get Started Now button.
3. Skip the blueprint
4. Name the Lambda Function "Mqtt-Skill".
5. Select the runtime as Java 8
6. Use Gradle to build. Run the task "fatJar".
7. Select Code entry type as "Upload a .ZIP file" and then upload the "alexa-mqtt-skill-1.0-jar-with-dependencies.jar" file from the build directory to Lambda
8. Set the Handler as de.awilhelmer.alexaskill.mqtt.MqttSpeechletRequestStreamHandler (this refers to the Lambda RequestStreamHandler file in the zip).
9. Create a basic execution role and click create.
10. Leave the Advanced settings as the defaults.
11. Click "Next" and review the settings then click "Create Function"
12. Click the "Event Sources" tab and select "Add event source"
13. Set the Event Source type as Alexa Skills kit and Enable it now. Click Submit.
14. Copy the ARN from the top right to be used later in the Alexa Skill Setup.

### Alexa Skill Setup
1. Change/Add your devices in LIST_OF_DEVICES and add it in the config.conf. IMPORTANT!!! Configure commands and device names in your language! Don't use whitespaces in this names! See https://github.com/fusesource/mqtt-client for SSL Connections (ssl:// in host! Port in Host!) 
2. Go to the [Alexa Console](https://developer.amazon.com/edw/home.html) and click Add a New Skill.
3. Set "Mqtt" as the skill name and "broker" as the invocation name, this is what is used to activate your skill. For example you would say: "Alexa, ask broker <YOUR DEVICE> on" You can change the invocation name as you want.
4. Select the Lambda ARN for the skill Endpoint and paste the ARN copied from above. Click Next.
5. Copy the Intent Schema from the included IntentSchema.json.
6. Copy the Sample Utterances from the included SampleUtterances.txt. Click Next.
7. Go back to the skill Information tab and copy the appId. Paste the appId into the MqttRequestStreamHandler.java file for the variable supportedApplicationIds,
   then update the lambda source zip file with this change and upload to lambda again, this step makes sure the lambda function only serves request from authorized source.
8. You are now able to start testing your mqtt skill! You should be able to go to the [Echo webpage](http://echo.amazon.com/#skills) and see your skill enabled.
9. In order to test it, try to say some of the Sample Utterances from the Examples section below.
10. Your skill is now saved and once you are finished testing you can continue to publish your skill.

## Examples
### One-shot model:
    User: "Alexa, ask broker <device> <command> <value>"
    Alexa: "Okay"
