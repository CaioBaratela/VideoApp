package com.example.caioferrari.videoapp.model;

import com.example.caioferrari.videoapp.application.VideoApplication;
import com.example.caioferrari.videoapp.dao.Videos;
import com.example.caioferrari.videoapp.interfaces.preview.PreviewMVP;

/**
 * Created by Caio Ferrari on 01/02/2018.
 */

public class PreviewModel implements PreviewMVP.PreviewModel {

    private PreviewMVP.PreviewProvider mPreviewProvider;

    public PreviewModel(PreviewMVP.PreviewProvider mPreviewProvider) {
        this.mPreviewProvider = mPreviewProvider;
    }

    @Override
    public void saveVideoInDataBase(Videos videos) {
        VideoApplication.getDaoSession().getVideosDao().insert(videos);
        mPreviewProvider.backToCameraFragment();
    }
}
