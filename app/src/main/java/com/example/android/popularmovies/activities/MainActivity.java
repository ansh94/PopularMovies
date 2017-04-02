package com.example.android.popularmovies.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.popularmovies.BuildConfig;
import com.example.android.popularmovies.R;
import com.example.android.popularmovies.adapters.CustomCursorAdapter;
import com.example.android.popularmovies.adapters.MoviesAdapter;
import com.example.android.popularmovies.data.MovieContract;
import com.example.android.popularmovies.models.Movie;
import com.example.android.popularmovies.utils.MovieUtils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MoviesAdapter.MoviesAdapterOnClickHandler, CustomCursorAdapter.CustomCursorAdapterOnClickHandler,
        LoaderManager.LoaderCallbacks<Cursor> {

    private RecyclerView mRecyclerView;

    public MoviesAdapter mAdapter;

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final int MOVIE_LOADER_ID = 0;


    // Member variables for the adapter and RecyclerView
    private CustomCursorAdapter cAdapter;

    private static final String EXTRA_MOVIES = "EXTRA_MOVIES";
    private static final String EXTRA_SORT_BY = "EXTRA_SORT_BY";


    private static final String MOVIE_DB_BASE_URL =
            "https://api.themoviedb.org/3/movie/";

    private static final String API_BASE = "?api_key=";
    private static final String API_KEY = BuildConfig.THE_MOVIE_DATABASE_API_KEY;
    private static final String API = API_BASE + API_KEY;

    private String sortBy;
    private List<Movie> moviesList;


    private String finalUrl;

    private TextView mErrorMessageDisplay;

    private ProgressBar mLoadingIndicator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        /* This TextView is used to display errors and will be hidden if there are no errors */
        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);

        /*
         * The ProgressBar that will indicate to the user that we are loading data. It will be
         * hidden when no data is loading.
         *
         * Please note: This so called "ProgressBar" isn't a bar by default. It is more of a
         * circle. We didn't make the rules (or the names of Views), we just follow them.
         */
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        mRecyclerView = (RecyclerView) findViewById(R.id.movies_recycler_view);

        cAdapter = new CustomCursorAdapter(this, this);

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        mAdapter = new MoviesAdapter(this, this);


        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        sortBy = sharedPrefs.getString(
                getString(R.string.settings_sort_by_key),
                getString(R.string.settings_sort_by_default)
        );

        if (sortBy.equals("favorites")) {
            mRecyclerView.setAdapter(cAdapter);
        } else {
            mRecyclerView.setAdapter(mAdapter);

        }


        if (savedInstanceState != null) {
            sortBy = savedInstanceState.getString(EXTRA_SORT_BY);

            if (sortBy.equals("favorites")) {
                mRecyclerView.setAdapter(cAdapter);
            } else {
                mRecyclerView.setAdapter(mAdapter);

            }

            if (savedInstanceState.containsKey(EXTRA_MOVIES)) {
                ArrayList<Movie> movies = savedInstanceState.getParcelableArrayList(EXTRA_MOVIES);
                mAdapter.setMovieList(movies);
            }
        } else {

            if (!sortBy.equals("favorites")) {
                finalUrl = MOVIE_DB_BASE_URL + sortBy + API;


                // If there is a network connection, fetch data
                if (MovieUtils.checkConnection(this)) {

                    loadMovieData(finalUrl);

                } else {
                    // Otherwise, display error
                    // First, hide loading indicator so error message will be visible
                    mLoadingIndicator.setVisibility(View.GONE);

                    showErrorMessage(getString(R.string.no_internet_main));
                }
            }


        }


        /*
         Ensure a loader is initialized and active. If the loader doesn't already exist, one is
         created, otherwise the last created loader is re-used.
         */

        getSupportLoaderManager().initLoader(MOVIE_LOADER_ID, null, this);


    }


    @Override
    protected void onResume() {
        super.onResume();


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String currentValue = preferences.getString(
                getString(R.string.settings_sort_by_key),
                getString(R.string.settings_sort_by_default)
        );

        getSupportLoaderManager().restartLoader(MOVIE_LOADER_ID, null, this);


        if (!currentValue.equals("favorites")) {

            // If there is a network connection, fetch data
            if (MovieUtils.checkConnection(this)) {

                mRecyclerView.setAdapter(mAdapter);
                finalUrl = MOVIE_DB_BASE_URL + sortBy + API;
                loadMovieData(finalUrl);

            } else {
                // Otherwise, display error
                // First, hide loading indicator so error message will be visible
                mLoadingIndicator.setVisibility(View.GONE);

                showErrorMessage(getString(R.string.no_internet_main));
            }
        } else if (currentValue.equals("favorites")) {

            getSupportLoaderManager().restartLoader(MOVIE_LOADER_ID, null, this);


            mRecyclerView.setAdapter(cAdapter);
        }

        if (!currentValue.equals(sortBy)) {
            sortBy = currentValue;
            if (sortBy.equals("favorites")) {

                showMovieDataView();
                getSupportLoaderManager().restartLoader(MOVIE_LOADER_ID, null, this);
                mRecyclerView.setAdapter(cAdapter);

            } else if (!sortBy.equals("favorites")) {


                // If there is a network connection, fetch data
                if (MovieUtils.checkConnection(this)) {

                    mRecyclerView.setAdapter(mAdapter);
                    finalUrl = MOVIE_DB_BASE_URL + sortBy + API;
                    loadMovieData(finalUrl);

                } else {
                    // Otherwise, display error
                    // First, hide loading indicator so error message will be visible
                    mLoadingIndicator.setVisibility(View.GONE);

                    showErrorMessage(getString(R.string.no_internet_main));
                }


            }

        }


    }


    private void loadMovieData(String requestUrl) {
        showMovieDataView();

        new MovieAsyncTask().execute(requestUrl);
    }


    private void showMovieDataView() {
        /* First, make sure the error is invisible */
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        /* Then, make sure the movie data is visible */
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage(String message) {
        /* First, hide the currently visible data */
        mRecyclerView.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageDisplay.setText(message);
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }


    @Override
    public void onClick(Movie movie) {
        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
        intent.putExtra("Movie", movie);
        startActivity(intent);
    }

    /**
     * Instantiates and returns a new AsyncTaskLoader with the given ID.
     * This loader will return task data as a Cursor or null if an error occurs.
     * <p>
     * Implements the required callbacks to take care of loading data at all stages of loading.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<Cursor>(this) {

            // Initialize a Cursor, this will hold all the task data
            Cursor mTaskData = null;

            // onStartLoading() is called when a loader first starts loading data
            @Override
            protected void onStartLoading() {
                if (mTaskData != null) {
                    // Delivers any previously loaded data immediately
                    deliverResult(mTaskData);
                } else {
                    // Force a new load
                    forceLoad();
                }
            }

            // loadInBackground() performs asynchronous loading of data
            @Override
            public Cursor loadInBackground() {
                // Will implement to load data

                // Query and load all task data in the background;

                try {
                    return getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI,
                            null,
                            null,
                            null,
                            null);

                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            // deliverResult sends the result of the load, a Cursor, to the registered listener
            public void deliverResult(Cursor data) {
                mTaskData = data;
                super.deliverResult(data);
            }
        };
    }


    /**
     * Called when a previously created loader has finished its load.
     *
     * @param loader The Loader that has finished.
     * @param data   The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update the data that the adapter uses to create ViewHolders
        cAdapter.swapCursor(data);
        cAdapter.notifyDataSetChanged();
    }

    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.
     * onLoaderReset removes any references this activity had to the loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cAdapter.swapCursor(null);
    }

    @Override
    public void onFavoriteMovieClick(Movie movie) {
        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
        intent.putExtra("Movie", movie);
        startActivity(intent);

    }

    private class MovieAsyncTask extends AsyncTask<String, Void, List<Movie>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<Movie> doInBackground(String... urls) {
            // Don't perform the request if there are no URLs, or the first URL is null.
            if (urls.length < 1 || urls[0] == null) {
                return null;
            }

            moviesList = MovieUtils.fetchMovieData(urls[0]);
            return moviesList;
        }

        @Override
        protected void onPostExecute(List<Movie> movies) {

            mLoadingIndicator.setVisibility(View.INVISIBLE);


            if (movies != null) {
                showMovieDataView();
                updateUi(movies);
            } else {
                showErrorMessage(getString(R.string.problem_movie_data));
            }

        }
    }

    private void updateUi(List<Movie> movies) {
        mAdapter.setMovieList(movies);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        ArrayList<Movie> movies = mAdapter.getMovies();
        if (movies != null && !movies.isEmpty()) {
            outState.putParcelableArrayList(EXTRA_MOVIES, movies);
        }
        outState.putString(EXTRA_SORT_BY, sortBy);

    }
}
