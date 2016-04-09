package com.nicholasgot.project1;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

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
import java.util.HashMap;

/**
 * Created by ngot on 09/04/2016.
 */
public class FetchMovieData extends AsyncTask<String, Void, ArrayList<String>> {
    private final static String LOG_TAG = FetchMovieData.class.getSimpleName();
    private ArrayList<String> mThumbIds;
    private HashMap<String, String> mIds;
    private ImageAdapter mMoviesAdapter;

    public FetchMovieData(ArrayList<String> mThumbIds, HashMap<String, String> mIds, ImageAdapter imageAdapter) {
        this.mThumbIds = mThumbIds;
        this.mIds = mIds;
        this.mMoviesAdapter = imageAdapter;
    }

    @Override
    protected ArrayList<String> doInBackground(String... params) {
        HttpURLConnection httpURLConnection = null;
        BufferedReader bufferedReader = null;
        String jsonStr;

        String API_KEY = "api_key";
        String sortOrder = params[0];

        try {
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("https")
                    .authority("api.themoviedb.org")
                    .appendPath("3")
                    .appendPath("movie")
                    .appendPath(sortOrder)
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
    protected void onPostExecute(ArrayList<String> details) {
        super.onPostExecute(details);
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

    private ArrayList<String> getMovieDataFromJson(String jsonStr) throws JSONException {
        final String RESULTS = "results";
        final String POSTER_PATH = "poster_path";
        final String ID = "id";

        ArrayList<String> resultList = new ArrayList<>();

        JSONObject moviesJson = new JSONObject(jsonStr);
        JSONArray movieArray = moviesJson.getJSONArray(RESULTS);

        for (int i = 0; i < movieArray.length(); i++) {
            JSONObject movieObject = movieArray.getJSONObject(i);
            String id = movieObject.getString(ID);
            String posterPath = movieObject.getString(POSTER_PATH);

            resultList.add(id + ":" + posterPath);
        }
        return resultList;
    }
}

