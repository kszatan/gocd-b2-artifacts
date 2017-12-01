/*
 * Copyright (c) 2017 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.json;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;

public class GsonService {
    private static String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    public static Gson getGson() {
        return new GsonBuilder()
                .setDateFormat(DATE_TIME_FORMAT)
                .create();
    }

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
        return getGson().toJson(object);
    }

    public static <T> T fromJson(String json, Class<T> type) {
        return getGson().fromJson(json, type);
    }

    public static <T> T fromJson(String json, Type type) {
        return getGson().fromJson(json, type);
    }
}
