package com.prashanth.doctorsearch;

import retrofit2.HttpException;

public class Utils {

    private Utils() {
        //prevent instance creation
    }

    public static String USERNAME_KEY = "username";

    public static String PASSWORD_KEY = "password";

    public static String GRANT_TYPE_KEY = "grant_type";

    public static String GRANT_TYPE_VALUE = "password";

    public static String USERNAME_LOGIN = "androidChallenge@vivy.com";

    public static String PASSWORD_LOGIN = "88888888";

    public static int returnResponseCode(Throwable throwable) {
        if (throwable instanceof HttpException) {
            return ((HttpException) throwable).response().code();
        } else {
            return 0;
        }
    }
}
