package com.nicholasgot.project1;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.squareup.picasso.Picasso;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by ngot on 09/04/2016.
 */
public class FetchMovieDetails {
    private static final String LOG_TAG = FetchMovieDetails.class.getSimpleName();
    private static final int RESULT_LENGTH = 2;
    private Context mContext;
    private TextView mTextView;
    private Activity mActivity;
    String[] mResultList;

    public FetchMovieDetails(Context context, TextView textView, Activity activity) {
        mContext = context;
        mTextView = textView;
        mActivity = activity;
        mResultList = new String[RESULT_LENGTH];
    }

    protected void doInBackground(String... params) {
        String API_KEY = "api_key";
        String id = params[0];

        OkHttpClient client = new OkHttpClient();
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://api.themoviedb.org").newBuilder();
        urlBuilder.addPathSegment("3");
        urlBuilder.addPathSegment("movie");
        urlBuilder.addPathSegment(id);
        urlBuilder.addQueryParameter(API_KEY, "f02800d89481918a2f7b70b9375ed8ad");
        String okUrl = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(okUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code: " + response);
                }

                try {
                    String responseData = response.body().string();
                    getMovieDataFromJson(responseData);

                    //Post movie details on the main thread
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            onPostExecute(mResultList);
                        }
                    });

                } catch (JSONException je) {
                    Log.e(LOG_TAG, "Json parsing error: " + je);
                }
            }
        });
    }

    protected void onPostExecute(String[] movieDetails) {
        if (movieDetails == null || movieDetails.length == 0) {
            CharSequence text = "Error querying movie details";
            Toast toast = Toast.makeText(mContext, text, Toast.LENGTH_SHORT);
            toast.show();
        }
        else {

            String imageUri = "http://image.tmdb.org/t/p/" + "w185/" + movieDetails[1];
            ImageView imageView = (ImageView) mActivity.findViewById(R.id.image_view);
            displayMoviePoster(imageUri, imageView);

            mTextView.setText(movieDetails[0]);
        }
    }

    protected void displayMoviePoster(String uri, ImageView imageView) {
        Picasso.with(mContext)
                .load(uri)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.error_drawable)
                .into(imageView);
    }

    private void getMovieDataFromJson(String jsonStr) throws JSONException {
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

        mResultList[0] = details;
        mResultList[1] = path;
    }
}
