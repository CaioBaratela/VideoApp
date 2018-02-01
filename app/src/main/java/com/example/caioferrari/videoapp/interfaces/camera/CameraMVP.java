package com.example.caioferrari.videoapp.interfaces.camera;

import android.app.Activity;
import android.content.Context;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.util.Size;
import android.widget.TextView;

import com.example.caioferrari.videoapp.util.AutoFitTextureView;

import java.io.IOException;
import java.util.concurrent.Semaphore;

import be.appfoundry.progressbutton.ProgressButton;

/**
 * @author Created by Caio Ferrari on 30/01/2018.
 */

public interface CameraMVP {

    interface CameraView {
        void goToPreviewFragment(final String videoPath);
        boolean shouldShowRequestPermissionRationale(String[] permissions);
        void requestVideoPermissions();
        boolean hasPermissionsGranted(String[] permissions);
        Context getContext();
        Activity getCameraActivity();
        AutoFitTextureView getTextureView();
        CameraDevice.StateCallback getStateCallback();
        CameraDevice getCameraDevice();
        void setCameraDevice(CameraDevice cameraDevice);
        void setRecorderButtonListener(ProgressButton pbRecorderVideo);
        void feedButtonClick(TextView btnFeed);
    }

    interface CameraProvider {
        void setView(CameraView cameraView);
        Size chooseVideoSize(Size[] choices);
        Size chooseOptimalSize(Size[] choices, int width, int height, Size aspectRatio);
        void startBackgroundThread();
        void stopBackgroundThread();
        void openCamera(int width, int height);
        Semaphore getCameraOpenCloseLock();
        void closeCamera();
        void startPreview();
        void updatePreview();
        void setUpCaptureRequestBuilder(CaptureRequest.Builder builder);
        void configureTransform(int viewWidth, int viewHeight);
        void setUpMediaRecorder() throws IOException;
        String getVideoFilePath(Context context);
        void startRecordingVideo();
        void closePreviewSession();
        String stopRecordingVideo();
        boolean isRecordingVideo();
    }

    interface CameraModel {
    }
}
