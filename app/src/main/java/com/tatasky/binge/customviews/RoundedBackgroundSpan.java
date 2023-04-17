package com.tatasky.binge.customviews;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.style.ReplacementSpan;

import com.tatasky.binge.R;
import com.tatasky.binge.utils.UtilityKt;

public class RoundedBackgroundSpan extends ReplacementSpan {

    private static final int CORNER_RADIUS = 20;
    private int backgroundColor = 0;
    private int textColor = 0;
    private  int transparent = 0;
    Typeface typeface;
    float fontSize;

    public RoundedBackgroundSpan(Context context) {
        super();
        backgroundColor = context.getResources().getColor(R.color.more_color);
        textColor = context.getResources().getColor(R.color.more_color);
        transparent = context.getResources().getColor(R.color.transparent);
        int spSize = 8;
        fontSize = spSize * context.getResources().getDisplayMetrics().scaledDensity;
        typeface = UtilityKt.getMoreTypeFace(context, "voltePlay");
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(backgroundColor);
        paint.setTextSize(fontSize);
        paint.setTypeface(typeface);
        paint.setStrokeWidth(2.5f);
        canvas.drawColor(transparent);
        RectF rect = new RectF(x, top, x + measureText(paint, text, start, end), bottom);
        canvas.drawRoundRect(rect, CORNER_RADIUS, CORNER_RADIUS, paint);
        paint.setColor(textColor);
        paint.setStrokeWidth(0);
        canvas.drawText(text, start, end, x, y, paint);
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        return Math.round(paint.measureText(text, start, end));
    }

    private float measureText(Paint paint, CharSequence text, int start, int end) {
        return paint.measureText(text, start, end);
    }

}