// 패키지 수정 부탁드립니다 (김현정)
package com.example.nutrifit4;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class StatisticsFragment extends Fragment {
    private CustomLineChart calorieChart;
    private CustomBarChart nutritionChart;
    private TextView dateRangeTextView;
    private Calendar currentStartDate;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat shortDateFormat;
    private TextView recommendedCalorieTextView;
    private TextView recommendedNutrientTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        calorieChart = view.findViewById(R.id.calorieChart);
        nutritionChart = view.findViewById(R.id.nutritionChart);
        dateRangeTextView = view.findViewById(R.id.dateRangeTextView);
        recommendedCalorieTextView = view.findViewById(R.id.recommendedCalorieTextView);
        recommendedNutrientTextView = view.findViewById(R.id.recommendedNutrientTextView);

        dateFormat = new SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA);
        shortDateFormat = new SimpleDateFormat("E", Locale.KOREA);

        currentStartDate = Calendar.getInstance();
        currentStartDate.add(Calendar.DAY_OF_YEAR, -6);

        ImageButton prevButton = view.findViewById(R.id.prevButton);
        ImageButton nextButton = view.findViewById(R.id.nextButton);

        prevButton.setOnClickListener(v -> {
            currentStartDate.add(Calendar.DAY_OF_YEAR, -7);
            updateCharts();
        });

        nextButton.setOnClickListener(v -> {
            currentStartDate.add(Calendar.DAY_OF_YEAR, 7);
            updateCharts();
        });

        updateCharts();

        return view;
    }


    // "userSelectedFood" 안에 있는 음식만 탐색하도록 했습니다. (김현정)
    // 일주일 동안 먹은 식단 파일 탐색이 안 되어서 코드 일부 수정했습니다.
    // 하루 누계 값이 나오도록 수정했습니다
    // 총 섭취 칼로리의 권장량이 dailyCalorie가 아닌 static string으로 나오던 오류 수정
    private void updateCharts() {
        float weeklyCalories = 0f; // 주간 누적용 변수
        float weeklyCarb = 0f, weeklyProtein = 0f, weeklyFat = 0f;

        Calendar endDate = (Calendar) currentStartDate.clone();
        endDate.add(Calendar.DAY_OF_YEAR, 6);

        String dateRange = String.format("%s - %s",
                dateFormat.format(currentStartDate.getTime()),
                dateFormat.format(endDate.getTime()));
        dateRangeTextView.setText(dateRange);

        // SharedPreferences에서 권장 칼로리 가져오기 (김현정)
        SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        float dailyCalorie = prefs.getFloat("dailyCalorie", 1780f);  // 기본값 1780 kcal
        recommendedCalorieTextView.setText("권장 : " + Math.round(dailyCalorie) + " kcal");
        recommendedNutrientTextView.setText("권장 : 탄수화물 55% / 단백질 20% / 지방 25%");

        List<Float> calorieData = new ArrayList<>();
        List<List<Float>> nutritionData = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        Calendar date = (Calendar) currentStartDate.clone();
        SimpleDateFormat fileDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.KOREA);

        for (int i = 0; i < 7; i++) {
            String fileDate = fileDateFormat.format(date.getTime());
            File dir = requireContext().getFilesDir();

            // 해당 날짜로 시작하는 모든 food_data_파일 필터링
            File[] files = dir.listFiles((d, name) ->
                    name.startsWith("food_data_" + fileDate));

            if (files != null && files.length > 0) {
                float fileCalories = 0f;
                float fileCarb = 0f, fileProtein = 0f, fileFat = 0f;

                for (File file : files) {
                    try (FileReader reader = new FileReader(file)) {
                        JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

                        if (root.has("foodPositionList")) {
                            JsonArray foodPositionList = root.getAsJsonArray("foodPositionList");

                            for (JsonElement positionElem : foodPositionList) {
                                JsonObject positionObj = positionElem.getAsJsonObject();

                                if (positionObj.has("userSelectedFood")) {
                                    JsonObject selectedFood = positionObj.getAsJsonObject("userSelectedFood");

                                    if (selectedFood.has("foodName") &&
                                            selectedFood.get("foodName").getAsString().equals("음식아님")) {
                                        continue;
                                    }

                                    if (selectedFood.has("nutrition") && !selectedFood.get("nutrition").isJsonNull()) {
                                        JsonObject nutrition = selectedFood.getAsJsonObject("nutrition");

                                        if (nutrition.has("calories") && !nutrition.get("calories").isJsonNull())
                                            fileCalories += nutrition.get("calories").getAsFloat();

                                        if (nutrition.has("carbonhydrate") && !nutrition.get("carbonhydrate").isJsonNull())
                                            fileCarb += nutrition.get("carbonhydrate").getAsFloat();

                                        if (nutrition.has("protein") && !nutrition.get("protein").isJsonNull())
                                            fileProtein += nutrition.get("protein").getAsFloat();

                                        if (nutrition.has("fat") && !nutrition.get("fat").isJsonNull())
                                            fileFat += nutrition.get("fat").getAsFloat();
                                    }
                                }
                            }
                        }

                    } catch (Exception e) {
                        Log.e("StatisticsFragment", "파일 읽기 오류: " + file.getName(), e);
                    }

                    // 누적 합산
                    weeklyCalories += fileCalories;
                    weeklyCarb += fileCarb;
                    weeklyProtein += fileProtein;
                    weeklyFat += fileFat;

                    // 로그 확인용
                    Log.d("누적결과", String.format("[%s] kcal=%.1f, 탄=%.1f, 단=%.1f, 지=%.1f",
                            file.getName(), fileCalories, fileCarb, fileProtein, fileFat));

                    // 그래프 데이터 추가
                    calorieData.add(fileCalories);
                    nutritionData.add(Arrays.asList(fileCarb, fileProtein, fileFat));
                    labels.add(shortDateFormat.format(date.getTime()));
                    date.add(Calendar.DAY_OF_YEAR, 1);
                }
            } else {
                Log.w("StatisticsFragment", "해당 날짜 파일 없음: " + fileDate);
            }

            // day + 1
            date.add(Calendar.DAY_OF_YEAR, 1);

            // 그래프 갱신
            calorieChart.setData(labels, calorieData);
            calorieChart.invalidate();
            nutritionChart.setData(labels, nutritionData);
            nutritionChart.invalidate();
            // 로그 확인용
            Log.d("일주일 누적결과", String.format("kcal=%.1f, 탄=%.1f, 단=%.1f, 지=%.1f",
                    weeklyCalories, weeklyCarb, weeklyProtein, weeklyFat));
        }
    }
}
