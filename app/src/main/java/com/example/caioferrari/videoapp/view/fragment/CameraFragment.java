package com.example.caioferrari.videoapp.view.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraDevice;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v13.app.ActivityCompat;
import android.support.v13.app.FragmentCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.caioferrari.videoapp.R;
import com.example.caioferrari.videoapp.activity.FeedActivity;
import com.example.caioferrari.videoapp.constansts.CameraConstants;
import com.example.caioferrari.videoapp.interfaces.camera.CameraMVP;
import com.example.caioferrari.videoapp.provider.CameraProvider;
import com.example.caioferrari.videoapp.util.AutoFitTextureView;

import be.appfoundry.progressbutton.ProgressButton;

public class CameraFragment extends Fragment implements FragmentCompat.OnRequestPermissionsResultCallback, CameraMVP.CameraView {

    private Context context;

    private OnFragmentInteractionListener mListener;

    private static CameraMVP.CameraProvider mCameraProvider;

    private static final String TAG = "CameraFragment";
    private static final int REQUEST_VIDEO_PERMISSIONS = 1;


    private AutoFitTextureView mTextureView;
    private CameraDevice mCameraDevice;

    private TextureView.SurfaceTextureListener mSurfaceTextureListener
            = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture,
                                              int width, int height) {
            mCameraProvider.openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture,
                                                int width, int height) {
            mCameraProvider.configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        }

    };


    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            mCameraProvider.startPreview();
            mCameraProvider.getCameraOpenCloseLock().release();
            if (null != mTextureView) {
                mCameraProvider.configureTransform(mTextureView.getWidth(), mTextureView.getHeight());
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraProvider.getCameraOpenCloseLock().release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraProvider.getCameraOpenCloseLock().release();
            cameraDevice.close();
            mCameraDevice = null;
            Activity activity = getActivity();
            if (null != activity) {
                activity.finish();
            }
        }

    };

    public static CameraFragment newInstance() {
        return new CameraFragment();
    }

    public CameraFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.context = getActivity();

        if (mCameraProvider == null) {
            mCameraProvider = new CameraProvider();
        }

        mCameraProvider.setView(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTextureView = view.findViewById(R.id.camera_texture);
        final ProgressButton pbRecorderVideo = view.findViewById(R.id.pb_recorder_video);
        final TextView btnFeed = view.findViewById(R.id.btn_feed);

        this.setRecorderButtonListener(pbRecorderVideo);
        this.feedButtonClick(btnFeed);
    }

    @Override
    public void onResume() {
        super.onResume();
        mCameraProvider.startBackgroundThread();
        if (mTextureView.isAvailable()) {
            mCameraProvider.openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    @Override
    public void onPause() {
        mCameraProvider.closeCamera();
        mCameraProvider.stopBackgroundThread();
        super.onPause();
    }

    // TODO: Rename method, update argument and hook method into UI event

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public boolean shouldShowRequestPermissionRationale(String[] permissions) {
        for (String permission : permissions) {
            if (FragmentCompat.shouldShowRequestPermissionRationale(this, permission)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void requestVideoPermissions() {
        if (shouldShowRequestPermissionRationale(CameraConstants.VIDEO_PERMISSIONS)) {
            new ConfirmationDialog().show(getChildFragmentManager(), CameraConstants.FRAGMENT_DIALOG);
        } else {
            FragmentCompat.requestPermissions(this, CameraConstants.VIDEO_PERMISSIONS, REQUEST_VIDEO_PERMISSIONS);
        }
    }

    @Override
    public boolean hasPermissionsGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(getActivity(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult");
        if (requestCode == REQUEST_VIDEO_PERMISSIONS) {
            if (grantResults.length == CameraConstants.VIDEO_PERMISSIONS.length) {
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        ErrorDialog.newInstance("Permission Denied")
                                .show(getChildFragmentManager(), CameraConstants.FRAGMENT_DIALOG);
                        break;
                    }
                }
            } else {
                ErrorDialog.newInstance("Permission Denied")
                        .show(getChildFragmentManager(), CameraConstants.FRAGMENT_DIALOG);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void goToPreviewFragment(final String videoPath) {
        getActivity()
                .getFragmentManager()
                .beginTransaction()
                .replace(R.id.container, PreviewFragment.newInstance(videoPath), TAG)
                .addToBackStack(TAG)
                .commit();
    }

    @Override
    public Context getContext() {
        return this.context;
    }

    @Override
    public Activity getCameraActivity() {
        return this.getActivity();
    }

    @Override
    public AutoFitTextureView getTextureView() {
        return this.mTextureView;
    }

    @Override
    public CameraDevice.StateCallback getStateCallback() {
        return this.mStateCallback;
    }

    @Override
    public CameraDevice getCameraDevice() {
        return this.mCameraDevice;
    }

    @Override
    public void setCameraDevice(CameraDevice cameraDevice) {
        this.mCameraDevice = cameraDevice;
    }

    @Override
    public void setRecorderButtonListener(final ProgressButton pbRecorderVideo) {
        pbRecorderVideo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mCameraProvider.startRecordingVideo();
                        pbRecorderVideo.setColor(Color.parseColor("#" +
                                "FA2422"));
                        pbRecorderVideo.setStrokeColor(Color.parseColor("#DF634A"));
                        break;

                    case MotionEvent.ACTION_UP:
                        if (mCameraProvider.isRecordingVideo()) {
                            goToPreviewFragment(mCameraProvider.stopRecordingVideo());
                        }
                        break;
                }
                return true;
            }

        });
    }

    @Override
    public void feedButtonClick(TextView btnFeed) {
        btnFeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent feedIntent = new Intent(context, FeedActivity.class);
                startActivity(feedIntent);
            }
        });
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public static class ErrorDialog extends DialogFragment {

        private static final String ARG_MESSAGE = "message";

        public static ErrorDialog newInstance(String message) {
            ErrorDialog dialog = new ErrorDialog();
            Bundle args = new Bundle();
            args.putString(ARG_MESSAGE, message);
            dialog.setArguments(args);
            return dialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Activity activity = getActivity();
            return new AlertDialog.Builder(activity)
                    .setMessage(getArguments().getString(ARG_MESSAGE))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            activity.finish();
                        }
                    })
                    .create();
        }

    }

    public static class ConfirmationDialog extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Fragment parent = getParentFragment();
            return new AlertDialog.Builder(getActivity())
                    .setMessage("Permission")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FragmentCompat.requestPermissions(parent, CameraConstants.VIDEO_PERMISSIONS,
                                    REQUEST_VIDEO_PERMISSIONS);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    parent.getActivity().finish();
                                }
                            })
                    .create();
        }

    }

}
