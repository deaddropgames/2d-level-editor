package com.deaddropgames.editor.web;


import com.badlogic.gdx.net.HttpStatus;
import com.deaddropgames.editor.pickle.ApiBaseList;
import com.deaddropgames.editor.pickle.AuthToken;
import com.deaddropgames.editor.pickle.Level;
import com.deaddropgames.editor.pickle.Utils;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
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
    private final String getLevelPath = "stuntski/api/editor/%d/";
    private final String getLevelListPath = "stuntski/api/levels/";

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
     * @return the HTTP status from the last call
     */
    public StatusLine getStatusLine() {

        return statusLine;
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
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {

                token = Utils.getJsonizer().fromJson(AuthToken.class, response.getEntity().getContent()).getValue();
            }
        }

        return token != null;
    }

    /**
     * Gets a list of levels
     * @param path the relative URI path or null to get the default (path is not null if a next/previous linke is used)
     * @return the level list result
     * @throws IOException unless we messed up our URIs above, this won't throw
     * @throws URISyntaxException unless we messed up our URIs above, this won't throw
     */
    public ApiBaseList getLevelList(String path)
            throws IOException, URISyntaxException {

        // if path isn't null, it's likely a next/previous link
        if (path == null) {

            path = getLevelListPath;
        }

        HttpGet httpGet = new HttpGet(createUrl(path).toURI());
        addAuthTokenToRequest(httpGet);

        ApiBaseList levelList = null;
        try (CloseableHttpResponse response = httpclient.execute(httpGet)) {

            statusLine = response.getStatusLine();

            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {

                levelList = Utils.getJsonizer().fromJson(ApiBaseList.class, response.getEntity().getContent());
            }
        }

        return levelList;
    }

    /**
     * Downloads a level from the website
     * @param id the id of the web resource
     * @return an editor level if successful
     * @throws IOException unless we messed up our URIs above, this won't throw
     * @throws URISyntaxException unless we messed up our URIs above, this won't throw
     */
    public Level getLevel(long id)
            throws IOException, URISyntaxException {

        HttpGet httpGet = new HttpGet(createUrl(String.format(getLevelPath, id)).toURI());
        addAuthTokenToRequest(httpGet);

        Level level = null;
        try (CloseableHttpResponse response = httpclient.execute(httpGet)) {

            statusLine = response.getStatusLine();

            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {

                level = Utils.getJsonizer().fromJson(Level.class, response.getEntity().getContent());
            }
        }

        return level;
    }

    private URL createUrl(final String path) throws MalformedURLException {

        return new URL(new URL(baseUrl), path);
    }

    private void addAuthTokenToRequest(HttpRequestBase request) {

        request.addHeader("Authorization", "Token " + token);
    }
}
