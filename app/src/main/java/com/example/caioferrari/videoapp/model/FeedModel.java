package com.example.caioferrari.videoapp.model;

import com.example.caioferrari.videoapp.application.VideoApplication;
import com.example.caioferrari.videoapp.dao.Videos;
import com.example.caioferrari.videoapp.dao.VideosDao;
import com.example.caioferrari.videoapp.interfaces.feed.FeedMVP;

import java.util.List;

/**
 * Created by Caio Ferrari on 01/02/2018.
 */

public class FeedModel implements FeedMVP.FeedModel {

    private FeedMVP.FeedProvider mFeedProvider;

    public FeedModel(final FeedMVP.FeedProvider mFeedProvider) {
        this.mFeedProvider = mFeedProvider;
    }

    @Override
    public List<Videos> getVideosList() {
        return VideoApplication.getDaoSession()
                .getVideosDao()
                .queryBuilder()
                .orderDesc(VideosDao.Properties.CreatedDate)
                .list();
    }
}
