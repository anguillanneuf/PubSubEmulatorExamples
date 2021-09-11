import com.google.cloud.functions.Context;
import com.google.cloud.functions.RawBackgroundFunction;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.logging.Logger;

public class Background implements RawBackgroundFunction {
  private static final Logger logger =
      Logger.getLogger(Background.class.getName());

  @Override
  public void accept(String json, Context context) {
    Gson gson = new Gson();
    JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
    logger.info("Received JSON object: " + jsonObject);
  }
}
