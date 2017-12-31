/*
 * Copyright (c) 2017. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.storage;

import com.thoughtworks.go.plugin.api.logging.Logger;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

public class BackblazeStorage implements Storage {
    private static final String B2_API_URL = "https://api.backblazeb2.com/b2api/v1/";
    private static final String AUTHORIZE_ACCOUNT_CMD = "b2_authorize_account";

    private Logger logger = Logger.getLoggerFor(BackblazeStorage.class);

    private String errorMessage;
    private String bucketId;

    public BackblazeStorage(String bucketId) {
        this.errorMessage = "";
        this.bucketId = bucketId;
    }

    @Override
    public String getLastErrorMessage() {
        return errorMessage;
    }

    @Override
    public Boolean authorize(String accountId, String applicationKey) {
        HttpURLConnection connection = null;
        String headerForAuthorizeAccount = "Basic " +
                Base64.getEncoder().encodeToString((accountId + ":" + applicationKey).getBytes());
        try {
            URL url = new URL(B2_API_URL + AUTHORIZE_ACCOUNT_CMD);
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", headerForAuthorizeAccount);
            InputStream in = new BufferedInputStream(connection.getInputStream());
            String jsonResponse = myInputStreamReader(in);
            logger.debug("authorize: " + jsonResponse);
        } catch (Exception e) {
            errorMessage = e.getMessage();
            logger.debug("authorize: " + e.getMessage());
            return false;
        } finally {
            connection.disconnect();
        }
        return true;
    }

    @Override
    public void upload(String filePath, String destination) throws StorageException {
        
        //throw new StorageException("Unable to upload file " + file.getPath());
    }

    @Override
    public void download(String filename) {
        
    }

    static public String myInputStreamReader(InputStream in) throws IOException {
        InputStreamReader reader = new InputStreamReader(in);
        StringBuilder sb = new StringBuilder();
        int c = reader.read();
        while (c != -1) {
            sb.append((char)c);
            c = reader.read();
        }
        reader.close();
        return sb.toString();
    }
}
