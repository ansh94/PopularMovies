package com.example.android.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity implements MoviesAdapter.MoviesAdapterOnClickHandler {

    private RecyclerView mRecyclerView;

    public MoviesAdapter mAdapter;

    /** Tag for the log messages */
    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final String MOVIE_DB_BASE_URL =
            "https://api.themoviedb.org/3/movie/";

    private static final String API_KEY = "?api_key=81e7fc2c7ca7d07a315d5209367438ce";

    private String sortBy;
    private List<Movie> moviesList;

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

        mRecyclerView.setLayoutManager(new GridLayoutManager(this,2));

        mAdapter = new MoviesAdapter(this,this);

        mRecyclerView.setAdapter(mAdapter);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        String orderBy = sharedPrefs.getString(
                getString(R.string.settings_sort_by_key),
                getString(R.string.settings_sort_by_default)
        );

        final String Url = MOVIE_DB_BASE_URL+orderBy+API_KEY;
        Log.d(LOG_TAG,"Final URL = "+ Url);

        loadMovieData(Url);

//        MovieAsyncTask asyncTask = new MovieAsyncTask();
//        asyncTask.execute(MOVIE_DB_URL);

//        List<Movie> movies = new ArrayList<>();

//        for (int i = 0; i < 25; i++) {
//            movies.add(new Movie());
//        }
//        mAdapter.setMovieList(movies);


    }

    private void loadMovieData(String requestUrl) {
        showMovieDataView();

        new MovieAsyncTask().execute(requestUrl);
    }


    private void showMovieDataView() {
        /* First, make sure the error is invisible */
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        /* Then, make sure the weather data is visible */
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage() {
        /* First, hide the currently visible data */
        mRecyclerView.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(Movie movie) {
//        String toastMessage = "Movie name: " + movie.getTitle();
//        Toast toast = Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT);
//        toast.show();

        Intent intent = new Intent(MainActivity.this,DetailActivity.class);
        intent.putExtra("Movie",movie);
        startActivity(intent);
    }

    private class MovieAsyncTask extends AsyncTask<String, Void, List<Movie>>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<Movie> doInBackground(String... urls) {
            // Don't perform the request if there are no URLs, or the first URL is null.
            if(urls.length < 1 || urls[0] == null){
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
                showErrorMessage();
            }

            // Update the information displayed to the user.
            updateUi(movies);
        }
    }

    private void updateUi(List<Movie> movies){
        mAdapter.setMovieList(movies);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_settings){
            Intent settingsIntent = new Intent(this,SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
