package com.credenceid.latentcamera;

/**
 * Created by avi on 2/9/17.
 */

import android.app.Activity;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.TextView;
import android.widget.Toast;

import camera.CameraSurfaceView;
import camera.DrawingView;

public class CameraActivity extends Activity implements SurfaceHolder.Callback {
    public Camera camera;
    CameraSurfaceView cameraSurfaceView;
    SurfaceHolder surfaceHolder;
    DrawingView drawingView;

    TextView statusView;

    boolean previewing = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fingerprint_camera_page);

        getWindow().setFormat(PixelFormat.UNKNOWN);

        this.drawingView = (DrawingView) findViewById(R.id.drawingView);
        this.drawingView = (DrawingView) findViewById(R.id.drawingView);
        this.cameraSurfaceView = (CameraSurfaceView) findViewById(R.id.camerapreview);
        this.statusView = (TextView) findViewById(R.id.status_view);

        this.surfaceHolder = cameraSurfaceView.getHolder();
        this.surfaceHolder.addCallback(this);
        this.surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        this.previewing = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (previewing) {
            camera.stopPreview();
            previewing = false;
        }
        initializePreview(width, height);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        camera = Camera.open();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
            previewing = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
            previewing = false;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {

            Log.d("getBestPreviewSize", size.width + ", " + size.height);

            if (size.width <= width && size.height <= height) {
                if (result == null) result = size;
                else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;
                    if (newArea > resultArea) result = size;
                }
            }
        }
        return (result);
    }

    private void initializePreview(int width, int height) {
        if (camera != null && surfaceHolder.getSurface() != null) {
            try {
                Camera.Parameters parameters = camera.getParameters();
                Camera.Size size = getBestPreviewSize(width, height, parameters);

                camera.setPreviewDisplay(surfaceHolder);
                if (size != null) {
                    parameters.setPreviewSize(size.width, size.height);
                    cameraSurfaceView.setAspectRatio((size.width) / (double) (size.height));
                    //cameraSurfaceView.getLayoutParams().width = (int) (size.width * 1.1);
                    //cameraSurfaceView.getLayoutParams().height = (int) (size.height * 1.1);
                    camera.setParameters(parameters);
                }

                camera.setDisplayOrientation(0);

                camera.setPreviewDisplay(surfaceHolder);
                camera.startPreview();
            } catch (Throwable t) {
                Log.e("PreviewDemo-Callback", "Exception in setPreviewDisplay()", t);
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    /* ------------------------------------------------------------------------------------------ */
    /* ------------------------------ Touch To Focus Implementation ----------------------------- */
    /* ------------------------------------------------------------------------------------------ */
    public void touchFocus(final Rect tfocusRect) {
        // Trident Device: The T2R device already auto-focuses the entire frame, so we do not need
        // to specify a Rect. If we were to run this block of code on the T2R it would crash the program.
        // This is a "feature" of Android 6.0, NOT specific to Tridents
        /*
        if (TRIDENT) {
            Parameters para = camera.getParameters();
            para.setFocusAreas(focusList);
            para.setMeteringAreas(focusList);
            camera.setParameters(para);
        }
        */

        // Tell user we are now autofocusing
        statusView.setText("Autofocusing...");
        // Call camera AutoFocus & pass callback to be called when auto focus finishes
        camera.autoFocus(myAutoFocusCallback);
        // Tell our drawing view we have a touch in the given Rect
        drawingView.setHaveTouch(true, tfocusRect);
        // Tell our drawing view to Update
        drawingView.invalidate();
    }

    AutoFocusCallback myAutoFocusCallback = new AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean arg0, Camera arg1) {
            // Set drawing view to remove green circle since autofocus has finished
            drawingView.setHaveTouch(false, new Rect(0, 0, 0, 0));
            // Update drawing view
            drawingView.invalidate();
            statusView.setText("");
        }
    };
}