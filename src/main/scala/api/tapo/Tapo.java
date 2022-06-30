package api.tapo;

import com.google.gson.JsonObject;
import api.tapo.domain.HandshakeResponse;
import api.tapo.domain.KspKeyPair;
import api.tapo.helpers.KspDebug;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.security.*;
import java.util.HashMap;
import java.util.Map;


public class Tapo {

    public Map<String, C658a> c658a = new HashMap<>();
    public Map<String, String> token = new HashMap<>();
    public Map<String, HandshakeResponse> handshakeResponse = new HashMap<>();


    public void Setup(String username, String password, String ipAddress) throws Exception {
        try {


            Security.addProvider(new BouncyCastleProvider());
            KspDebug.out("Generating keypair...");
            TapoFlow tapoFlow = new TapoFlow(ipAddress);

            KspKeyPair kspKeyPair = KspEncryption.generateKeyPair();

            KspDebug.out("Sending handshake!");
            if (handshakeResponse.containsKey(ipAddress)) {
                handshakeResponse.computeIfPresent(ipAddress, (k, v) -> tapoFlow.makeHandshake(kspKeyPair));
                KspDebug.out("Updated handshake");
            } else {
                handshakeResponse.put(ipAddress, tapoFlow.makeHandshake(kspKeyPair));
                KspDebug.out("Created handshake");
            }

            String keyFromTapo = handshakeResponse.get(ipAddress).getResponse().getAsJsonObject("result").get("key").getAsString();
            KspDebug.out("Tapo's key is: " + keyFromTapo);
            KspDebug.out("Our session cookie is: " + handshakeResponse.get(ipAddress).getCookie());

            KspDebug.out("Will try to decode it!");
            if (c658a.containsKey(ipAddress)) {
                c658a.computeIfPresent(ipAddress, (k, v) -> KspEncryption.decodeTapoKey(keyFromTapo, kspKeyPair));
                KspDebug.out("Updated c658a");
            } else {
                c658a.put(ipAddress, KspEncryption.decodeTapoKey(keyFromTapo, kspKeyPair));
                KspDebug.out("Created c658a");
            }

            KspDebug.out("Decoded!");

            KspDebug.out("Will try to login!");
            JsonObject resp = tapoFlow.loginRequest(username, password, c658a.get(ipAddress), handshakeResponse.get(ipAddress).getCookie());

            if (token.containsKey(ipAddress)) {
                token.computeIfPresent(ipAddress, (k, v) -> resp.getAsJsonObject("result").get("token").getAsString());
                KspDebug.out("Updated token");
            } else {
                token.put(ipAddress, resp.getAsJsonObject("result").get("token").getAsString());
                KspDebug.out("Created token");
            }
            System.out.println("Got new token for : " + ipAddress);
            KspDebug.out("Got token: " + token.get(ipAddress));
        } catch (Exception ex) {
            throw new Exception("Could not Get Token - Device Offline");
        }
    }

    public Integer Run(String ipAddress, String tag) throws Exception {
        TapoFlow tapoFlow = new TapoFlow(ipAddress);
        KspDebug.out("Will try to Get Energy Info!");
        Integer value = tapoFlow.getPlugEnergyUsage(c658a.get(ipAddress), token.get(ipAddress), handshakeResponse.get(ipAddress).getCookie());
        if(value != null) {
            System.out.println("Tapo Energy: " + ipAddress + ":"+tag+" = " + value);
            return value;
        }
        else
        {
            throw new Exception("Could not Get Energy reading");
        }
    }
}
