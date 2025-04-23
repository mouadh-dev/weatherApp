package com.example.weatherapplicationb.data.services;

import androidx.annotation.NonNull;

import com.example.weatherapplicationb.data.api.ApiInterface;
import com.example.weatherapplicationb.data.entities.GeocodingResponse;
import com.example.weatherapplicationb.data.entities.WeatherResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WeatherService {
    private static final String GEOCODING_BASE_URL = "https://geocoding-api.open-meteo.com/";
    private static final String WEATHER_BASE_URL = "https://api.open-meteo.com/";

    public static void fetchWeather(String cityName, Callback<WeatherResponse> callback) {
        Retrofit geocodingRetrofit = new Retrofit.Builder()
                .baseUrl(GEOCODING_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiInterface geoCodingApi = geocodingRetrofit.create(ApiInterface.class);
        geoCodingApi.getCoordinates(cityName).enqueue(new retrofit2.Callback<>() {
            @Override
            public void onResponse(@NonNull Call<GeocodingResponse> call, @NonNull retrofit2.Response<GeocodingResponse> response) {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    if (response.body().results != null) {
                        GeocodingResponse.Result result = response.body().results.get(0);
                        double lat = result.latitude;
                        double lon = result.longitude;

                        Retrofit weatherRetrofit = new Retrofit.Builder()
                                .baseUrl(WEATHER_BASE_URL)
                                .addConverterFactory(GsonConverterFactory.create())
                                .build();

                        ApiInterface weatherApi = weatherRetrofit.create(ApiInterface.class);
                        weatherApi.getWeather(lat, lon, "temperature_2m,relative_humidity_2m,wind_speed_10m,weather_code",
                                "temperature_2m_max,temperature_2m_min,weather_code").enqueue(callback);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<GeocodingResponse> call, @NonNull Throwable t) {
                // Handle failure
            }
        });
    }
}