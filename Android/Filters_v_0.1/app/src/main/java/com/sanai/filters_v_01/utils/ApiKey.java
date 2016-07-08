package com.sanai.filters_v_01.utils;

public class ApiKey {
    private static String apiKey;

    public ApiKey(String apiKey) {
        ApiKey.apiKey = apiKey;
    }

    public static String getApiKey() {
        return apiKey;
    }
}
