package com.example.caioferrari.videoapp.provider;

import com.example.caioferrari.videoapp.dao.Videos;
import com.example.caioferrari.videoapp.interfaces.feed.FeedMVP;
import com.example.caioferrari.videoapp.model.FeedModel;

import java.util.List;

/**
 * Created by Caio Ferrari on 01/02/2018.
 */

public class FeedProvider implements FeedMVP.FeedProvider {

    private FeedMVP.FeedView mFeedView;
    private FeedMVP.FeedModel mFeedModel;

    public FeedProvider() {
        this.mFeedModel = new FeedModel(this);
    }

    @Override
    public void setView(final FeedMVP.FeedView feedView) {
        this.mFeedView = feedView;
    }

    @Override
    public List<Videos> getVideosList() {
        return mFeedModel.getVideosList();
    }
}
