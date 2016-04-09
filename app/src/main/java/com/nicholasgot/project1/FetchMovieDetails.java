package com.nicholasgot.project1;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by ngot on 09/04/2016.
 */
public class FetchMovieDetails extends AsyncTask<String, Void, String[]> {
    private static final String LOG_TAG = FetchMovieDetails.class.getSimpleName();
    private Context mContext;
    private TextView mTextView;
    private Activity mActivity;

    public FetchMovieDetails(Context context, TextView textView, Activity activity) {
        mContext = context;
        mTextView = textView;
        mActivity = activity;
    }

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
                    .appendQueryParameter(API_KEY, "API KEY");

            String myUrl = builder.build().toString();

            URL url = new URL(myUrl);

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
        ImageView imageView = (ImageView) mActivity.findViewById(R.id.image_view);
        displayMoviePoster(imageUri, imageView);

        mTextView.setText(movieDetails[0]);
    }

    protected void displayMoviePoster(String uri, ImageView imageView) {
        Picasso.with(mContext).load(uri).into(imageView);
    }

    private String[] getMovieDataFromJson(String jsonStr) throws JSONException {
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
        JSONObject posterJsonObject;
        String path;
        try {
            posterJsonObject = moviesJsonObject.getJSONObject(COLLECTION);
            path = posterJsonObject.getString(POSTER_PATH);
        } catch (JSONException je) {
            path = moviesJsonObject.getString(POSTER_PATH);
        }

        String details = (title + "\n\n" + "Synopsis: " + synopsis + "\n\n" + "Rating: " + userRating + "\n\n"
                + "Release date: " + releaseDate);

        String[] resultList = new String[2];
        resultList[0] = details;
        resultList[1] = path;

        return resultList;
    }
}
