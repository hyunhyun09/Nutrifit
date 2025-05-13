package com.example.nutrifit1;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

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

    private void updateCharts() {
        Calendar endDate = (Calendar) currentStartDate.clone();
        endDate.add(Calendar.DAY_OF_YEAR, 6);

        String dateRange = String.format("%s - %s",
                dateFormat.format(currentStartDate.getTime()),
                dateFormat.format(endDate.getTime()));
        dateRangeTextView.setText(dateRange);

        recommendedCalorieTextView.setText("권장 : 1,780 kcal");
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

            float totalCalories = 0f;
            float carb = 0f, protein = 0f, fat = 0f;

            if (files != null && files.length > 0) {
                for (File file : files) {
                    try (FileReader reader = new FileReader(file)) {
                        JsonArray foodArray = JsonParser.parseReader(reader).getAsJsonArray();

                        for (JsonElement elem : foodArray) {
                            JsonObject food = elem.getAsJsonObject();

                            // "음식아님"은 무시
                            if (food.has("foodName") &&
                                    food.get("foodName").getAsString().equals("음식아님")) {
                                continue;
                            }

                            if (food.has("nutrition") && !food.get("nutrition").isJsonNull()) {
                                JsonObject nutrition = food.getAsJsonObject("nutrition");

                                if (nutrition.has("calories") && !nutrition.get("calories").isJsonNull())
                                    totalCalories += nutrition.get("calories").getAsFloat();

                                if (nutrition.has("carbonhydrate") && !nutrition.get("carbonhydrate").isJsonNull())
                                    carb += nutrition.get("carbonhydrate").getAsFloat();

                                if (nutrition.has("protein") && !nutrition.get("protein").isJsonNull())
                                    protein += nutrition.get("protein").getAsFloat();

                                if (nutrition.has("fat") && !nutrition.get("fat").isJsonNull())
                                    fat += nutrition.get("fat").getAsFloat();
                            }
                        }

                    } catch (Exception e) {
                        Log.e("StatisticsFragment", "파일 읽기 오류: " + file.getName(), e);
                    }
                }
            } else {
                Log.w("StatisticsFragment", "해당 날짜 파일 없음: " + fileDate);
            }

            // 로그 확인용
            Log.d("누적결과", String.format("[%s] kcal=%.1f, 탄=%.1f, 단=%.1f, 지=%.1f",
                    fileDate, totalCalories, carb, protein, fat));

            calorieData.add(totalCalories);
            List<Float> dailyNutrition = new ArrayList<>();
            dailyNutrition.add(carb);
            dailyNutrition.add(protein);
            dailyNutrition.add(fat);
            nutritionData.add(dailyNutrition);

            labels.add(shortDateFormat.format(date.getTime()));
            date.add(Calendar.DAY_OF_YEAR, 1);
        }

        calorieChart.setData(labels, calorieData);
        calorieChart.invalidate();

        nutritionChart.setData(labels, nutritionData);
        nutritionChart.invalidate();
    }
}
