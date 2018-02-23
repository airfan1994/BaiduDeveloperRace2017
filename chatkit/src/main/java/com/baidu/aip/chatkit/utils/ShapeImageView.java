/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */

package com.baidu.aip.chatkit.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * ImageView with mask what described with Bézier Curves
 */

public class ShapeImageView extends ImageView {
    private Path path;

    public ShapeImageView(Context context) {
        super(context);
    }

    public ShapeImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        path = new Path();
        float halfWidth = (float) w / 2f;
        float firstParam = (float) w * 0.1f;
        float secondParam = (float) w * 0.8875f;

        // Bézier Curves
        path.moveTo(halfWidth, (float) w);
        path.cubicTo(firstParam, (float) w, 0, secondParam, 0, halfWidth);
        path.cubicTo(0, firstParam, firstParam, 0, halfWidth, 0);
        path.cubicTo(secondParam, 0, (float) w, firstParam, (float) w, halfWidth);
        path.cubicTo((float) w, secondParam, secondParam, (float) w, halfWidth, (float) w);
        path.close();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (canvas != null) {
            canvas.clipPath(path);
            super.onDraw(canvas);
        }
    }
}
