package  api.tapo.domain;

import com.google.gson.JsonObject;

public class HandshakeResponse {
    private String cookie;
    private JsonObject response;

    public HandshakeResponse(String cookie, JsonObject response){
        this.cookie = cookie;
        this.response = response;
    }

    public JsonObject getResponse() {
        return response;
    }

    public String getCookie() {
        return cookie;
    }
}
