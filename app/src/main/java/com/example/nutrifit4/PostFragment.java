// 패키지 수정 부탁드립니다 (김현정)
package com.example.nutrifit4;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PostFragment extends Fragment {
    private TextView dailyCalorieValue;
    private TextView consumedCalorieValue;
    private TextView calorieTextView;
    private ProgressBar calorieProgressBar;
    private TextView carbsTextView;
    private ProgressBar carbsProgressBar;
    private TextView proteinTextView;
    private ProgressBar proteinProgressBar;
    private TextView fatTextView;
    private ProgressBar fatProgressBar;

    private double dailyCalorie = 0.0;
    private double totalCalories = 0.0;
    private double totalCarbs = 0.0;
    private double totalProtein = 0.0;
    private double totalFat = 0.0;
    private double weight = 0.0;
    private double height = 0.0;
    private int age = 0;
    private String gender = "";
    private int activity = 0;

    public PostFragment() {}

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post, container, false);

        dailyCalorieValue = view.findViewById(R.id.dailyCalorieValue);
        consumedCalorieValue = view.findViewById(R.id.consumedCalorieValue);
        calorieTextView = view.findViewById(R.id.calorieTextView);
        calorieProgressBar = view.findViewById(R.id.calorieProgressBar);
        carbsTextView = view.findViewById(R.id.carbsTextView);
        carbsProgressBar = view.findViewById(R.id.carbsProgressBar);
        proteinTextView = view.findViewById(R.id.proteinTextView);
        proteinProgressBar = view.findViewById(R.id.proteinProgressBar);
        fatTextView = view.findViewById(R.id.fatTextView);
        fatProgressBar = view.findViewById(R.id.fatProgressBar);

        loadUserData();
        calculateDailyCalorie();
        loadConsumedCalorieData();
        loadMealLogs(view);

        // 날짜 지정
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        String date = sdf.format(Calendar.getInstance().getTime());

        // 오늘 섭취 칼로리 계산
        totalCalories = MealUtils.calculateDailyCalories(requireContext(), date); // totalCalories 변수에 저장

        // UI 업데이트
        consumedCalorieValue.setText((int) totalCalories + " kcal");

        // ProgressBar 설정
        calorieProgressBar.setMax(2000); // 기준 설정

        // 🔥 여기서 남은 칼로리 업데이트
        updateCalorieDifference();

        return view;
    }

    private void loadUserData() {
        try {
            File file = new File(requireContext().getFilesDir(), "user_data.json");
            Gson gson = new Gson();
            JsonObject root = gson.fromJson(new InputStreamReader(new FileInputStream(file)), JsonObject.class);
            JsonArray userArray = root.getAsJsonArray("user");
            if (userArray.size() > 0) {
                JsonObject user = userArray.get(0).getAsJsonObject();
                height = user.get("height").getAsDouble();
                weight = user.get("weight").getAsDouble();
                age = user.get("age").getAsInt();
                gender = user.get("gender").getAsString();
                activity = user.get("activity").getAsInt();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void calculateDailyCalorie() {
        double bmr = 10 * weight + 6.25 * height - 5 * age + (gender.equals("1") ? 5 : -161);
        dailyCalorie = bmr * (1.0 + 0.2 * (activity - 1));

        // 저장
        SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat("dailyCalorie", (float) dailyCalorie);
        editor.apply();

        dailyCalorieValue.setText(Math.round(dailyCalorie) + " kcal");
        updateCalorieDifference();
    }

    // json 열기 및 노드 탐색 + 전반 수정 (김현정)
    // "userSelectedFood" 안에 있는 음식만 탐색하도록 했습니다.
    private void loadConsumedCalorieData() {
        try {
            // 날짜 포맷 및 오늘 날짜 얻기
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.KOREA);
            String today = dateFormat.format(new Date()); // ex: "20250607"

            // 앱 내부 저장소 경로
            File dir = requireContext().getFilesDir();

            // 오늘 날짜 파일만 필터링: food_data_YYYYMMDD_*.json
            File[] files = dir.listFiles((d, name) ->
                    name.startsWith("food_data_" + today) && name.endsWith(".json"));

            if (files != null && files.length > 0) {
                for (File file : files) {
                    try (FileReader reader = new FileReader(file)) {
                        JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

                        // foodPositionList가 존재하고 배열인지 확인
                        if (root.has("foodPositionList") && root.get("foodPositionList").isJsonArray()) {
                            JsonArray foodPositionList = root.getAsJsonArray("foodPositionList");

                            // foodPositionList가 비어있는지 확인
                            if (foodPositionList.size() == 0) {
                                Log.w("NutritionSummary", "foodPositionList가 비어있음: " + file.getName());
                                continue; // 비어있는 경우 건너뜀
                            }

                            // 각 foodPosition 항목을 순회
                            for (JsonElement positionElem : foodPositionList) {
                                JsonObject positionObj = positionElem.getAsJsonObject();

                                // userSelectedFood 정보가 있는지 확인
                                if (positionObj.has("userSelectedFood") && positionObj.get("userSelectedFood").isJsonObject()) {
                                    JsonObject selectedFood = positionObj.getAsJsonObject("userSelectedFood");

                                    // foodName이 "음식아님"인지 확인하여 해당 항목은 건너뛰기
                                    if (selectedFood.has("foodName") &&
                                            selectedFood.get("foodName").getAsString().equals("음식아님")) {
                                        continue;
                                    }

                                    // nutrition 객체가 있는지 확인
                                    if (selectedFood.has("nutrition") && selectedFood.get("nutrition").isJsonObject()) {
                                        JsonObject nutrition = selectedFood.getAsJsonObject("nutrition");

                                        // 각 영양소를 누적
                                        if (nutrition.has("calories") && !nutrition.get("calories").isJsonNull())
                                            totalCalories += nutrition.get("calories").getAsFloat();

                                        if (nutrition.has("carbonhydrate") && !nutrition.get("carbonhydrate").isJsonNull())
                                            totalCarbs += nutrition.get("carbonhydrate").getAsFloat();

                                        if (nutrition.has("protein") && !nutrition.get("protein").isJsonNull())
                                            totalProtein += nutrition.get("protein").getAsFloat();

                                        if (nutrition.has("fat") && !nutrition.get("fat").isJsonNull())
                                            totalFat += nutrition.get("fat").getAsFloat();
                                    }
                                }
                            }
                        } else {
                            Log.w("NutritionSummary", "foodPositionList 없음 또는 배열 아님: " + file.getName());
                        }

                    } catch (Exception e) {
                        Log.e("NutritionSummary", "파일 읽기 오류: " + file.getName(), e);
                    }
                }
            } else {
                Log.w("NutritionSummary", "오늘 날짜 파일 없음: " + today);
            }

            // 결과 출력 (또는 UI에 표시)
            Log.i("NutritionSummary", String.format("총합 - kcal: %.1f, 탄수화물: %.1fg, 단백질: %.1fg, 지방: %.1fg",
                    totalCalories, totalCarbs, totalProtein, totalFat));

            consumedCalorieValue.setText(Math.round(totalCalories) + " kcal");
            updateCalorieDifference();
            updateCarbsInfo();
            updateProteinInfo();
            updateFatInfo();
        } catch (Exception e) {
            consumedCalorieValue.setText("-");
            e.printStackTrace();
        }
    }

    // json 열기 및 노드 탐색 + 전반 수정 (김현정)
    // "userSelectedFood" 안에 있는 음식만 탐색하도록 했습니다.
    private void loadMealLogs(View view) {
        try {
            Context context = getContext();
            if (context == null) return;

            File dir = context.getFilesDir();
            String today = new SimpleDateFormat("yyyyMMdd", Locale.KOREA).format(new Date());
            File[] files = dir.listFiles((dir1, name) ->
                    name.startsWith("food_data_" + today) && name.endsWith(".json"));

            LinearLayout mealLogLayout = view.findViewById(R.id.mealLogLayout);
            LayoutInflater inflater = LayoutInflater.from(context);

            if (files != null) {
                for (File file : files) {
                    InputStreamReader reader = new InputStreamReader(new FileInputStream(file));
                    JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

                    // foodPositionList가 JSON 배열인지 확인
                    if (root.has("foodPositionList") && root.get("foodPositionList").isJsonArray()) {
                        JsonArray foodPositionList = root.getAsJsonArray("foodPositionList");

                        List<String> foodNames = new ArrayList<>();
                        double totalFileCalories = 0;

                        // foodPositionList 항목 순회
                        for (JsonElement positionElem : foodPositionList) {
                            JsonObject positionObj = positionElem.getAsJsonObject();

                            // userSelectedFood가 JSON 객체인지 확인
                            if (positionObj.has("userSelectedFood") && positionObj.get("userSelectedFood").isJsonObject()) {
                                JsonObject selectedFood = positionObj.getAsJsonObject("userSelectedFood");

                                // foodName이 "음식아님"인지 확인하여 해당 항목은 건너뛰기
                                if (selectedFood.has("foodName") && "음식아님".equals(selectedFood.get("foodName").getAsString())) {
                                    continue;
                                }

                                // nutrition 객체가 존재하고 유효한 경우에만 처리
                                if (selectedFood.has("nutrition") && selectedFood.get("nutrition").isJsonObject()) {
                                    JsonObject nutrition = selectedFood.getAsJsonObject("nutrition");

                                    // foodName을 리스트에 추가
                                    String foodName = selectedFood.get("foodName").getAsString();
                                    foodNames.add(foodName);

                                    // calories 계산
                                    if (nutrition.has("calories") && !nutrition.get("calories").isJsonNull()) {
                                        double calories = nutrition.get("calories").getAsDouble();
                                        double eatAmount = positionObj.has("eatAmount") ? positionObj.get("eatAmount").getAsDouble() : 1; // eatAmount가 없을 경우 기본값 1
                                        totalFileCalories += calories * eatAmount;
                                    }
                                }
                            }
                        }

                        // 리스트에 foodNames가 하나 이상 있을 때만 뷰에 추가
                        if (!foodNames.isEmpty()) {
                            View item = inflater.inflate(R.layout.item_food_calorie, mealLogLayout, false);
                            TextView nameView = item.findViewById(R.id.foodName);
                            TextView calorieView = item.findViewById(R.id.foodCalorie);

                            nameView.setText(TextUtils.join(", ", foodNames));
                            calorieView.setText(Math.round(totalFileCalories) + " kcal");

                            mealLogLayout.addView(item);
                        }
                    }

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateCalorieDifference() {
        if (dailyCalorie > 0 && totalCalories >= 0) {
            double remaining = dailyCalorie - totalCalories;
            if (remaining < 0) remaining = 0;
            calorieTextView.setText(String.valueOf(Math.round(remaining)));

            int progress = (int) Math.round((totalCalories / dailyCalorie) * 100);
            if (progress > 100) progress = 100;
            calorieProgressBar.setProgress(progress);
        } else {
            calorieTextView.setText("-");
            calorieProgressBar.setProgress(0);
        }
    }

    private void updateCarbsInfo() {
        if (dailyCalorie > 0) {
            double carbGoal = dailyCalorie * 0.5 / 4.0;
            String displayText = Math.round(totalCarbs) + "/" + Math.round(carbGoal) + "g";
            carbsTextView.setText(displayText);

            int progress = (int) Math.round((totalCarbs / carbGoal) * 100);
            if (progress > 100) progress = 100;
            carbsProgressBar.setProgress(progress);
        } else {
            carbsTextView.setText("-/-");
            carbsProgressBar.setProgress(0);
        }
    }

    private void updateProteinInfo() {
        if (weight > 0) {
            double proteinGoal = weight * 0.8;
            String displayText = Math.round(totalProtein) + "/" + Math.round(proteinGoal) + "g";
            proteinTextView.setText(displayText);

            int progress = (int) Math.round((totalProtein / proteinGoal) * 100);
            if (progress > 100) progress = 100;
            proteinProgressBar.setProgress(progress);
        } else {
            proteinTextView.setText("-/-");
            proteinProgressBar.setProgress(0);
        }
    }

    private void updateFatInfo() {
        if (dailyCalorie > 0) {
            double fatGoal = dailyCalorie * 0.3 / 9.0;
            String displayText = Math.round(totalFat) + "/" + Math.round(fatGoal) + "g";
            fatTextView.setText(displayText);

            int progress = (int) Math.round((totalFat / fatGoal) * 100);
            if (progress > 100) progress = 100;
            fatProgressBar.setProgress(progress);
        } else {
            fatTextView.setText("-/-");
            fatProgressBar.setProgress(0);
        }
    }
}
