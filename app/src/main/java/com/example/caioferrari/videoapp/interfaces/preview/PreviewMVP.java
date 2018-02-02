package com.example.caioferrari.videoapp.interfaces.preview;

import android.widget.TextView;

import com.example.caioferrari.videoapp.dao.Videos;

/**
 * Created by Caio Ferrari on 01/02/2018.
 */

public interface PreviewMVP {

    interface PreviewView {
        void deleteInternalVideoFile(String videoPath);
        void saveButtonOnClick(String videoPath, TextView btnSave);
        void backToCameraFragment();
    }

    interface PreviewProvider{
        void setView(PreviewMVP.PreviewView previewView);
        void deleteInternalVideoFile(String videoPath);
        void saveVideo(String videoPath);
        void backToCameraFragment();
    }

    interface PreviewModel{
        void saveVideoInDataBase(Videos videos);
    }
}
