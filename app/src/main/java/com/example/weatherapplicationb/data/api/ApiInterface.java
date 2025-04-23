package com.example.weatherapplicationb.data.api;

import com.example.weatherapplicationb.data.entities.GeocodingResponse;
import com.example.weatherapplicationb.data.entities.WeatherResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiInterface {
    @GET("v1/search?count=1")
    Call<GeocodingResponse> getCoordinates(@Query("name") String cityName);

    @GET("v1/forecast")
    Call<WeatherResponse> getWeather(
            @Query("latitude") double latitude,
            @Query("longitude") double longitude,
            @Query("current") String current,
            @Query("daily") String daily
    );

    @GET("v1/search")
    Call<GeocodingResponse> searchCities(@Query("name") String query, @Query("count") int count);
}