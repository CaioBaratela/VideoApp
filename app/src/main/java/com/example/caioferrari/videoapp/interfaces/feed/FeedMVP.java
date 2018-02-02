package com.example.caioferrari.videoapp.interfaces.feed;

import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.example.caioferrari.videoapp.dao.Videos;

import java.util.List;

/**
 * Created by Caio Ferrari on 01/02/2018.
 */

public interface FeedMVP {

    interface FeedView {
        void shootButtonClick(TextView btnShoot);
        List<Videos> getVideosList();
        void configureRecycleView(RecyclerView rvFeedMovies);
        void swipeOnTouchListener(RecyclerView rvFeedMovies);
    }

    interface FeedProvider {
        void setView(FeedMVP.FeedView feedView);
        List<Videos> getVideosList();
    }

    interface FeedModel {
        List<Videos> getVideosList();
    }
}
