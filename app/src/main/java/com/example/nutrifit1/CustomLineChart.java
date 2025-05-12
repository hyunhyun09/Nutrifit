package com.example.nutrifit1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class CustomLineChart extends View {
    private List<String> labels = new ArrayList<>();
    private List<Float> data = new ArrayList<>();
    private Paint linePaint, pointPaint, textPaint, averagePaint;
    private float maxValue = 0f;

    public CustomLineChart(Context context) {
        super(context);
        init();
    }

    public CustomLineChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        linePaint = new Paint();
        linePaint.setColor(Color.parseColor("#8B5CF6"));
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(4f);
        linePaint.setAntiAlias(true);

        pointPaint = new Paint();
        pointPaint.setColor(Color.parseColor("#8B5CF6"));
        pointPaint.setStyle(Paint.Style.FILL);
        pointPaint.setAntiAlias(true);

        textPaint = new Paint();
        textPaint.setColor(Color.parseColor("#444444"));
        textPaint.setTextSize(30f);
        textPaint.setAntiAlias(true);

        averagePaint = new Paint();
        averagePaint.setColor(Color.GRAY);
        averagePaint.setStrokeWidth(2f);
        averagePaint.setStyle(Paint.Style.STROKE);
        averagePaint.setPathEffect(null);
        averagePaint.setAntiAlias(true);
    }

    public void setData(List<String> labels, List<Float> data) {
        this.labels = labels;
        this.data = data;
        maxValue = 0f;
        for (Float value : data) {
            if (value > maxValue) maxValue = value;
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (data.isEmpty() || maxValue <= 0f) return;

        float width = getWidth();
        float height = getHeight();
        float padding = 50f;
        float chartWidth = width - 2 * padding;
        float chartHeight = height - 2 * padding;

        float xStep = chartWidth / (data.size() - 1);
        float yStep = chartHeight / maxValue;

        // 평균 계산
        float average = 0f;
        for (Float v : data) average += v;
        average /= data.size();
        float averageY = height - padding - average * yStep;

        // 평균선 그리기
        canvas.drawLine(padding, averageY, width - padding, averageY, averagePaint);
        canvas.drawText(String.format("평균: %.0f kcal", average), padding + 10f, averageY - 10f, textPaint);

        // 데이터 라인 그리기
        Path path = new Path();
        for (int i = 0; i < data.size(); i++) {
            float x = padding + i * xStep;
            float y = height - padding - data.get(i) * yStep;

            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }

            canvas.drawCircle(x, y, 8f, pointPaint);
            canvas.drawText(labels.get(i), x - 15f, height - padding / 4 + 10f, textPaint);
        }

        canvas.drawPath(path, linePaint);
    }
}
