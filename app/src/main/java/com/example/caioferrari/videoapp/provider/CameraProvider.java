package com.example.caioferrari.videoapp.provider;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.widget.Toast;

import com.example.caioferrari.videoapp.constansts.CameraConstants;
import com.example.caioferrari.videoapp.interfaces.camera.CameraMVP;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * @author Created by Caio Ferrari on 30/01/2018.
 */

public class CameraProvider implements CameraMVP.CameraProvider {

    private CameraMVP.CameraView cameraView;

    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    private CaptureRequest.Builder mPreviewBuilder;
    private CameraCaptureSession mPreviewSession;
    private boolean mIsRecordingVideo;
    private String mNextVideoAbsolutePath;

    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    private static final SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();
    private static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();

    private Integer mSensorOrientation;
    private Size mVideoSize;
    private Size mPreviewSize;
    private MediaRecorder mMediaRecorder;

    static {
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_0, 90);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_90, 0);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_180, 270);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    static {
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 180);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 0);
    }


    @Override
    public void setView(CameraMVP.CameraView cameraView) {
        this.cameraView = cameraView;
    }

    @Override
    public Size chooseVideoSize(Size[] choices) {
        for (Size size : choices) {
            if (size.getWidth() == size.getHeight() * 4 / 3 && size.getWidth() <= 1080) {
                return size;
            }
        }
        return choices[choices.length - 1];
    }

    @Override
    public Size chooseOptimalSize(Size[] choices, int width, int height, Size aspectRatio) {
        // Collect the supported resolutions that are at least as big as the preview Surface
        final List<Size> bigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * h / w &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }

        // Pick the smallest of those, assuming we found any
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else {
            return choices[0];
        }
    }

    @Override
    public void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    @Override
    public void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void openCamera(int width, int height) {
        if (!cameraView.hasPermissionsGranted(CameraConstants.VIDEO_PERMISSIONS)) {
            cameraView.requestVideoPermissions();
            return;
        }
        final Activity activity = cameraView.getCameraActivity();
        if (null == activity || activity.isFinishing()) {
            return;
        }
        final CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            final String cameraId = manager.getCameraIdList()[0];

            // Choose the sizes for camera preview and video recording
            final CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            final StreamConfigurationMap map = characteristics
                    .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            this.mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            if (map == null) {
                throw new RuntimeException("Cannot get available preview/video sizes");
            }
            this.mVideoSize = chooseVideoSize(map.getOutputSizes(MediaRecorder.class));
            this.mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                    width, height, mVideoSize);

            int orientation = cameraView.getContext().getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                cameraView.getTextureView().setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            } else {
                cameraView.getTextureView().setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
            }
            configureTransform(width, height);
            this.mMediaRecorder = new MediaRecorder();
            manager.openCamera(cameraId, cameraView.getStateCallback(), null);
        } catch (CameraAccessException e) {
            activity.finish();
        } catch (NullPointerException ignored) {

        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.");
        }
    }

    @Override
    public Semaphore getCameraOpenCloseLock() {
        return this.mCameraOpenCloseLock;
    }

    @Override
    public void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            closePreviewSession();
            if (null != cameraView.getCameraDevice()) {
                cameraView.getCameraDevice().close();
                cameraView.setCameraDevice(null);
            }
            if (null != mMediaRecorder) {
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.");
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    @Override
    public void startPreview() {
        if (null == cameraView.getCameraDevice() || !cameraView.getTextureView().isAvailable() || null == mPreviewSize) {
            return;
        }
        try {
            closePreviewSession();
            SurfaceTexture texture = cameraView.getTextureView().getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            mPreviewBuilder = cameraView.getCameraDevice().createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            final Surface previewSurface = new Surface(texture);
            mPreviewBuilder.addTarget(previewSurface);

            cameraView.getCameraDevice().createCaptureSession(Collections.singletonList(previewSurface),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            mPreviewSession = session;
                            updatePreview();
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            Activity activity = cameraView.getCameraActivity();
                            if (null != activity) {
                                Toast.makeText(activity, "Failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updatePreview() {
        if (null == cameraView.getCameraDevice()) {
            return;
        }
        try {
            setUpCaptureRequestBuilder(mPreviewBuilder);
            HandlerThread thread = new HandlerThread("CameraPreview");
            thread.start();
            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setUpCaptureRequestBuilder(CaptureRequest.Builder builder) {
        builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
    }

    @Override
    public void configureTransform(int viewWidth, int viewHeight) {
        final Activity activity = cameraView.getCameraActivity();
        if (null == cameraView.getTextureView() || null == mPreviewSize || null == activity) {
            return;
        }
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        final Matrix matrix = new Matrix();
        final  RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        final RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        }
        cameraView.getTextureView().setTransform(matrix);
    }

    @Override
    public void setUpMediaRecorder() throws IOException {
        final Activity activity = cameraView.getCameraActivity();
        if (null == activity) {
            return;
        }
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        if (mNextVideoAbsolutePath == null || mNextVideoAbsolutePath.isEmpty()) {
            mNextVideoAbsolutePath = getVideoFilePath(cameraView.getContext());
        }
        mMediaRecorder.setOutputFile(mNextVideoAbsolutePath);
        mMediaRecorder.setVideoEncodingBitRate(10000000);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        switch (mSensorOrientation) {
            case CameraConstants.SENSOR_ORIENTATION_DEFAULT_DEGREES:
                mMediaRecorder.setOrientationHint(DEFAULT_ORIENTATIONS.get(rotation));
                break;
            case CameraConstants.SENSOR_ORIENTATION_INVERSE_DEGREES:
                mMediaRecorder.setOrientationHint(INVERSE_ORIENTATIONS.get(rotation));
                break;
        }
        mMediaRecorder.prepare();
    }

    @Override
    public String getVideoFilePath(Context context) {
        final File dir = context.getExternalFilesDir(null);
        return (dir == null ? "" : (dir.getAbsolutePath() + "/"))
                + System.currentTimeMillis() + ".mp4";
    }

    @Override
    public void startRecordingVideo() {
        if (null == cameraView.getCameraDevice() || !cameraView.getTextureView().isAvailable() || null == mPreviewSize) {
            return;
        }
        try {
            closePreviewSession();
            setUpMediaRecorder();
            SurfaceTexture texture = cameraView.getTextureView().getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            mPreviewBuilder = cameraView.getCameraDevice().createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            final List<Surface> surfaces = new ArrayList<>();

            // Set up Surface for the camera preview
            final Surface previewSurface = new Surface(texture);
            surfaces.add(previewSurface);
            mPreviewBuilder.addTarget(previewSurface);

            // Set up Surface for the MediaRecorder
            final Surface recorderSurface = mMediaRecorder.getSurface();
            surfaces.add(recorderSurface);
            mPreviewBuilder.addTarget(recorderSurface);

            // Start a capture session
            // Once the session starts, we can update the UI and start recording
            cameraView.getCameraDevice().createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    mPreviewSession = cameraCaptureSession;
                    updatePreview();
                    cameraView.getCameraActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // UI
                            mIsRecordingVideo = true;

                            // Start recording
                            mMediaRecorder.start();
                        }
                    });
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Activity activity = cameraView.getCameraActivity();
                    if (null != activity) {
                        Toast.makeText(activity, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void closePreviewSession() {
        if (mPreviewSession != null) {
            mPreviewSession.close();
            mPreviewSession = null;
        }
    }

    @Override
    public String stopRecordingVideo() {
        // UI
        String videoPath = "";
        mIsRecordingVideo = false;
        // Stop recording
        mMediaRecorder.stop();
        mMediaRecorder.reset();

        Activity activity = cameraView.getCameraActivity();
        if (null != activity) {
            videoPath = mNextVideoAbsolutePath;
        }
        mNextVideoAbsolutePath = null;
        return videoPath;
    }

    @Override
    public boolean isRecordingVideo() {
        return this.mIsRecordingVideo;
    }

    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }
}
