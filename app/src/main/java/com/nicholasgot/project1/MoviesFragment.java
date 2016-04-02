package com.nicholasgot.project1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

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
 * A placeholder fragment containing a simple view.
 */
public class MoviesFragment extends Fragment {
    private ImageAdapter mMoviesAdapter;
    private String LOG_TAG = MoviesFragment.class.getSimpleName();
    private ArrayList<String> mThumbIds = new ArrayList<>();
    private HashMap<String, String> mIds = new HashMap<>();

    public MoviesFragment() {
    }

    @Override
    public void onStart() {
        super.onStart();
        collectMovieData();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void collectMovieData() {
        final String SORT_ORDER = "sort_order";
        final String SORT_BY_RATING = "TopRated";

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String prefSortOrder = sharedPref.getString(SORT_ORDER, "");
        if (prefSortOrder.equals(SORT_BY_RATING)) {
            prefSortOrder = "top_rated";
        }
        else {
            prefSortOrder = "popular";
        }

        FetchMovieData fetchTask = new FetchMovieData();
        fetchTask.execute(prefSortOrder);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_main, container, false);

        GridView gridView = (GridView) rootView.findViewById(R.id.gridview_movies);
        mMoviesAdapter = new ImageAdapter(getActivity(), mThumbIds);
        gridView.setAdapter(mMoviesAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String moviePosterPath = (String) mMoviesAdapter.getItem(position);

                Intent intent = new Intent(getActivity(), MovieDetailsActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, mIds.get(moviePosterPath));
                startActivity(intent);
            }
        });

        return rootView;
    }

    public class FetchMovieData extends AsyncTask<String, Void, ArrayList<String>> {

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
                        .appendQueryParameter(API_KEY, "f02800d89481918a2f7b70b9375ed8ad");

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
            for (int index = 0; index < details.size(); index++) {
                String relativePath = details.get(index).split(":")[1];
                String uri = "http://image.tmdb.org/t/p/" + "w185/" + relativePath;
                mThumbIds.add(uri);

                String id = details.get(index).split(":")[0];
                mIds.put(uri, id); // Assumption: poster uri's are unique among the movies
            }
            mMoviesAdapter.notifyDataSetChanged();
        }

        private ArrayList<String> getMovieDataFromJson(String jsonStr) throws JSONException {
            // TODO:
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
}
