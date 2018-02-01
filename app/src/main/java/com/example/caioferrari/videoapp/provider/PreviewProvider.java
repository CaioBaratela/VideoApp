package com.example.caioferrari.videoapp.provider;

import com.example.caioferrari.videoapp.interfaces.PreviewMVP;

import java.io.File;

/**
 * Created by Caio Ferrari on 01/02/2018.
 */

public class PreviewProvider implements PreviewMVP.PreviewProvider {

    private PreviewMVP.PreviewView previewView;

    public PreviewProvider() {
    }

    @Override
    public void setView(final PreviewMVP.PreviewView previewView) {
        this.previewView = previewView;
    }

    @Override
    public void deleteInternalVideoFile(final String videoPath) {
        final File videoFile = new File(videoPath);
        videoFile.delete();
    }
}
