package com.example.caioferrari.videoapp.view.fragment;

import android.app.Fragment;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

import com.example.caioferrari.videoapp.R;
import com.example.caioferrari.videoapp.interfaces.PreviewMVP;
import com.example.caioferrari.videoapp.provider.PreviewProvider;

public class PreviewFragment extends Fragment implements PreviewMVP.PreviewView {

    private final static String VIDEO_PATH_FILE = "video_path_file";
    private String mVideoPath;
    private Context mContext;

    private PreviewMVP.PreviewProvider mPreviewProvider;

    private OnFragmentInteractionListener mListener;

    public PreviewFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static PreviewFragment newInstance(String videoPath) {
        final PreviewFragment previewFragment = new PreviewFragment();
        final Bundle args = new Bundle();
        args.putString(VIDEO_PATH_FILE, videoPath);
        previewFragment.setArguments(args);
        return previewFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
          this.mVideoPath = getArguments().getString(VIDEO_PATH_FILE);
        }

        if(mPreviewProvider == null) {
            this.mPreviewProvider = new PreviewProvider();
        }

        mPreviewProvider.setView(this);

        this.mContext = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_preview, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final VideoView vvVideoPreview = view.findViewById(R.id.vv_preview);
        final MediaController mediaController = new MediaController(mContext);

        mediaController.setVisibility(View.GONE);

        vvVideoPreview.setVideoPath(mVideoPath);
        vvVideoPreview.setMediaController(mediaController);
        vvVideoPreview.requestFocus();
        vvVideoPreview.start();

        vvVideoPreview.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });
    }

    @Override
    public void onPause() {
        this.deleteInternalVideoFile(mVideoPath);
        super.onPause();
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

    @Override
    public void deleteInternalVideoFile(String videoPath) {
        this.mPreviewProvider.deleteInternalVideoFile(videoPath);
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
