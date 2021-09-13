# Pub/Sub Emulator Examples

This repo shows some examples that use the [Pub/Sub Emulator].

## Starting the emulator
1. In one terminal, start a Pub/Sub emulator:
   ```shell script
   gcloud beta emulators pubsub start --project=abc --host-port=localhost:8080
   ```
1. In another terminal, set environment variables used by the emulator.
   ```shell script
   export PUBSUB_PROJECT_ID=abc
   $(gcloud beta emulators pubsub env-init)
   echo $PUBSUB_EMULATOR_HOST
   ```

## Replaying messages

Known issue: https://github.com/googleapis/java-pubsub/issues/584

In Cloud SDK release `341.0.0` and later, the following steps will work if you comment out https://github.com/anguillanneuf/PubSubEmulatorExamples/blob/bcab38a0b1031448c3b7842737e47d13edbe68fe/src/main/java/CreateResources.java#L40. In the earlier releases, you must uncomment it for the following steps to work.
 
1. Create a topic and subscription. Update the subscription to retain acked messages. Publish some messages.
   ```shell script
   mvn compile exec:java -Dexec.mainClass=CreateResources
   ```
1. Seek to the subscription creation time. Pull messages from the subscription. Re-run this command to replay messages repeatedly.
   ```shell script
   mvn compile exec:java -Dexec.mainClass=Subscribe
   ```
## Functions Framework

### Java

1. In a first terminal, start the [Pub/Sub emulator] on port 8043.
   ```sh
   gcloud beta emulators pubsub start \
     --project=abc \
     --host-port=localhost:8043
   ```

1. In a second terminal, initialize the environment variable `PUBSUB_EMULATOR_HOST` to be `8043`.
   ```sh
   $(gcloud beta emulators pubsub env-init)
   ```

1. In the second terminal, clone this repo, then run a script to create a Pub/Sub topic "may" and attach a push subscription "five" to the topic, using `http://localhost:8082` as its push endpoint. You may need to comment out some lines in the main class of `CreateResources.java`.
   ```sh
   git clone https://github.com/anguillanneuf/PubSubEmulatorExamples.git
   mvn clean compile exec:java -Dexec.mainClass=CreateResources
   ```

1. In a third terminal, download `functions-framework-java`:
   ```sh
   git clone https://github.com/GoogleCloudPlatform/functions-framework-java.git
   ```

1. In order to use the Java Function Invoker, you can either include the package as a dependency in your POM file or use the Invoker's jar directly.
   
   1. If you choose to build the `java-function-invoker` JAR, follow these commands to build the `1.0.3-SNAPSHOT` version.
      ```sh
      cd ../invoker
      mvn dependency:copy \
        -Dartifact='com.google.cloud.functions.invoker:java-function-invoker:1.0.3-SNAPSHOT' \
        -DoutputDirectory=.
      ```
      Next, give the JAR a quick test.
      1. Copy the `java-function-invoker` JAR to the `PubSubEmulatorExamples` directory.
      1. Package the examples JAR.
      1. Try a `HelloWorld` example.
      ```sh
       mvn clean package
       java -jar java-function-invoker-1.0.3-SNAPSHOT.jar \
          --classpath target/emulator-1.0-SNAPSHOT.jar \
          --target HelloWorld
      ```
      You should see "Hello, World" on the page when you visit localhost:8080. Press `Ctrl+C` to abort.
   
   1. Since the [POM file](pom.xml) in this repo already includes `function-maven-plugin` as a dependency, you can also try the `HelloWord` example using this command: 
      ```sh
      mvn com.google.cloud.functions:function-maven-plugin:0.9.8-SNAPSHOT:run \
        -Drun.functionTarget=HelloWorld
      ```
      You should see "Hello, World" on the page when you visit localhost:8080. Press `Ctrl+C` to abort.
   
1. In the second terminal again, try a background function on port 8082:
   ```sh
    mvn com.google.cloud.functions:function-maven-plugin:0.9.8-SNAPSHOT:run -Drun.functionTarget=Background -Drun.port=8082
   ```

1. In the third terminal, invoke the background function with:
   ```sh
    curl -X POST "localhost:8082" -H 'Content-Type: application/json' -d @event.json
   ```
   You should see the output in the second terminal as follows:
   ```none
   INFO: Received JSON object: {"@type":"type.googleapis.com/google.pubsub.v1.PubsubMessage","data":"eyJmb28iOiJiYXIifQ==","attributes":{"test":"123"}}
   ```
   Press `Ctrl+C` to abort.

1. Now in your second terminal, try a Pub/Sub background function on port 8082.
   ```sh
    mvn com.google.cloud.functions:function-maven-plugin:0.9.8-SNAPSHOT:run \
     -Drun.functionTarget=PubSubBackground \
     -Drun.port=8082
   ```

1. In your third terminal, invoke the background function by publishing a message:
   ```sh
   mvn clean compile exec:java -Dexec.mainClass=Publish
   ```
   You should see the output in the second terminal as follows:
   ```none
   INFO: Received message with id 1
   ```
   You can publish again and again to see the event ID increaes. Press `Ctrl+C` to abort.
   Functionally, publishing a message works the same as directly curling the endpoint: 
   ```sh
    curl -X POST "localhost:8082" -H 'Content-Type: application/json' -d @message.json  
   ```

### Python

1. Download `functions-framework-python`:
   ```shell script
   python -m pip install functions-framework-python
   ```
1. Provide a simple function. See `functions-framework-python` [README.md](https://github.com/GoogleCloudPlatform/functions-framework-python).

1. Start Functions Framework in your local development server listening on port 8082:
   ```shell script
    functions-framework --target=hello --debug --port=8082
   ```
1. Start the Pub/Sub emulator.
   ```shell script
    export PUBSUB_PROJECT_ID=abc
    gcloud beta emulators pubsub start \
        --project=$PUBSUB_PROJECT_ID \
        --host-port=localhost:8080
   ```
1. Create a Pub/Sub topic and attach a push subscription to the topic, using `http://localhost:8082` as its push endpoint. Publish some messages to the topic. Observe your function getting triggered by Pub/Sub messages.
   ```sh
    export TOPIC_ID_=may
    export PUSH_SUBSCRIPTION_ID=five
    $(gcloud beta emulators pubsub env-init)
    git clone https://github.com/googleapis/python-pubsub.git
    cd python-pubsub/samples/snippets/
    python publisher.py $PUBSUB_PROJECT_ID create $TOPIC_ID
    python subscriber.py $PUBSUB_PROJECT_ID create $TOPIC_ID $PUSH_SUBSCRIPTION_ID http://localhost:8082
    python publisher.py $PUBSUB_PROJECT_ID publish $TOPIC_ID
   ```

[Pub/Sub Emulator]: https://cloud.google.com/pubsub/docs/emulator
