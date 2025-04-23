package com.example.weatherapplicationb.data.ui;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.weatherapplicationb.data.adapters.CityAdapter;
import com.example.weatherapplicationb.data.api.ApiInterface;
import com.example.weatherapplicationb.data.entities.City;
import com.example.weatherapplicationb.data.entities.GeocodingResponse;
import com.example.weatherapplicationb.databinding.ActivityCitySelectionBinding;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CitySelectionActivity extends AppCompatActivity {
    private ActivityCitySelectionBinding binding;
    private CityAdapter adapter;
    private boolean mapVisible = false;
    private MapView map;
    private List<City> currentCities = new ArrayList<>();
    private static final String GEOCODING_BASE_URL = "https://geocoding-api.open-meteo.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize osmdroid configuration
        Configuration.getInstance().load(getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        binding = ActivityCitySelectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        map = binding.map;

        setupRecyclerView();
        setupSearch();
        setupMapToggle();

        // Load popular cities as default
        loadPopularCities();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (map != null) {
            map.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (map != null) {
            map.onPause();
        }
    }

    private void setupRecyclerView() {
        adapter = new CityAdapter();
        binding.cityList.setLayoutManager(new LinearLayoutManager(this));
        binding.cityList.setAdapter(adapter);

        adapter.setOnCityClickListener(city -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("city_name", city.getName());
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    private void setupSearch() {
        binding.searchCity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.length() >= 2) {
                    searchCities(query);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupMapToggle() {
        binding.toggleMapButton.setOnClickListener(v -> {
            mapVisible = !mapVisible;
            binding.mapContainer.setVisibility(mapVisible ? View.VISIBLE : View.GONE);
            binding.toggleMapButton.setText(mapVisible ? "Hide Map" : "Show Map");

            if (mapVisible && !map.isShown()) {
                initializeMap();
            } else if (mapVisible) {
                updateMapMarkers();
            }
        });
    }

    private void initializeMap() {
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.getController().setZoom(2.0);
        map.getController().setCenter(new GeoPoint(20.0, 0.0));

        updateMapMarkers();
    }

    private void updateMapMarkers() {
        if (map == null) return;

        map.getOverlays().clear();

        if (currentCities.isEmpty()) {
            // Default world view
            map.getController().setZoom(2.0);
            map.getController().setCenter(new GeoPoint(20.0, 0.0));
            return;
        }

        GeoPoint lastPosition = null;
        for (City city : currentCities) {
            Marker marker = new Marker(map);
            GeoPoint position = new GeoPoint(city.getLatitude(), city.getLongitude());
            marker.setPosition(position);
            marker.setTitle(city.getName());
            marker.setSnippet(city.getCountry());
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

            marker.setOnMarkerClickListener((marker1, mapView) -> {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("city_name", city.getName());
                setResult(RESULT_OK, resultIntent);
                finish();
                return true;
            });

            map.getOverlays().add(marker);
            lastPosition = position;
        }

        // Center map on last city
        map.getController().setZoom(5.0);
        map.getController().setCenter(lastPosition);

        map.invalidate(); // refresh the map
    }

    private void searchCities(String query) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(GEOCODING_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiInterface api = retrofit.create(ApiInterface.class);
        api.searchCities(query, 10).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<GeocodingResponse> call, @NonNull Response<GeocodingResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().results != null) {
                    List<City> cities = new ArrayList<>();
                    for (GeocodingResponse.Result result : response.body().results) {
                        cities.add(new City(
                                result.name,
                                result.country,
                                result.latitude,
                                result.longitude
                        ));
                    }
                    currentCities = cities;
                    adapter.setCities(cities);

                    // Update map if visible
                    if (mapVisible) {
                        updateMapMarkers();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<GeocodingResponse> call, @NonNull Throwable t) {
                // Handle failure
            }
        });
    }

    private void loadPopularCities() {
        // Add some popular cities as default options
        List<City> popularCities = new ArrayList<>();
        popularCities.add(new City("London", "United Kingdom", 51.5074, -0.1278));
        popularCities.add(new City("Paris", "France", 48.8566, 2.3522));
        popularCities.add(new City("New York", "USA", 40.7128, -74.0060));
        popularCities.add(new City("Tokyo", "Japan", 35.6762, 139.6503));
        popularCities.add(new City("Sydney", "Australia", -33.8688, 151.2093));

        currentCities = popularCities;
        adapter.setCities(popularCities);
    }
}