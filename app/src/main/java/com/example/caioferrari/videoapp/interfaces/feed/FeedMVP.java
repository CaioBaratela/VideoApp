package com.example.caioferrari.videoapp.interfaces.feed;

import android.widget.TextView;

/**
 * Created by Caio Ferrari on 01/02/2018.
 */

public interface FeedMVP {

    interface FeedView {
        void shootButtonClick(TextView btnShoot);
    }

    interface FeedProvier {

    }

    interface FeedModel {

    }
}
