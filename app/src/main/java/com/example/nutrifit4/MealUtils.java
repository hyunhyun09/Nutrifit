package com.example.nutrifit4; // 또는 com.example.nutrifit4.utils

import android.content.Context;

import com.google.gson.Gson;

import java.io.*;

public class MealUtils {

    public static MealJson readJsonFromInternalStorage(Context context, String fileName) {
        File file = new File(context.getFilesDir(), fileName);
        if (!file.exists()) return null;

        try {
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader reader = new BufferedReader(isr);
            StringBuilder builder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            reader.close();
            isr.close();
            fis.close();

            Gson gson = new Gson();
            return gson.fromJson(builder.toString(), MealJson.class);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static float calculateDailyCalories(Context context, String date) {
        float totalCalories = 0f;

        // 날짜를 yyyyMMdd 형식으로 변환 (예: 2025-06-04 → 20250604)
        String datePrefix = date.replace("-", "");

        File dir = context.getFilesDir();
        File[] files = dir.listFiles();

        if (files == null) return 0f;

        for (File file : files) {
            String fileName = file.getName();

            // "food_data_20250604"로 시작하고 ".json"으로 끝나는 파일만 읽음
            if (fileName.startsWith("food_data_" + datePrefix) && fileName.endsWith(".json")) {
                MealJson mealJson = readJsonFromInternalStorage(context, fileName);

                if (mealJson != null && mealJson.foodPositionList != null) {
                    for (MealJson.FoodPosition food : mealJson.foodPositionList) {
                        if (food.userSelectedFood != null && food.userSelectedFood.nutrition != null) {
                            totalCalories += food.eatAmount * food.userSelectedFood.nutrition.calories;
                        }
                    }
                }
            }
        }

        return totalCalories;
    }

}
