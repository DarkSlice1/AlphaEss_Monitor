package api.tapo;

import com.google.gson.JsonObject;
import api.tapo.domain.HandshakeResponse;
import api.tapo.domain.KspKeyPair;
import api.tapo.helpers.KspDebug;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.security.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Tapo {

    public Map<String, C658a> c658a = new HashMap<>();
    public Map<String, String> token = new HashMap<>();
    public Map<String, HandshakeResponse> handshakeResponse = new HashMap<>();


    Config config = ConfigFactory.load();
    String email = config.getString("tapo.username");
    String password = config.getString("tapo.password");
    List<String> addresses = Arrays.asList(config.getString("tapo.addresses").split(","));


    public void Setup() throws Exception {

        for (Integer i =0; i < addresses.size();i++) {

            Security.addProvider(new BouncyCastleProvider());
            KspDebug.out("Generating keypair...");
            TapoFlow tapoFlow = new TapoFlow(addresses.get(i));

            KspKeyPair kspKeyPair = KspEncryption.generateKeyPair();

            KspDebug.out("Sending handshake!");
            if(handshakeResponse.containsKey(addresses.get(i)))
            {
                handshakeResponse.computeIfPresent(addresses.get(i), (k, v) -> tapoFlow.makeHandshake(kspKeyPair));
                KspDebug.out("Updated handshake");
            }
            else
            {
                handshakeResponse.put(addresses.get(i),tapoFlow.makeHandshake(kspKeyPair));
                KspDebug.out("Created handshake");
            }

            String keyFromTapo = handshakeResponse.get(addresses.get(i)).getResponse().getAsJsonObject("result").get("key").getAsString();
            KspDebug.out("Tapo's key is: " + keyFromTapo);
            KspDebug.out("Our session cookie is: " + handshakeResponse.get(addresses.get(i)).getCookie());

            KspDebug.out("Will try to decode it!");
            if(c658a.containsKey(addresses.get(i)))
            {
                c658a.computeIfPresent(addresses.get(i), (k, v) -> KspEncryption.decodeTapoKey(keyFromTapo, kspKeyPair));
                KspDebug.out("Updated c658a");
            }
            else
            {
                c658a.put(addresses.get(i),KspEncryption.decodeTapoKey(keyFromTapo, kspKeyPair));
                KspDebug.out("Created c658a");
            }

            KspDebug.out("Decoded!");

            KspDebug.out("Will try to login!");
            JsonObject resp = tapoFlow.loginRequest(email, password, c658a.get(addresses.get(i)), handshakeResponse.get(addresses.get(i)).getCookie());

            if(token.containsKey(addresses.get(i)))
            {
                token.computeIfPresent(addresses.get(i), (k, v) -> resp.getAsJsonObject("result").get("token").getAsString());
                KspDebug.out("Updated token");
            }
            else
            {
                token.put(addresses.get(i),resp.getAsJsonObject("result").get("token").getAsString());
                KspDebug.out("Created token");
            }
            KspDebug.out("Got token: " + token.get(addresses.get(i)));
            tapoFlow.getPlugEnergyUsage(c658a.get(addresses.get(i)), token.get(addresses.get(i)), handshakeResponse.get(addresses.get(i)).getCookie());
        }
    }

    public Map<String, Integer> Run() {
        Map<String, Integer> reply = new HashMap<>();
        for (Integer i = 0; i < addresses.size(); i++) {
            TapoFlow tapoFlow = new TapoFlow(addresses.get(i));
            KspDebug.out("Will try to Get Energy Info!");
            Integer value = tapoFlow.getPlugEnergyUsage(c658a.get(addresses.get(i)), token.get(addresses.get(i)), handshakeResponse.get(addresses.get(i)).getCookie());
            System.out.println("Tapo Energy: "+addresses.get(i)+" = "+value);
            reply.put(addresses.get(i),value);
       }
        return reply;
    }
}
