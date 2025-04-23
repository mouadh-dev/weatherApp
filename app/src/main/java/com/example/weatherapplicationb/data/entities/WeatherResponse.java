package com.example.weatherapplicationb.data.entities;

import com.google.gson.annotations.SerializedName;

public class WeatherResponse {
    @SerializedName("current")
    public Current current;

    public class Current {
        @SerializedName("temperature_2m")
        public float temperature_2m;

        @SerializedName("relative_humidity_2m")
        public float humidity;

        @SerializedName("wind_speed_10m")
        public float windSpeed;

        @SerializedName("weather_code")
        public int weatherCode;
    }

    // Helper method to map weather_code to a human-readable condition
    public String getWeatherCondition() {
        if (current == null) return "Unknown";
        return mapWeatherCodeToCondition(current.weatherCode);
    }

    private String mapWeatherCodeToCondition(int code) {
        switch (code) {
            case 0: return "Clear sky";
            case 1: case 2: case 3: return "Partly cloudy";
            case 45: case 48: return "Fog";
            case 51: case 53: case 55: return "Drizzle";
            case 61: case 63: case 65: return "Rain";
            case 71: case 73: case 75: return "Snow";
            case 95: return "Thunderstorm";
            default: return "Unknown";
        }
    }

    public Current getCurrent() { return current; }
}