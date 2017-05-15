package camera;

/**
 * Created by avi on 2/16/17.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;

import com.credenceid.latentcamera.CameraActivity;

public class CameraSurfaceView extends SurfaceView {

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private double mAspectRatio = 1280 / 720;

    public void setAspectRatio(double ratio) {
        if (ratio <= 0.0)
            throw new IllegalArgumentException();
        if (mAspectRatio != ratio) {
            mAspectRatio = ratio;
            requestLayout();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        int previewWidth = MeasureSpec.getSize(widthSpec);
        int previewHeight = MeasureSpec.getSize(heightSpec);

        // Get the padding of the border background.
        int hPadding = getPaddingLeft() + getPaddingRight();
        int vPadding = getPaddingTop() + getPaddingBottom();

        // Resize the preview frame with correct aspect ratio.
        previewWidth -= hPadding;
        previewHeight -= vPadding;
        if (previewWidth > previewHeight * mAspectRatio)
            previewWidth = (int) (previewHeight * mAspectRatio + .5);
        else
            previewHeight = (int) (previewWidth / mAspectRatio + .5);

        // Add the padding of the border.
        previewWidth += hPadding;
        previewHeight += vPadding;

        // Ask children to follow the new preview dimension.
        super.onMeasure(MeasureSpec.makeMeasureSpec(previewWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(previewHeight, MeasureSpec.EXACTLY));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // If user touched SurfaceView
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            // Grab touch coordinates
            float x = event.getX();
            float y = event.getY();
            float touchMajor = event.getTouchMajor();
            float touchMinor = event.getTouchMinor();
            // Create Rect of where used touched
            Rect touchRect = new Rect(
                    (int) (x - touchMajor / 2),
                    (int) (y - touchMinor / 2),
                    (int) (x + touchMajor / 2),
                    (int) (y + touchMinor / 2));

            // Run function in CameraActivity.class to actually instantiate touch to focus
            ((CameraActivity) getContext()).touchFocus(touchRect);
        }
        return true;
    }
}