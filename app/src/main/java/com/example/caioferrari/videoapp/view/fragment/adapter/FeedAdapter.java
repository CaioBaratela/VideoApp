package com.example.caioferrari.videoapp.view.fragment.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

import com.example.caioferrari.videoapp.R;
import com.example.caioferrari.videoapp.dao.Videos;

import java.util.List;

/**
 * Created by Caio Ferrari on 01/02/2018.
 */

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.ViewHolder> {

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

        final MediaController mediaController = new MediaController(mContext);

        mediaController.setVisibility(View.GONE);

        holder.vvFeed.setVideoPath(videos.getVideoPath());
        holder.vvFeed.setMediaController(mediaController);
        holder.vvFeed.requestFocus();
        holder.vvFeed.start();
    }

    @Override
    public int getItemCount() {
        return mVideos.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final VideoView vvFeed;

        ViewHolder(View view) {
            super(view);
            vvFeed = view.findViewById(R.id.vv_feed);
        }
    }
}
