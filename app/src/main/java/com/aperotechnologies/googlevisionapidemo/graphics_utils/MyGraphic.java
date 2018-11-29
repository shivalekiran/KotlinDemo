package com.aperotechnologies.googlevisionapidemo.graphics_utils;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.constraint.solver.widgets.Rectangle;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.text.FirebaseVisionText;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Graphic instance for rendering TextBlock position, size, and ID within an associated graphic
 * overlay view.
 */
public class MyGraphic extends GraphicOverlay.Graphic {

    private static final String TAG = "MyGraphic";
    private static final int TEXT_COLOR = Color.RED;
    private static final float TEXT_SIZE = 54.0f;
    private static final float STROKE_WIDTH = 4.0f;

    private Paint rectPaint = new Paint();
    private Paint textPaint = new Paint();
    private Paint pointPaint = new Paint();
    private Rect mRect;
    private FirebaseVisionText.Element element = null;
    private FirebaseVisionPoint firebaseVisionPoint;
    int x;
    int y;
    String text;

    public MyGraphic(GraphicOverlay overlay, Object type, FirebaseVisionText.Element element) {
        super(overlay, type);

        this.element = element;
        rectPaint.setColor(TEXT_COLOR);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(STROKE_WIDTH);

        textPaint.setColor(TEXT_COLOR);
        textPaint.setTextSize(TEXT_SIZE);
        // Redraw the overlay, as this graphic has been added.
        postInvalidate();
    }


    public MyGraphic(@Nullable GraphicOverlay graphic_overlay, Object type, @NotNull Rect rect, Paint paint) {
        super(graphic_overlay, type);
        rectPaint = paint;
        mRect = rect;
        postInvalidate();
    }

    public MyGraphic(@Nullable GraphicOverlay graphic_overlay, Object type, FirebaseVisionPoint firebaseVisionPoint, Paint paint) {
        super(graphic_overlay, type);
        pointPaint = paint;
        this.firebaseVisionPoint = firebaseVisionPoint;
        postInvalidate();
    }

    public MyGraphic(@Nullable GraphicOverlay graphic_overlay, @NotNull Object point, int x, int y, @NotNull Paint paint) {
        super(graphic_overlay, point);
        this.x = x;
        this.y = y;
        pointPaint = paint;
    }

    public MyGraphic(@Nullable GraphicOverlay graphic_overlay, @NotNull Object point, Rect rect, String value, @NotNull Paint paint) {
        super(graphic_overlay, point);
        textPaint = paint;
        mRect = rect;
        text = value;
    }

    /**
     * Draws the text block annotations for position, size, and raw value on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        try {
            if (object instanceof FirebaseVisionText.Element) {
                // Draws the bounding box around the TextBlock.
                RectF rect = new RectF(element.getBoundingBox());
                canvas.drawRect(rect, rectPaint);
                // Renders the text at the bottom of the box.
                canvas.drawText(element.getText(), rect.left, rect.bottom, textPaint);
            } else if (object instanceof Rectangle) {
                canvas.drawRect(mRect, rectPaint);
            } else if (object instanceof Point) {
                if (firebaseVisionPoint != null) {
                    canvas.drawPoint(firebaseVisionPoint.getX(), firebaseVisionPoint.getY(), pointPaint);
                } else {
                    canvas.drawPoint(x, y, pointPaint);

                }
            } else if (object instanceof TextView) {
                canvas.drawText(text, mRect.left, mRect.bottom, textPaint);
            }
        } catch (Exception e) {
            Log.e(TAG, "draw: ", e);
        }


    }
}
