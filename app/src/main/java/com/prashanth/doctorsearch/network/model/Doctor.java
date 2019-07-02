package com.prashanth.doctorsearch.network.model;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

public class Doctor {

    @Getter
    @SerializedName("id")
    String id;

    @Getter
    @SerializedName("name")
    String name;

    @Getter
    @SerializedName("address")
    String address;

    @Getter
    @SerializedName("photoId")
    String photoId;

    @Override
    public String toString() {
        return "Doctor{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", photoId='" + photoId + '\'' +
                '}';
    }
}
