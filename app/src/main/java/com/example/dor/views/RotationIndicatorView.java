package com.example.dor.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

/**
 * A subtle directional indicator showing clockwise rotation direction
 * with small arrows around the circle
 */
public class RotationIndicatorView extends View {

    private Paint arrowPaint;
    private Path arrowPath;
    private float radius;
    private int arrowCount = 3; // Number of arrows around the circle

    public RotationIndicatorView(Context context) {
        super(context);
        init();
    }

    public RotationIndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RotationIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Arrow paint - more visible
        arrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arrowPaint.setStyle(Paint.Style.FILL);
        arrowPaint.setColor(0xAAFFFFFF); // 67% opacity - more visible

        arrowPath = new Path();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int size = Math.min(w, h);
        // Position arrows between center card and player indicators
        radius = size * 0.26f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        float arrowSize = dpToPx(12); // Larger arrows

        // Draw small arrows at different positions around the circle
        // Arrows pointing in clockwise tangent direction
        // Position at: top-right, right, bottom-right (showing clockwise flow)
        float[] angles = {-30, 90, 210}; // degrees, starting from right=0

        for (float angleDeg : angles) {
            float angle = (float) Math.toRadians(angleDeg);
            float arrowX = centerX + radius * (float) Math.cos(angle);
            float arrowY = centerY + radius * (float) Math.sin(angle);

            // Tangent angle for clockwise direction (perpendicular + 90 degrees)
            float tangentAngle = angle + (float) Math.toRadians(90);

            drawArrow(canvas, arrowX, arrowY, tangentAngle, arrowSize);
        }
    }

    private void drawArrow(Canvas canvas, float x, float y, float angle, float size) {
        arrowPath.reset();

        // Arrow tip
        float tipX = x + size * (float) Math.cos(angle);
        float tipY = y + size * (float) Math.sin(angle);
        arrowPath.moveTo(tipX, tipY);

        // Arrow base points (wider angle for chevron shape)
        float baseAngle1 = angle + (float) Math.toRadians(150);
        float baseAngle2 = angle - (float) Math.toRadians(150);
        arrowPath.lineTo(
            x + size * 0.6f * (float) Math.cos(baseAngle1),
            y + size * 0.6f * (float) Math.sin(baseAngle1)
        );
        arrowPath.lineTo(
            x + size * 0.6f * (float) Math.cos(baseAngle2),
            y + size * 0.6f * (float) Math.sin(baseAngle2)
        );
        arrowPath.close();

        canvas.drawPath(arrowPath, arrowPaint);
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}
