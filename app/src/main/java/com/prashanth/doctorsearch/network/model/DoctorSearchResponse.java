package com.prashanth.doctorsearch.network.model;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import lombok.Getter;
import lombok.Setter;

public class DoctorSearchResponse {

    @Getter
    @Setter
    @SerializedName("doctors")
    ArrayList<Doctor> doctors;

    @Getter
    @SerializedName("lastKey")
    String lastKey;

    @Override
    public String toString() {
        return "DoctorSearchResponse{" +
                "doctors=" + doctors +
                ", lastKey='" + lastKey + '\'' +
                '}';
    }
}
