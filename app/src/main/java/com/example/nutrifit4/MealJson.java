package com.example.nutrifit4;

import java.util.List;

public class MealJson {
    public String eatDate;
    public int eatType;
    public String mealType;
    public String foodImagepath;
    public String predictedImagePath;
    public List<FoodPosition> foodPositionList;

    public static class FoodPosition {
        public float eatAmount;
        public List<FoodCandidate> foodCandidates;
        public UserSelectedFood userSelectedFood;
        public ImagePosition imagePosition;
    }

    public static class FoodCandidate {
        public int foodId;
        public String foodName;
        public String keyName;
        public Nutrition nutrition;
    }

    public static class UserSelectedFood {
        public int foodId;
        public String foodName;
        public Nutrition nutrition;
    }

    public static class ImagePosition {
        public int xmax;
        public int xmin;
        public int ymax;
        public int ymin;
    }

    public static class Nutrition {
        public float calcium;
        public float calories;
        public float carbonhydrate;
        public float cholesterol;
        public float dietrayfiber;
        public float fat;
        public float protein;
        public float saturatedfat;
        public float sodium;
        public float sugar;
        public float totalgram;
        public float transfat;
        public String unit;
        public String foodtype;
        public float vitamina;
        public float vitaminb;
        public float vitaminc;
        public float vitamind;
        public float vitamine;
    }
}
