package com.example.caioferrari.videoapp.view.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.caioferrari.videoapp.R;
import com.example.caioferrari.videoapp.activity.CameraActivity;
import com.example.caioferrari.videoapp.dao.Videos;
import com.example.caioferrari.videoapp.interfaces.feed.FeedMVP;
import com.example.caioferrari.videoapp.provider.FeedProvider;
import com.example.caioferrari.videoapp.view.fragment.adapter.FeedAdapter;

import java.util.List;

public class FeedFragment extends Fragment implements FeedMVP.FeedView {

    private OnFragmentInteractionListener mListener;
    private Context mContext;

    private FeedMVP.FeedProvider mFeedProvider;

    public FeedFragment() {
        // Required empty public constructor
    }

    public static FeedFragment newInstance() {
        return new FeedFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if(mFeedProvider == null) {
            mFeedProvider = new FeedProvider();
        }

        mFeedProvider.setView(this);

        this.mContext = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feed, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final TextView btnShoot = view.findViewById(R.id.btn_shoot);
        final RecyclerView rvFeedMovies = view.findViewById(R.id.rv_feed_movies);

        configureRecycleView(rvFeedMovies);
        shootButtonClick(btnShoot);
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
    public void shootButtonClick(final TextView btnShoot) {
        btnShoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent cameraIntent = new Intent(mContext, CameraActivity.class);
                startActivity(cameraIntent);
            }
        });
    }

    @Override
    public List<Videos> getVideosList() {
        return mFeedProvider.getVideosList();
    }

    @Override
    public void configureRecycleView(RecyclerView rvFeedMovies) {

        final FeedAdapter feedAdapter = new FeedAdapter(mContext,getVideosList());

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext);
        rvFeedMovies.setLayoutManager(layoutManager);
        rvFeedMovies.setAdapter(feedAdapter);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
