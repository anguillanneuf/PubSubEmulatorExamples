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

1. Create a topic and subscription. Update the subscription to retain acked messages. Publish some messages.
   ```shell script
   mvn compile exec:java -Dexec.mainClass=CreateResources
   ```
1. Seek to the subscription creation time. Pull messages from the subscription. Re-run this command to replay messages repeatedly.
   ```shell script
   mvn compile exec:java -Dexec.mainClass=Subscribe
   ```
## Functions Framework

This is Python-focused.

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
