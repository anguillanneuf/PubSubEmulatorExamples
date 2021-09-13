import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.AlreadyExistsException;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.cloud.pubsub.v1.SubscriptionAdminSettings;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminSettings;
import com.google.protobuf.FieldMask;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PushConfig;
import com.google.pubsub.v1.Subscription;
import com.google.pubsub.v1.Topic;
import com.google.pubsub.v1.TopicName;
import com.google.pubsub.v1.UpdateSubscriptionRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import java.io.IOException;

public class CreateResources {
  /*
   mvn clean compile exec:java -Dexec.mainClass=CreateResources
  * */

  public static void main(String... args) throws Exception {
    String projectId = "abc";
    String topicId = "may";
    // String pullSubscriptionId = "five";
    String pushSubscriptionId = "five-push";
    String pushEndpoint = "http://localhost:8082";

    createTopicExample(projectId, topicId);
    // createPullSubscriptionExample(projectId, pullSubscriptionId, topicId);
    createPushSubscriptionExample(projectId, pushSubscriptionId, topicId, pushEndpoint);
    // updatePullSubscriptionExample(projectId, pullSubscriptionId, topicId);
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
    } finally {
      channel.shutdown();
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
                    .setRetainAckedMessages(true)
                    .build());
        System.out.println("Created pull subscription: " + subscription.getName());
      } catch (AlreadyExistsException | StatusRuntimeException e) {
        System.out.println(subscriptionName + " already exists.");
      }
    } finally {
      channel.shutdown();
    }
  }

  public static void createPushSubscriptionExample(
      String projectId, String subscriptionId, String topicId, String pushEndpoint) throws IOException {
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
      PushConfig pushConfig = PushConfig.newBuilder().setPushEndpoint(pushEndpoint).build();

      Subscription subscription = Subscription.newBuilder()
          .setTopic(topicName.toString())
          .setName(subscriptionName.toString())
          .setRetainAckedMessages(true)
          .setPushConfig(pushConfig)
          .setAckDeadlineSeconds(60)
          .build();

      try {
        Subscription response = subscriptionAdminClient.createSubscription(subscription);
        System.out.println("Created push subscription: " + response.getName() + " to " + subscription.getPushConfig().getPushEndpoint());
      } catch (AlreadyExistsException | StatusRuntimeException e) {
        System.out.println(subscriptionName + " already exists.");
        subscriptionAdminClient.deleteSubscription(subscriptionName);
        System.out.println("Deleted push subscription: " + subscriptionName + " to " + pushConfig.getPushEndpoint());
        Subscription response = subscriptionAdminClient.createSubscription(subscription);
        System.out.println("Created push subscription: " + response.getName() + " to " + subscription.getPushConfig().getPushEndpoint());
      }
    } finally {
      channel.shutdown();
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
    } finally {
      channel.shutdown();
    }
  }
}
