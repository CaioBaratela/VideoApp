package com.example.caioferrari.videoapp.constansts;

import android.Manifest;

/**
 * Created by Caio Ferrari on 30/01/2018.
 */

public class CameraConstants {

    public static final int SENSOR_ORIENTATION_DEFAULT_DEGREES = 90;
    public static final int SENSOR_ORIENTATION_INVERSE_DEGREES = 270;

    public static final String FRAGMENT_DIALOG = "dialog";

    public static final String[] VIDEO_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
    };
}
