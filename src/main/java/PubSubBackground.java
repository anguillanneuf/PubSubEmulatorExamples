import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import java.util.Map;
import java.util.logging.Logger;

class PubSubMessage {
  String data;
  Map<String, String> attributes;
  String messageId;
  String publishTime;
}

public class PubSubBackground implements BackgroundFunction<PubSubMessage> {
  private static final Logger logger =
      Logger.getLogger(PubSubBackground.class.getName());

  @Override
  public void accept(PubSubMessage pubSubMessage, Context context) {
    logger.info("Received message with id " + pubSubMessage.messageId);
  }
}