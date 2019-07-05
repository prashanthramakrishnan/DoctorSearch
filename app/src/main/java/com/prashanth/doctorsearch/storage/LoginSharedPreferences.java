package com.prashanth.doctorsearch.storage;

import android.content.Context;
import android.content.SharedPreferences;

public class LoginSharedPreferences {

    private static final String FILE_NAME = "doctorsearch";

    private static final String ACCESS_TOKEN_KEY = "access_token";

    private static final String LAST_KEY = "last_key";

    private SharedPreferences sharedPreferences;

    public LoginSharedPreferences(Context context) {
        sharedPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    public void setAccessToken(String accessToken) {
        sharedPreferences.edit().putString(ACCESS_TOKEN_KEY, accessToken).apply();
    }

    public String getAccessToken() {
        return sharedPreferences.getString(ACCESS_TOKEN_KEY, null);
    }

    public void setLastKey(String lastKey) {
        sharedPreferences.edit().putString(LAST_KEY, lastKey).apply();
    }

    public String getLastKey() {
        return sharedPreferences.getString(LAST_KEY, null);
    }

    public void clear() {
        sharedPreferences.edit().clear().apply();
    }

}
