package  api.tapo.helpers;

import com.squareup.okhttp.*;


import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class KspHttp {
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient okHttpClient;

    public KspHttp() {

        this.okHttpClient = new OkHttpClient();
        //force a fail fast here - its all local network calls.
        this.okHttpClient.setConnectTimeout(500, TimeUnit.MILLISECONDS);
        this.okHttpClient.setReadTimeout(500, TimeUnit.MILLISECONDS);
    }

    public Response makePost(String url, String json, String cookie) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .addHeader("Cookie", cookie != null ? cookie : "")
                .url(url)
                .post(body)
                .build();

        Response response = null;

        try {
            response = okHttpClient.newCall(request).execute();

        } catch (IOException ex) {
            KspDebug.out("Request failed, retry..." + ex.getMessage());
        }
        return response;

    }
}
