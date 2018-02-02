package com.example.caioferrari.videoapp.provider;

import com.example.caioferrari.videoapp.dao.Videos;
import com.example.caioferrari.videoapp.interfaces.preview.PreviewMVP;
import com.example.caioferrari.videoapp.model.PreviewModel;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Caio Ferrari on 01/02/2018.
 */

public class PreviewProvider implements PreviewMVP.PreviewProvider {

    private PreviewMVP.PreviewView mPreviewView;
    private PreviewMVP.PreviewModel mPreviewModel;

    public PreviewProvider() {
        this.mPreviewModel = new PreviewModel(this);
    }

    @Override
    public void setView(final PreviewMVP.PreviewView previewView) {
        this.mPreviewView = previewView;
    }

    @Override
    public void deleteInternalVideoFile(final String videoPath) {
        final File videoFile = new File(videoPath);
        videoFile.delete();
    }

    @Override
    public void saveVideo(String videoPath) {
        final Date currentTime = Calendar.getInstance().getTime();

        final Videos video = new Videos();
        video.setCreatedDate(currentTime);
        video.setVideoPath(videoPath);

        mPreviewModel.saveVideoInDataBase(video);
    }

    @Override
    public void backToCameraFragment() {
        this.mPreviewView.backToCameraFragment();
    }
}
