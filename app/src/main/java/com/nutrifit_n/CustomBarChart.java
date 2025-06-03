package com.nutrifit_n;

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

        float padding = 50f;
        float labelHeight = 50f;
        //float labelOffset = 1f;

        float chartWidth = width - 2 * padding;
        float chartHeight = height - 2 * padding - labelHeight;

        float xStep = chartWidth / (data.size() - 1);  // LineChart와 동일

        float barWidth = xStep * 0.4f;  // 좌우 여백 고려해서 조금 작게

        for (int i = 0; i < data.size(); i++) {
            float centerX = padding + i * xStep;
            float left = centerX - barWidth / 2;
            float right = centerX + barWidth / 2;
            float bottom = height - padding - labelHeight;

            List<Float> values = data.get(i);

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

            float labelY = height - padding - labelHeight / 2f + 25f;
            canvas.drawText(labels.get(i), centerX - 14f, labelY, textPaint);
        }
    }
}

