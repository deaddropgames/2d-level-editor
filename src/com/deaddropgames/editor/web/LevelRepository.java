package com.deaddropgames.editor.web;


import com.deaddropgames.editor.pickle.AuthToken;
import com.deaddropgames.editor.pickle.Utils;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class LevelRepository {

    private CloseableHttpClient httpclient;
    private String token;
    private StatusLine statusLine;

    private final String baseUrl = "http://localhost:8000";
    private final String initTokenPath = "auth/token/";

    public LevelRepository() {

        httpclient = HttpClients.createDefault();
        token = null;
        statusLine = null;
    }

    /**
     * Checks if the user is logged in
     * @return true if the token is initialized
     */
    public boolean hasToken() {

        return token != null;
    }

    /**
     * Sends user credentials to repository to get REST authentication token
     * @param username username or email
     * @param password password
     * @return true if successful
     * @throws IOException unless we messed up our URIs above, this won't throw
     * @throws URISyntaxException unless we messed up our URIs above, this won't throw
     */
    public boolean initToken(final String username, final char[] password)
            throws IOException, URISyntaxException {

        HttpPost httpPost = new HttpPost(createUrl(initTokenPath).toURI());
        List<NameValuePair> nvps = new ArrayList<>();

        nvps.add(new BasicNameValuePair("username", username));
        nvps.add(new BasicNameValuePair("password", new String(password)));
        httpPost.setEntity(new UrlEncodedFormEntity(nvps));

        try (CloseableHttpResponse response = httpclient.execute(httpPost)) {

            statusLine = response.getStatusLine();

            // only try to parse the response if it was successful
            if (statusLine.getStatusCode() == 200) {

                token = Utils.getJsonizer().fromJson(AuthToken.class, response.getEntity().getContent()).getValue();
            }
        }

        return token != null;
    }

    public StatusLine getStatusLine() {

        return statusLine;
    }

    private URL createUrl(final String path) throws MalformedURLException {

        return new URL(new URL(baseUrl), path);
    }

    private void addAuthTokenToRequest(HttpRequestBase request) {

        request.addHeader("Authorization", "Token " + token);
    }
}
