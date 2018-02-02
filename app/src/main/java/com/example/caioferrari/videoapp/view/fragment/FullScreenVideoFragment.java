package com.example.caioferrari.videoapp.view.fragment;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

import com.example.caioferrari.videoapp.R;


public class FullScreenVideoFragment extends Fragment {

    private static final String VIDEO_PATH = "video_path";

    private String mVideoPath;
    private Context mContext;

    private OnFragmentInteractionListener mListener;

    public FullScreenVideoFragment() {
        // Required empty public constructor
    }

    public static FullScreenVideoFragment newInstance(String videoPath) {
        FullScreenVideoFragment fragment = new FullScreenVideoFragment();
        Bundle args = new Bundle();
        args.putString(VIDEO_PATH, videoPath);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mVideoPath = getArguments().getString(VIDEO_PATH);
        }

        this.mContext = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_full_screen_video, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final VideoView vvFullScreen = view.findViewById(R.id.vv_full_screen);

        final MediaController mediaController = new MediaController(mContext);

        mediaController.setVisibility(View.GONE);

        vvFullScreen.setVideoPath(mVideoPath);
        vvFullScreen.setMediaController(mediaController);
        vvFullScreen.requestFocus();
        vvFullScreen.start();

        vvFullScreen.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
