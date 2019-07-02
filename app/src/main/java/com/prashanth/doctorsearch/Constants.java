package com.prashanth.doctorsearch;

public class Constants {

    private Constants() {
        //prevent instance creation
    }

    public static String USERNAME_KEY = "username";

    public static String PASSWORD_KEY = "password";

    public static String GRANT_TYPE_KEY = "grant_type";

    public static String GRANT_TYPE_VALUE = "password";

    public static final String CONTENT_TYPE = "application/x-www-form-urlencoded";

    public static String LOGIN_ENDPOINT = "oauth/token/?grant_type=password";

    public static String CONTENT_TYPE_ACCEPT_VALUE = "application/json";

    public static String USERNAME_LOGIN = "androidChallenge@vivy.com";

    public static String PASSWORD_LOGIN = "88888888";

    public static String LOGIN_URL = "https://auth.staging.vivy.com";

    public static String LOGGED_IN_URL = "https://api.staging.vivy.com";

}
