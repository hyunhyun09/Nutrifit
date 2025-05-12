package com.example.nutrifit1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class CustomBarChart extends View {
    private List<String> labels = new ArrayList<>();
    private List<List<Float>> data = new ArrayList<>();
    private Paint barPaint, textPaint;
    private int[] colors = {Color.parseColor("#818CF8"), Color.parseColor("#FB7185"), Color.parseColor("#34D399")};

    public CustomBarChart(Context context) {
        super(context);
        init();
    }

    public CustomBarChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        barPaint = new Paint();
        barPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(30f);
    }

    public void setData(List<String> labels, List<List<Float>> data) {
        this.labels = labels;
        this.data = data;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (data.isEmpty() || labels.size() != data.size()) return;

        float width = getWidth();
        float height = getHeight();
        float padding = 40f;
        float chartWidth = width - 2 * padding;
        float chartHeight = height - 2 * padding;

        float barWidth = chartWidth / (data.size() * 1.5f);  // 살짝 넓게

        for (int i = 0; i < data.size(); i++) {
            float left = padding + i * (barWidth * 1.5f);
            float bottom = height - padding;
            float right = left + barWidth;

            List<Float> values = data.get(i);

            // 이 날의 탄/단/지 총합 (0이면 bar 그리지 않음)
            float total = 0f;
            for (Float v : values) total += v;
            if (total <= 0f) continue;

            float currentTop = bottom;

            for (int j = 0; j < values.size(); j++) {
                float ratio = values.get(j) / total;
                float barHeight = ratio * chartHeight;
                float top = currentTop - barHeight;

                barPaint.setColor(colors[j % colors.length]);
                canvas.drawRect(left, top, right, currentTop, barPaint);

                currentTop = top;
            }

            // 라벨 중앙 정렬
            float labelX = left + barWidth / 4;
            canvas.drawText(labels.get(i), labelX, height - padding / 4, textPaint);
        }
    }

}

