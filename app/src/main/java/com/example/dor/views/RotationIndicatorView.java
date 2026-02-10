package com.example.dor.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * A subtle directional indicator showing clockwise rotation direction
 * with a minimal arc and arrow
 */
public class RotationIndicatorView extends View {

    private Paint arcPaint;
    private Paint arrowPaint;
    private Path arrowPath;
    private RectF arcRect;
    private float arcRadius;

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
        // Arc paint - subtle but visible
        arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeWidth(dpToPx(2.5f));
        arcPaint.setColor(0x60FFFFFF); // More visible white (about 38% opacity)
        arcPaint.setStrokeCap(Paint.Cap.ROUND);

        // Arrow paint
        arrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arrowPaint.setStyle(Paint.Style.FILL);
        arrowPaint.setColor(0x80FFFFFF); // 50% opacity for arrow

        arrowPath = new Path();
        arcRect = new RectF();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        int size = Math.min(w, h);
        float centerX = w / 2f;
        float centerY = h / 2f;

        // Arc radius - positioned between center and players
        arcRadius = size * 0.28f;

        float padding = dpToPx(8);
        arcRect.set(
            centerX - arcRadius,
            centerY - arcRadius,
            centerX + arcRadius,
            centerY + arcRadius
        );
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;

        // Draw subtle arc - clockwise direction indicator
        // Start from top-right and go most of the way around
        canvas.drawArc(arcRect, -60, 300, false, arcPaint);

        // Draw small arrow at the end of arc (indicating clockwise direction)
        float arrowAngle = (float) Math.toRadians(-60 + 300); // End of arc
        float arrowX = centerX + arcRadius * (float) Math.cos(arrowAngle);
        float arrowY = centerY + arcRadius * (float) Math.sin(arrowAngle);

        // Arrow pointing in clockwise tangent direction
        float arrowSize = dpToPx(10);
        float tangentAngle = arrowAngle + (float) Math.toRadians(90); // Perpendicular to radius, clockwise

        arrowPath.reset();
        // Arrow tip
        float tipX = arrowX + arrowSize * (float) Math.cos(tangentAngle);
        float tipY = arrowY + arrowSize * (float) Math.sin(tangentAngle);
        arrowPath.moveTo(tipX, tipY);

        // Arrow base points
        float baseAngle1 = tangentAngle + (float) Math.toRadians(140);
        float baseAngle2 = tangentAngle - (float) Math.toRadians(140);
        arrowPath.lineTo(
            arrowX + arrowSize * 0.7f * (float) Math.cos(baseAngle1),
            arrowY + arrowSize * 0.7f * (float) Math.sin(baseAngle1)
        );
        arrowPath.lineTo(
            arrowX + arrowSize * 0.7f * (float) Math.cos(baseAngle2),
            arrowY + arrowSize * 0.7f * (float) Math.sin(baseAngle2)
        );
        arrowPath.close();

        canvas.drawPath(arrowPath, arrowPaint);
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}
