/*
 * Copyright (c) 2017 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.json;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.Collection;

public class GsonService {
    private static String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    public static Collection<String> validate(String json, Collection<String> requiredFields) throws InvalidJson {
        if (json == null) {
            throw new InvalidJson("Null JSON object");
        }
        try {
            ArrayList<String> missing = new ArrayList<>();
            JsonParser parser = new JsonParser();
            JsonObject root = parser.parse(json).getAsJsonObject();
            for (String field : requiredFields) {
                if (!root.has(field)) {
                    missing.add(field);
                }
            }
            return missing;
        } catch (JsonSyntaxException e) {
            throw new InvalidJson("Malformed JSON: " + json);
        }
    }

    public static String getField(String json, String fieldName) {
        JsonParser parser = new JsonParser();
        JsonObject root = parser.parse(json).getAsJsonObject();
        return root.get(fieldName).toString();
    }

    public static String toJson(Object object) {
        return (new GsonBuilder())
                .setDateFormat(DATE_TIME_FORMAT)
                .create()
                .toJson(object);
    }

    public static <T> T fromJson(String json, Class<T> type) {
        return (new GsonBuilder())
                .setDateFormat(DATE_TIME_FORMAT)
                .create()
                .fromJson(json, type);
    }
}
