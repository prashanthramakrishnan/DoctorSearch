package com.prashanth.doctorsearch.network.model;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

public class Doctor {

    @Getter
    @Setter
    @SerializedName("id")
    String id;

    @Getter
    @Setter
    @SerializedName("name")
    String name;

    @Getter
    @Setter
    @SerializedName("address")
    String address;

    @Getter
    @Setter
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