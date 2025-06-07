package com.example.nutrifit4;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class CustomLineChart extends View {
    private List<String> labels = new ArrayList<>();
    private List<Float> data = new ArrayList<>();
    private Paint linePaint, pointPaint, textPaint, averagePaint, bgPaint;
    private float maxValue = 0f;

    private int selectedIndex = -1;

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
        averagePaint.setAntiAlias(true);

        bgPaint = new Paint();
        bgPaint.setColor(Color.WHITE);
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setShadowLayer(4f, 0f, 2f, Color.GRAY);

        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    public void setData(List<String> labels, List<Float> data) {
        this.labels = labels;
        this.data = data;
        maxValue = 0f;
        for (Float value : data) {
            if (value > maxValue) maxValue = value;
        }
        selectedIndex = -1;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (data.isEmpty() || maxValue <= 0f) return;

        float width = getWidth();
        float height = getHeight();
        float padding = 50f;
        float bottomExtra = 40f;
        float chartWidth = width - 2 * padding;
        float chartHeight = height - padding - bottomExtra - padding;

        float xStep = chartWidth / (data.size() - 1);
        float yStep = chartHeight / maxValue;

        // 평균 계산
        float average = 0f;
        for (Float v : data) average += v;
        average /= data.size();
        float averageY = padding + chartHeight - average * yStep;

        // 평균선
        canvas.drawLine(padding, averageY, width - padding, averageY, averagePaint);
        canvas.drawText(String.format("평균: %.0f kcal", average), padding + 10f, averageY - 10f, textPaint);

        // 그래프 경로
        Path path = new Path();
        for (int i = 0; i < data.size(); i++) {
            float x = padding + i * xStep;
            float y = padding + chartHeight - data.get(i) * yStep;

            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }

            canvas.drawCircle(x, y, 8f, pointPaint);

            // 라벨 중앙 보정
            String label = labels.get(i);
            float textWidth = textPaint.measureText(label);
            float labelX = x - textWidth / 2 + 5f;
            float labelY = height - bottomExtra / 2;
            canvas.drawText(label, labelX, labelY, textPaint);
        }

        // 선 그리기
        canvas.drawPath(path, linePaint);

        // 선택된 점에 대한 kcal 플로팅
        if (selectedIndex >= 0 && selectedIndex < data.size()) {
            float x = padding + selectedIndex * xStep;
            float y = padding + chartHeight - data.get(selectedIndex) * yStep;
            String valueText = String.format("%.0f kcal", data.get(selectedIndex));

            float textWidth = textPaint.measureText(valueText);
            float textPadding = 16f;
            float rectWidth = textWidth + textPadding * 2;
            float rectHeight = 50f;

            float rectLeft = x - rectWidth / 2;
            float rectRight = x + rectWidth / 2;

            float viewWidth = getWidth();
            float viewHeight = getHeight();

            if (rectLeft < 0) {
                rectLeft = 0;
                rectRight = rectLeft + rectWidth;
            } else if (rectRight > viewWidth) {
                rectRight = viewWidth;
                rectLeft = rectRight - rectWidth;
            }

            float rectTop = y - 70f;
            float rectBottom = rectTop + rectHeight;
            if (rectTop < 0) {
                rectTop = y + 20f;
                rectBottom = rectTop + rectHeight;
            }

            // 말풍선 배경
            bgPaint.setColor(Color.WHITE);
            bgPaint.setStyle(Paint.Style.FILL);
            bgPaint.setShadowLayer(2f, 0f, 1f, Color.GRAY);
            canvas.drawRoundRect(rectLeft, rectTop, rectRight, rectBottom, 14f, 14f, bgPaint);

            // 텍스트 중앙 정렬 계산
            Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
            float textX = rectLeft + (rectWidth - textWidth) / 2;
            float textY = rectTop + (rectHeight - (fontMetrics.bottom - fontMetrics.top)) / 2 - fontMetrics.top;

            canvas.drawText(valueText, textX, textY, textPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (data.isEmpty()) return false;

        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            float x = event.getX();
            float width = getWidth();
            float padding = 50f;
            float chartWidth = width - 2 * padding;

            if (data.size() > 1) {
                float xStep = chartWidth / (data.size() - 1);

                for (int i = 0; i < data.size(); i++) {
                    float dataX = padding + i * xStep;
                    if (Math.abs(x - dataX) < xStep / 2) {
                        selectedIndex = i;
                        invalidate();
                        break;
                    }
                }
            }
        }

        return true;
    }
}