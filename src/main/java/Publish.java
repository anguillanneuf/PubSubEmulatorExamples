import com.google.api.core.ApiFuture;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class Publish {
  /*
   mvn clean compile exec:java -Dexec.mainClass=Publish
  * */

  public static void main(String... args) throws Exception {
    String projectId = "abc";
    String topicId = "may";

    publisherExample(projectId, topicId);
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
      channel.shutdown();
    }
  }
}
