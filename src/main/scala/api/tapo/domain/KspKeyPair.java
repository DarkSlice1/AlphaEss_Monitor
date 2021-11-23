package  api.tapo.domain;

public class KspKeyPair {


    private final String privateKey;
    private final String publicKey;
    public KspKeyPair(String privateKey, String publicKey)
    {
        this.privateKey = privateKey;
        this.publicKey = publicKey;

    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getPublicKey() {
        return publicKey.replace("\r\n","")
                .replace("-----BEGIN PUBLIC KEY-----","----BEGIN PUBLIC KEY-----\n")
                .replace("-----END PUBLIC KEY-----","-----END PUBLIC KEY-----\n");

       // return publicKey
    }
}
