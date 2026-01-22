package com.example.dor.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * A custom layout that arranges its children in a circle around a center point.
 * The first child is placed at the center, and all other children are arranged
 * in a circle around it.
 */
public class CircularPlayerLayout extends ViewGroup {

    private int centerViewSize = 0;
    private float radiusRatio = 0.38f;

    public CircularPlayerLayout(Context context) {
        super(context);
    }

    public CircularPlayerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CircularPlayerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        // Make it square
        int size = Math.min(width, height);

        // Measure all children
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                measureChild(child,
                    MeasureSpec.makeMeasureSpec(size, MeasureSpec.AT_MOST),
                    MeasureSpec.makeMeasureSpec(size, MeasureSpec.AT_MOST));
            }
        }

        setMeasuredDimension(size, size);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = r - l;
        int height = b - t;
        int centerX = width / 2;
        int centerY = height / 2;

        int childCount = getChildCount();
        if (childCount == 0) return;

        // First child is the center view
        View centerView = getChildAt(0);
        if (centerView != null && centerView.getVisibility() != GONE) {
            int cw = centerView.getMeasuredWidth();
            int ch = centerView.getMeasuredHeight();
            centerViewSize = Math.max(cw, ch);

            int cl = centerX - cw / 2;
            int ct = centerY - ch / 2;
            centerView.layout(cl, ct, cl + cw, ct + ch);
        }

        // Remaining children are arranged in a circle
        int circularChildCount = childCount - 1;
        if (circularChildCount <= 0) return;

        // Calculate optimal radius
        int containerSize = Math.min(width, height);

        // Get the size of the first circular child to estimate all sizes
        View firstCircularChild = getChildAt(1);
        int childSize = Math.max(firstCircularChild.getMeasuredWidth(), firstCircularChild.getMeasuredHeight());

        // Calculate radius: large enough to not overlap center, small enough to stay in bounds
        int minRadius = (centerViewSize / 2) + (childSize / 2) + dpToPx(16);
        int maxRadius = (containerSize / 2) - (childSize / 2) - dpToPx(8);
        int radius = Math.max(minRadius, Math.min(maxRadius, (int)(containerSize * radiusRatio)));

        // Layout each circular child
        for (int i = 0; i < circularChildCount; i++) {
            View child = getChildAt(i + 1); // Skip center view
            if (child.getVisibility() == GONE) continue;

            int cw = child.getMeasuredWidth();
            int ch = child.getMeasuredHeight();

            // Calculate angle: start from top (-90 degrees) and go clockwise
            double angle = Math.toRadians(-90 + (360.0 / circularChildCount) * i);

            // Calculate center position of child
            int childCenterX = centerX + (int)(radius * Math.cos(angle));
            int childCenterY = centerY + (int)(radius * Math.sin(angle));

            // Convert to top-left position
            int cl = childCenterX - cw / 2;
            int ct = childCenterY - ch / 2;

            child.layout(cl, ct, cl + cw, ct + ch);
        }
    }

    private int dpToPx(int dp) {
        return (int)(dp * getResources().getDisplayMetrics().density);
    }

    public void setRadiusRatio(float ratio) {
        this.radiusRatio = ratio;
        requestLayout();
    }
}

