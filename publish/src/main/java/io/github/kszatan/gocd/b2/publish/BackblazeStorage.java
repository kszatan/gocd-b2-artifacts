/*
 * Copyright (c) 2017. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

public class BackblazeStorage {
    private static final String B2_API_URL = "https://api.backblazeb2.com/b2api/v1/";
    private static final String AUTHORIZE_ACCOUNT_CMD = "b2_authorize_account";

    private String accountId;
    private String applicationKey;

    public String getLasetErrorMessage() {
        return errorMessage;
    }

    private String errorMessage;
    
    public BackblazeStorage(String accountId, String applicationKey) {
        this.accountId = accountId;
        this.applicationKey = applicationKey;
        this.errorMessage = "";
    }

    public Boolean authorize() {
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
            System.out.println(jsonResponse);
        } catch (Exception e) {
            errorMessage = e.getMessage();
            return false;
        } finally {
            connection.disconnect();
        }
        return true;
    }

    public void upload(String filePath, String destination) throws StorageException {
        
        //throw new StorageException("Unable to upload file " + file.getPath());
    }

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
