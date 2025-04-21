package com.example.nutrifit1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

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

    public PostFragment() {
    }

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
        dailyCalorieValue.setText(Math.round(dailyCalorie) + " kcal");
        updateCalorieDifference();
    }

    private void loadConsumedCalorieData() {
        try {
            File dir = requireContext().getFilesDir();
            String today = new SimpleDateFormat("yyyyMMdd").format(new Date());
            File[] files = dir.listFiles((dir1, name) -> name.startsWith("food_data_" + today) && name.endsWith(".json"));

            Gson gson = new Gson();

            if (files != null) {
                for (File file : files) {
                    JsonArray foodArray = gson.fromJson(new InputStreamReader(new FileInputStream(file)), JsonArray.class);
                    for (int i = 0; i < foodArray.size(); i++) {
                        JsonObject foodObj = foodArray.get(i).getAsJsonObject();
                        JsonObject nutrition = foodObj.getAsJsonObject("nutrition");
                        if (nutrition.has("calories")) {
                            totalCalories += nutrition.get("calories").getAsDouble();
                        }
                        if (nutrition.has("carbonhydrate")) {
                            totalCarbs += nutrition.get("carbonhydrate").getAsDouble();
                        }
                        if (nutrition.has("protein")) {
                            totalProtein += nutrition.get("protein").getAsDouble();
                        }
                        if (nutrition.has("fat")) {
                            totalFat += nutrition.get("fat").getAsDouble();
                        }
                    }
                }
            }
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