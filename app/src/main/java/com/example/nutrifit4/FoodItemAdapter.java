package com.example.nutrifit4;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FoodItemAdapter extends RecyclerView.Adapter<FoodItemAdapter.ViewHolder> {

    private final List<CameraResultActivity.FoodItem> foodItems;

    public FoodItemAdapter(List<CameraResultActivity.FoodItem> foodItems) {
        this.foodItems = foodItems;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView foodName, foodCalorie;
        public ViewHolder(View view) {
            super(view);
            foodName = view.findViewById(R.id.foodName);
            foodCalorie = view.findViewById(R.id.foodCalorie);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_food_calorie, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CameraResultActivity.FoodItem item = foodItems.get(position);
        holder.foodName.setText(item.name);
        holder.foodCalorie.setText(Math.round(item.calories) + " kcal");
    }

    @Override
    public int getItemCount() {
        return foodItems.size();
    }
}
