package com.example.caioferrari.videoapp.view.fragment.adapter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.media.MediaPlayer;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.caioferrari.videoapp.R;
import com.example.caioferrari.videoapp.activity.FeedActivity;
import com.example.caioferrari.videoapp.dao.Videos;
import com.example.caioferrari.videoapp.view.fragment.FullScreenVideoFragment;
import com.malmstein.fenster.controller.SimpleMediaFensterPlayerController;
import com.malmstein.fenster.view.FensterVideoView;

import java.util.List;

/**
 * Created by Caio Ferrari on 01/02/2018.
 */

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.ViewHolder> {

    private static final String TAG = "FeedFragment";

    private final Context mContext;
    private final List<Videos> mVideos;

    public FeedAdapter(Context mContext, List<Videos> mVideos) {
        this.mContext = mContext;
        this.mVideos = mVideos;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.from(mContext).inflate(R.layout.adapter_feed,parent,false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Videos videos = mVideos.get(position);
        holder.bind(videos);
    }

    @Override
    public int getItemCount() {
        return mVideos.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final FensterVideoView vvFeed;
        final ConstraintLayout ccFeed;
        final SimpleMediaFensterPlayerController playerController;

        ViewHolder(View view) {
            super(view);
            vvFeed = view.findViewById(R.id.vv_feed);
            ccFeed = view.findViewById(R.id.cc_feed);
            playerController = view.findViewById(R.id.play_video_controller);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }

        void bind(final Videos videos) {

            vvFeed.setVideo(videos.getVideoPath());
            playerController.setVisibility(View.GONE);
            vvFeed.setMediaController(playerController);
            vvFeed.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.setLooping(true);
                    vvFeed.start();
                }
            });


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Fragment fragment = FullScreenVideoFragment.newInstance(videos.getVideoPath());
                    FragmentManager fm = ((FeedActivity) mContext).getFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.replace(R.id.container, fragment,TAG);
                    ft.addToBackStack(TAG);
                    ft.commit();
                }
            });
        }
    }
}
