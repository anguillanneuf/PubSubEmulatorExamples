import com.google.api.core.ApiFuture;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.AlreadyExistsException;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.cloud.pubsub.v1.SubscriptionAdminSettings;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminSettings;
import com.google.protobuf.ByteString;
import com.google.protobuf.FieldMask;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.Subscription;
import com.google.pubsub.v1.Topic;
import com.google.pubsub.v1.TopicName;
import com.google.pubsub.v1.UpdateSubscriptionRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class CreateResources {
  /*
   mvn clean compile exec:java -Dexec.mainClass=CreateResources
  * */

  public static void main(String... args) throws Exception {
    String projectId = "abc";
    String topicId = "may";
    String subscriptionId = "six";

    createTopicExample(projectId, topicId);
    createPullSubscriptionExample(projectId, subscriptionId, topicId);
    updatePullSubscriptionExample(projectId, subscriptionId, topicId);
    publisherExample(projectId, topicId);
  }

  public static void createTopicExample(String projectId, String topicId) throws IOException {
    String hostport = System.getenv("PUBSUB_EMULATOR_HOST");
    ManagedChannel channel = ManagedChannelBuilder.forTarget(hostport).usePlaintext().build();
    TransportChannelProvider channelProvider =
        FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel));
    CredentialsProvider credentialsProvider = NoCredentialsProvider.create();

    try (TopicAdminClient topicAdminClient =
        TopicAdminClient.create(
            TopicAdminSettings.newBuilder()
                .setTransportChannelProvider(channelProvider)
                .setCredentialsProvider(credentialsProvider)
                .build())) {
      TopicName topicName = TopicName.of(projectId, topicId);
      try {
        Topic topic = topicAdminClient.createTopic(topicName);
        System.out.println("Created topic: " + topic.getName());
      } catch (AlreadyExistsException | StatusRuntimeException e) {
        System.out.println(topicName + " already exits.");
      }
    }
  }

  public static void createPullSubscriptionExample(
      String projectId, String subscriptionId, String topicId) throws IOException {
    String hostport = System.getenv("PUBSUB_EMULATOR_HOST");
    ManagedChannel channel = ManagedChannelBuilder.forTarget(hostport).usePlaintext().build();
    TransportChannelProvider channelProvider =
        FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel));
    CredentialsProvider credentialsProvider = NoCredentialsProvider.create();

    try (SubscriptionAdminClient subscriptionAdminClient =
        SubscriptionAdminClient.create(
            SubscriptionAdminSettings.newBuilder()
                .setTransportChannelProvider(channelProvider)
                .setCredentialsProvider(credentialsProvider)
                .build())) {
      TopicName topicName = TopicName.of(projectId, topicId);
      ProjectSubscriptionName subscriptionName =
          ProjectSubscriptionName.of(projectId, subscriptionId);
      try {
        Subscription subscription =
            subscriptionAdminClient.createSubscription(
                Subscription.newBuilder()
                    .setTopic(topicName.toString())
                    .setName(subscriptionName.toString())
                    .build());
        System.out.println("Created pull subscription: " + subscription.getName());
      } catch (AlreadyExistsException | StatusRuntimeException e) {
        System.out.println(subscriptionName + " already exists.");
      }
    }
  }

  public static void updatePullSubscriptionExample(
      String projectId, String subscriptionId, String topicId) throws IOException {
    String hostport = System.getenv("PUBSUB_EMULATOR_HOST");
    ManagedChannel channel = ManagedChannelBuilder.forTarget(hostport).usePlaintext().build();
    TransportChannelProvider channelProvider =
        FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel));
    CredentialsProvider credentialsProvider = NoCredentialsProvider.create();

    try (SubscriptionAdminClient subscriptionAdminClient =
        SubscriptionAdminClient.create(
            SubscriptionAdminSettings.newBuilder()
                .setTransportChannelProvider(channelProvider)
                .setCredentialsProvider(credentialsProvider)
                .build())) {
      TopicName topicName = TopicName.of(projectId, topicId);
      ProjectSubscriptionName subscriptionName =
          ProjectSubscriptionName.of(projectId, subscriptionId);

      Subscription subscription =
          Subscription.newBuilder()
              .setTopic(topicName.toString())
              .setName(subscriptionName.toString())
              .setRetainAckedMessages(true)
              .build();

      FieldMask updateMask = FieldMask.newBuilder().addPaths("retain_acked_messages").build();

      try {
        UpdateSubscriptionRequest request =
            UpdateSubscriptionRequest.newBuilder()
                .setSubscription(subscription)
                .setUpdateMask(updateMask)
                .build();
        Subscription response = subscriptionAdminClient.updateSubscription(request);
        System.out.println("Updated pull subscription: " + response);
      } catch (StatusRuntimeException e) {
        System.out.println(subscriptionName + " did not get updated.");
      }
    }
  }

  public static void publisherExample(String projectId, String topicId)
      throws IOException, ExecutionException, InterruptedException {
    String hostport = System.getenv("PUBSUB_EMULATOR_HOST");
    ManagedChannel channel = ManagedChannelBuilder.forTarget(hostport).usePlaintext().build();
    TransportChannelProvider channelProvider =
        FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel));
    CredentialsProvider credentialsProvider = NoCredentialsProvider.create();

    TopicName topicName = TopicName.of(projectId, topicId);

    Publisher publisher = null;
    try {
      publisher =
          Publisher.newBuilder(topicName)
              .setChannelProvider(channelProvider)
              .setCredentialsProvider(credentialsProvider)
              .build();

      String message = "Hello World!";
      ByteString data = ByteString.copyFromUtf8(message);
      PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();

      ApiFuture<String> messageIdFuture = publisher.publish(pubsubMessage);
      String messageId = messageIdFuture.get();
      System.out.println("Published message ID: " + messageId);
    } finally {
      if (publisher != null) {
        publisher.shutdown();
        publisher.awaitTermination(1, TimeUnit.MINUTES);
      }
    }
  }
}
