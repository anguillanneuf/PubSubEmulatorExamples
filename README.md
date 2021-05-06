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


[Pub/Sub Emulator]: https://cloud.google.com/pubsub/docs/emulator
