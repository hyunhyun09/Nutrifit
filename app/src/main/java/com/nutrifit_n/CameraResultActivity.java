package com.nutrifit_n;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import okhttp3.*;

public class CameraResultActivity extends AppCompatActivity {

    private String resultJson;
    private String imageUriStr;
    private String foodFileName;

    private ImageView mealImageView;
    private TextView titleText;
    private TextView foodNamesText;
    private TextView totalCalorieLabel;
    private TextView totalCalorieValue;
    private RecyclerView foodItemsList;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_result);

        mealImageView = findViewById(R.id.mealImage);
        titleText = findViewById(R.id.titleText); // 새로 추가된 TextView (XML에 있어야 함)
        foodNamesText = findViewById(R.id.foodNamesText);
        totalCalorieLabel = findViewById(R.id.totalCalorieLabel);
        totalCalorieValue = findViewById(R.id.totalCalorieValue);
        foodItemsList = findViewById(R.id.foodItemsList);
        saveButton = findViewById(R.id.saveButton);

        resultJson = getIntent().getStringExtra("result_json");
        imageUriStr = getIntent().getStringExtra("image_uri");
        foodFileName = getIntent().getStringExtra("food_file_name");

        if (imageUriStr != null) {
            Uri imageUri = Uri.parse(imageUriStr);
            mealImageView.setImageURI(imageUri);
        }

        File foodFile = getLatestFoodDataFile();
        if (foodFile != null) {
            loadFoodData(foodFile);
        } else {
            Toast.makeText(this, "food_data 파일을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
        }

        saveButton.setOnClickListener(v -> {
            if (saveButton.getText().toString().equals("다시 시도")) {
                Intent intent = new Intent(CameraResultActivity.this, CameraActivity.class);
                startActivity(intent);
                finish();
                return;
            }

            List<File> todayFiles = getTodayFoodDataFiles();
            if (todayFiles.isEmpty()) {
                Toast.makeText(this, "오늘 날짜의 food_data 파일이 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            sendRecommendationRequest(todayFiles);
        });
    }

    private void loadFoodData(File file) {
        try {
            JsonArray foodArray = JsonParser.parseReader(new FileReader(file)).getAsJsonArray();

            List<FoodItem> foodItems = new ArrayList<>();
            double totalCalories = 0.0;
            List<String> foodNameList = new ArrayList<>();

            for (JsonElement elem : foodArray) {
                JsonObject foodObj = elem.getAsJsonObject();
                String name = foodObj.get("foodName").getAsString();
                double kcal = foodObj.getAsJsonObject("nutrition").get("calories").getAsDouble();

                if (kcal > 0) {
                    foodNameList.add(name);
                    foodItems.add(new FoodItem(name, kcal));
                    totalCalories += kcal;
                }
            }

            if (totalCalories == 0) {
                titleText.setText("인공지능 카메라가\n음식을 인식하지 못했어요.");
                saveButton.setText("다시 시도");

                foodNamesText.setVisibility(View.GONE);
                totalCalorieLabel.setVisibility(View.GONE);
                totalCalorieValue.setVisibility(View.GONE);
                foodItemsList.setVisibility(View.GONE);

                return;
            }

            foodNamesText.setText(String.join(", ", foodNameList));
            totalCalorieLabel.setText("총 섭취량");
            totalCalorieValue.setText(Math.round(totalCalories) + " kcal");

            foodItemsList.setLayoutManager(new LinearLayoutManager(this));
            foodItemsList.setAdapter(new FoodItemAdapter(foodItems));

        } catch (Exception e) {
            Log.e("FOOD_LOAD", "food_data 읽기 실패", e);
        }
    }

    private File getLatestFoodDataFile() {
        File dir = getFilesDir();
        File[] files = dir.listFiles((f, name) -> name.startsWith("food_data_") && name.endsWith(".json"));
        if (files == null || files.length == 0) return null;
        Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
        return files[0];
    }

    private List<File> getTodayFoodDataFiles() {
        File dir = getFilesDir();
        String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        File[] files = dir.listFiles((f, name) -> name.startsWith("food_data_" + today) && name.endsWith(".json"));
        if (files == null) return new ArrayList<>();
        return Arrays.asList(files);
    }

    private void sendRecommendationRequest(List<File> foodFiles) {
        try {
            File userFile = new File(getFilesDir(), "user_data.json");

            Gson gson = new Gson();
            JsonObject userJson = gson.fromJson(new InputStreamReader(new FileInputStream(userFile)), JsonObject.class);

            JsonArray mergedFoodArray = new JsonArray();
            for (File foodFile : foodFiles) {
                JsonArray singleFoodArray = gson.fromJson(new InputStreamReader(new FileInputStream(foodFile)), JsonArray.class);
                for (JsonElement item : singleFoodArray) {
                    mergedFoodArray.add(item);
                }
            }

            JsonObject merged = new JsonObject();
            merged.add("user", userJson.get("user"));
            merged.add("foods", mergedFoodArray);

            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(
                    merged.toString(),
                    MediaType.get("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url("http://43.203.201.216:5000/recommend")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(CameraResultActivity.this, "추천 요청 실패", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        runOnUiThread(() -> Toast.makeText(CameraResultActivity.this, "추천 실패: " + response.code(), Toast.LENGTH_SHORT).show());
                        return;
                    }

                    String responseBody = response.body().string();
                    Intent intent = new Intent(CameraResultActivity.this, MainActivity.class);
                    intent.putExtra("result_json", responseBody);
                    startActivity(intent);
                }
            });

        } catch (Exception e) {
            Log.e("REQUEST_ERROR", "추천 요청 중 오류", e);
            runOnUiThread(() -> Toast.makeText(CameraResultActivity.this, "추천 요청 중 오류 발생", Toast.LENGTH_SHORT).show());
        }
    }

    static class FoodItem {
        String name;
        double calories;

        FoodItem(String name, double calories) {
            this.name = name;
            this.calories = calories;
        }
    }
}
