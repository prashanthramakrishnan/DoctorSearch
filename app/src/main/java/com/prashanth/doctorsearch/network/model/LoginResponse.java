package com.prashanth.doctorsearch.network.model;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

public class LoginResponse {

    @Getter
    @SerializedName("access_token")
    String access_token;

    @Getter
    @SerializedName("token_type")
    String token_type;

    @Getter
    @SerializedName("refresh_token")
    String refresh_token;

    @Getter
    @SerializedName("expires_in")
    String expires_in;

    @Getter
    @SerializedName("scope")
    String scope;

    @Getter
    @SerializedName("jti")
    String jti;

    @Getter
    @SerializedName("phoneVerified")
    boolean phoneVerified;

    @Override
    public String toString() {
        return "LoginResponse{" +
                "accessToken='" + access_token + '\'' +
                ", token_type='" + token_type + '\'' +
                ", refresh_token='" + refresh_token + '\'' +
                ", expires_in='" + expires_in + '\'' +
                '}';
    }
}
