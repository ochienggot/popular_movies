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
            FetchMovieDetails fetchMovieDetails = new FetchMovieDetails();
            fetchMovieDetails.execute(movieId);
        }

        return rootView;
    }

    public class FetchMovieDetails extends AsyncTask<String, Void, String[]> {

        @Override
        protected String[] doInBackground(String... params) {
            HttpURLConnection httpURLConnection = null;
            BufferedReader bufferedReader = null;
            String jsonStr;

            String API_KEY = "api_key";
            String id = params[0];

            try {
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("https")
                        .authority("api.themoviedb.org")
                        .appendPath("3")
                        .appendPath("movie")
                        .appendPath(id)
                        .appendQueryParameter(API_KEY, "API_KEY");

                String myUrl = builder.build().toString();

                URL url = new URL(myUrl);
                Log.v(LOG_TAG, "Movie URL: " + url);

                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.connect();

                // Read response into a buffer
                InputStream inputStream = httpURLConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                if (inputStream == null) {
                    return null;
                }

                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line = bufferedReader.readLine();
                while (line != null) {
                    buffer.append(line + "\n");
                    line = bufferedReader.readLine();
                }

                jsonStr = buffer.toString();
                return getMovieDataFromJson(jsonStr);

            } catch (MalformedURLException m) {
                Log.e(LOG_TAG, "Malformed URL: " + m);
                return null;

            } catch (IOException e) {
                Log.e(LOG_TAG, "IO error: " + e);
                return null;

            } catch (JSONException je) {
                Log.e(LOG_TAG, "Json parsing error: " + je);
                return null;

            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }

                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "Error closing buffer: " + e);
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(String[] movieDetails) {
            super.onPostExecute(movieDetails);

            String imageUri = "http://image.tmdb.org/t/p/" + "w185/" + movieDetails[1];
            ImageView imageView = (ImageView) getActivity().findViewById(R.id.image_view);
            displayMoviePoster(imageUri, imageView);

            mTextView.setText(movieDetails[0]);
        }

        protected void displayMoviePoster(String uri, ImageView imageView) {
            Picasso.with(getContext()).load(uri).into(imageView);
        }

        private String[] getMovieDataFromJson(String jsonStr) throws JSONException {
            // TODO:
            final String ORIGINAL_TITLE = "original_title";
            final String SYNOPSIS = "overview";
            final String USER_RATING = "vote_average";
            final String RELEASE_DATE = "release_date";

            JSONObject moviesJsonObject = new JSONObject(jsonStr);
            String title = moviesJsonObject.getString(ORIGINAL_TITLE);
            String synopsis = moviesJsonObject.getString(SYNOPSIS);
            String userRating = moviesJsonObject.getString(USER_RATING);
            String releaseDate = moviesJsonObject.getString(RELEASE_DATE);

            // Get poster path
            final String POSTER_PATH = "poster_path";
            final String COLLECTION = "belongs_to_collection";
            JSONObject posterJsonObject = moviesJsonObject.getJSONObject(COLLECTION);
            String path = posterJsonObject.getString(POSTER_PATH);

            String details = (title + "\n\n" + "Synopsis: " + synopsis + "\n\n" + "Rating: " + userRating + "\n\n"
                    + "Release date: " + releaseDate);

            String[] resultList = new String[2];
            resultList[0] = details;
            resultList[1] = path;

            return resultList;
        }
    }
}
