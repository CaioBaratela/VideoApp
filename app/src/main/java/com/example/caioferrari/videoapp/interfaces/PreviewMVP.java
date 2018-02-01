package com.example.caioferrari.videoapp.interfaces;

/**
 * Created by Caio Ferrari on 01/02/2018.
 */

public interface PreviewMVP {

    interface PreviewView {
        void deleteInternalVideoFile(String videoPath);
    }

    interface PreviewProvider{
        void setView(PreviewMVP.PreviewView previewView);
        void deleteInternalVideoFile(String videoPath);
    }

    interface PreviewModel{

    }
}
