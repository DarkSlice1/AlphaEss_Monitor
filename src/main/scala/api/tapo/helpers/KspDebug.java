package  api.tapo.helpers;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class KspDebug {

    static Config config = ConfigFactory.load();
    static Boolean DebugMode = config.getBoolean("tapo.debugLogging");

    public static void out(String content) {
        if (DebugMode) {
            System.out.println(String.format("[TAPO-PoC] %s", content));
        }
    }
}
