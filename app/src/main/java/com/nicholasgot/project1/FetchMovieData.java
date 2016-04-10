package com.nicholasgot.project1;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by ngot on 09/04/2016.
 */
public class FetchMovieData {
    private final static String LOG_TAG = FetchMovieData.class.getSimpleName();
    private ArrayList<String> mThumbIds;
    private HashMap<String, String> mIds;
    private ImageAdapter mMoviesAdapter;
    ArrayList<String> mResultList;

    public FetchMovieData(ArrayList<String> mThumbIds, HashMap<String, String> mIds, ImageAdapter imageAdapter) {
        this.mThumbIds = mThumbIds;
        this.mIds = mIds;
        this.mMoviesAdapter = imageAdapter;
        mResultList = new ArrayList<>();
    }

    protected void doInBackground(String... params) {
        String API_KEY = "api_key";
        String sortOrder = params[0];

        OkHttpClient client = new OkHttpClient();
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://api.themoviedb.org").newBuilder();
        urlBuilder.addPathSegment("3");
        urlBuilder.addPathSegment("movie");
        urlBuilder.addPathSegment(sortOrder);
        urlBuilder.addQueryParameter(API_KEY, "f02800d89481918a2f7b70b9375ed8ad");
        String okUrl = urlBuilder.build().toString();

        Log.v(LOG_TAG, "URL: " + okUrl);

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
                    throw new IOException("Unexpected code " + response);
                }

                try {
                    String responseData = response.body().string();
                    getMovieDataFromJson(responseData);
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            onPostExecute(mResultList);
                        }
                    });
                } catch (JSONException je) {
                    Log.v(LOG_TAG, "Json parsing error " + je);
                }
            }
        });
    }

    protected void onPostExecute(ArrayList<String> details) {
        processMovieDetails(details);
    }

    private void processMovieDetails(ArrayList<String> details) {
        ArrayList<String> newThumbIds = new ArrayList<>();
        HashMap<String, String> newIds = new HashMap<>();

        for (int index = 0; index < details.size(); index++) {
            String relativePath = details.get(index).split(":")[1];
            String uri = "http://image.tmdb.org/t/p/" + "w185/" + relativePath;
            newThumbIds.add(uri);

            String id = details.get(index).split(":")[0];
            newIds.put(uri, id); // Assumption: poster uri's are unique among the movies
        }

        // Update adapter with new details
        mThumbIds.clear();
        mIds.clear();
        mThumbIds.addAll(newThumbIds);
        mIds.putAll(newIds);
        mMoviesAdapter.notifyDataSetChanged();
    }

    private void getMovieDataFromJson(String jsonStr) throws JSONException {
        final String RESULTS = "results";
        final String POSTER_PATH = "poster_path";
        final String ID = "id";

        JSONObject moviesJson = new JSONObject(jsonStr);
        JSONArray movieArray = moviesJson.getJSONArray(RESULTS);

        for (int i = 0; i < movieArray.length(); i++) {
            JSONObject movieObject = movieArray.getJSONObject(i);
            String id = movieObject.getString(ID);
            String posterPath = movieObject.getString(POSTER_PATH);

            mResultList.add(id + ":" + posterPath);
        }
    }
}

