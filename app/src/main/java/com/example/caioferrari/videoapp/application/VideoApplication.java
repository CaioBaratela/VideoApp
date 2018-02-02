package com.example.caioferrari.videoapp.application;

import android.app.Application;

import com.example.caioferrari.videoapp.dao.DaoMaster;
import com.example.caioferrari.videoapp.dao.DaoSession;
import com.example.caioferrari.videoapp.database.DbOpenHelper;

/**
 * Created by Caio Ferrari on 01/02/2018.
 */

public class VideoApplication extends Application {

    public static DaoSession mDaoSession;

    private static final String DATA_BASE_NAME = "video_app.db";

    @Override
    public void onCreate() {
        super.onCreate();
        this.mDaoSession = new DaoMaster(
                new DbOpenHelper(this, DATA_BASE_NAME)
                        .getWritableDb())
                .newSession();
    }

    public static DaoSession getDaoSession() {
        return mDaoSession;
    }
}
