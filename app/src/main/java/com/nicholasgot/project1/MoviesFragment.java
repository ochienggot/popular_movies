package com.nicholasgot.project1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
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

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String prefSortOrder = sharedPref.getString(SORT_ORDER, "");

        FetchMovieData fetchTask = new FetchMovieData(mThumbIds, mIds, mMoviesAdapter);
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
}
