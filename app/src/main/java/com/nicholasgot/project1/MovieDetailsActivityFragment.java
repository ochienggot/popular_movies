package com.nicholasgot.project1;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieDetailsActivityFragment extends Fragment {
    private final String LOG_TAG = MovieDetailsActivity.class.getSimpleName();
    private TextView mTextView;

    public MovieDetailsActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_details, container, false);

        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            String movieId = intent.getStringExtra(Intent.EXTRA_TEXT);
            mTextView = (TextView) rootView.findViewById(R.id.movie_details_textview);

            // Request for details of movie with this id
            FetchMovieDetails fetchMovieDetails = new FetchMovieDetails(getContext(), mTextView, getActivity());
            fetchMovieDetails.execute(movieId);
        }

        return rootView;
    }
}
