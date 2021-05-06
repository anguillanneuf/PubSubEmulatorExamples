import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.cloud.pubsub.v1.SubscriptionAdminSettings;
import com.google.protobuf.Duration;
import com.google.protobuf.Timestamp;
import com.google.pubsub.v1.GetSubscriptionRequest;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.SeekRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Subscribe {
  /*
    mvn clean compile exec:java -Dexec.mainClass=Subscribe
  * */
  public static void main(String... args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "abc";
    String subscriptionId = "six";

    subscribeAsyncExample(projectId, subscriptionId);
  }

  public static void subscribeAsyncExample(String projectId, String subscriptionId) {

    String hostport = System.getenv("PUBSUB_EMULATOR_HOST");
    ManagedChannel channel = ManagedChannelBuilder.forTarget(hostport).usePlaintext().build();
    TransportChannelProvider channelProvider =
        FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel));
    CredentialsProvider credentialsProvider = NoCredentialsProvider.create();

    ProjectSubscriptionName subscriptionName =
        ProjectSubscriptionName.of(projectId, subscriptionId);

    // Instantiate an asynchronous message receiver.
    MessageReceiver receiver =
        (PubsubMessage message, AckReplyConsumer consumer) -> {
          // Handle incoming message, then ack the received message.
          System.out.println("Id: " + message.getMessageId());
          System.out.println("Data: " + message.getData().toStringUtf8());
          consumer.ack();
        };

    try (SubscriptionAdminClient subscriptionAdminClient =
        SubscriptionAdminClient.create(
            SubscriptionAdminSettings.newBuilder()
                .setTransportChannelProvider(channelProvider)
                .setCredentialsProvider(credentialsProvider)
                .build())) {

      Duration messageRetentionDuration =
          subscriptionAdminClient
              .getSubscription(
                  GetSubscriptionRequest.newBuilder()
                      .setSubscription(subscriptionName.toString())
                      .build())
              .getMessageRetentionDuration();

      LocalDateTime retentionTime =
          LocalDateTime.now().minusSeconds(messageRetentionDuration.getSeconds());

      Timestamp retentionTimeEpoch =
          Timestamp.newBuilder()
              .setSeconds(retentionTime.atZone(ZoneOffset.UTC).toEpochSecond())
              .build();

      SeekRequest request =
          SeekRequest.newBuilder()
              .setSubscription(subscriptionName.toString())
              .setTime(retentionTimeEpoch)
              .build();

      subscriptionAdminClient.seek(request);
    } catch (IOException e) {
      System.out.println("Cannot replay messages");
    }

    Subscriber subscriber = null;
    try {
      subscriber =
          Subscriber.newBuilder(subscriptionName, receiver)
              .setChannelProvider(channelProvider)
              .setCredentialsProvider(credentialsProvider)
              .build();
      subscriber.startAsync().awaitRunning();
      System.out.printf("Listening for messages on %s:\n", subscriptionName.toString());
      subscriber.awaitTerminated(30, TimeUnit.SECONDS);
    } catch (TimeoutException timeoutException) {
      subscriber.stopAsync();
    }
  }
}
