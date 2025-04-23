package com.example.weatherapplicationb.data.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.weatherapplicationb.data.entities.WeatherResponse;
import com.example.weatherapplicationb.data.services.WeatherService;
import com.example.weatherapplicationb.databinding.ActivityMainBinding;

import retrofit2.Call;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private final ActivityResultLauncher<Intent> citySelectionLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String cityName = result.getData().getStringExtra("city_name");
                    if (cityName != null && !cityName.isEmpty()) {
                        binding.citySearch.setText(cityName);
                        fetchWeatherData(cityName);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initViews();
    }

    private void initViews() {
        binding.cityListButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CitySelectionActivity.class);
            citySelectionLauncher.launch(intent);
        });
        binding.searchButton.setOnClickListener(v -> {
            String cityName = binding.citySearch.getText().toString().trim();
            if (!cityName.isEmpty()) {
                binding.text.setText("Fetching weather for " + cityName + "...");
                fetchWeatherData(cityName);
            } else {
                Toast.makeText(this, "Please enter a city name", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchWeatherData(String cityName) {
        WeatherService.fetchWeather(cityName, new retrofit2.Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeatherResponse weather = response.body();
                    updateUI(weather);
                } else {
                    binding.text.setText("Failed to retrieve weather data");
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                binding.text.setText("Error: " + t.getMessage());
            }
        });
    }

    private void updateUI(WeatherResponse weather) {
        // Display weather details
        String weatherDetails;
        weatherDetails = String.format(
                "Temperature: %.1f°C\nCondition: %s\nHumidity: %.0f%%\nWind Speed: %.1f m/s",
                weather.getCurrent().temperature_2m,
                weather.getWeatherCondition(),
                (double) weather.getCurrent().humidity,
                weather.getCurrent().windSpeed
        );
        binding.text.setText(weatherDetails);

        // Update background color and image based on weather condition
        String condition = weather.getWeatherCondition().toLowerCase();
        if (condition.contains("clear") || condition.contains("sunny")) {
            binding.getRoot().setBackgroundColor(Color.parseColor("#FFD700")); // Yellow for sunny
        } else if (condition.contains("cloudy")) {
            binding.getRoot().setBackgroundColor(Color.parseColor("#B0BEC5")); // Grey for cloudy
        } else if (condition.contains("rain") || condition.contains("drizzle")) {
            binding.getRoot().setBackgroundColor(Color.parseColor("#0288D1")); // Blue for rain
        } else if (condition.contains("snow")) {
            binding.getRoot().setBackgroundColor(Color.parseColor("#CFD8DC")); // Light grey for snow
        } else if (condition.contains("thunderstorm")) {
            binding.getRoot().setBackgroundColor(Color.parseColor("#4A148C")); // Purple for thunderstorm
        } else {
            binding.getRoot().setBackgroundColor(Color.parseColor("#FFFFFF")); // Default white
        }
    }
}