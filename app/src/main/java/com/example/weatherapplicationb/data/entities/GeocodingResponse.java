package com.example.weatherapplicationb.data.entities;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GeocodingResponse {
    @SerializedName("results")
    public List<Result> results;

    public class Result {
        @SerializedName("name")
        public String name;

        @SerializedName("country")
        public String country;

        @SerializedName("latitude")
        public double latitude;

        @SerializedName("longitude")
        public double longitude;
    }
}