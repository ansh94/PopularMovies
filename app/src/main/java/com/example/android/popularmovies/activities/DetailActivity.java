package com.example.android.popularmovies.activities;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.adapters.ReviewsAdapter;
import com.example.android.popularmovies.adapters.TrailerAdapter;
import com.example.android.popularmovies.data.MovieContract;
import com.example.android.popularmovies.models.Movie;
import com.example.android.popularmovies.models.Review;
import com.example.android.popularmovies.models.Trailer;
import com.example.android.popularmovies.utils.MovieUtils;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class DetailActivity extends AppCompatActivity implements TrailerAdapter.TrailerAdapterOnClickHandler {

    public static final String LOG_TAG = DetailActivity.class.getSimpleName();


    private RecyclerView tRecyclerView;

    public TrailerAdapter tAdapter;
    private List<Trailer> trailerList;

    private RecyclerView reviewRecyclerView;

    public ReviewsAdapter reviewAdapter;
    private List<Review> reviewList;

    private boolean movieFavourite = false;
    private Movie movie;

    private TextView detailTitle;
    private ImageView detailImage;
    private TextView detailReleaseDate;
    private TextView detailUserRating;
    private TextView detailOverview;

    private TextView mErrorMessageDisplay;

    private Button mButtonMarkAsFavorite;
    private Button mButtonRemoveFromFavorites;



    private static final String RATING_BASE = "User Rating: ";

    private static final String MOVIE_DB_BASE_URL =
            "https://api.themoviedb.org/3/movie/";

    private static final String TRAILER_BASE_URL = "/videos";
    private static final String REVIEW_BASE_URL = "/reviews";

    private static final String API_KEY = "?api_key=81e7fc2c7ca7d07a315d5209367438ce";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        movie = intent.getParcelableExtra("Movie");


        if (movie != null) {
            setup();
            updateFavoriteButtons();

            String trailerUrl = MOVIE_DB_BASE_URL + movie.getId() + TRAILER_BASE_URL + API_KEY;
            Log.d(LOG_TAG, "Final trailer URL = " + trailerUrl);

            String reviewUrl = MOVIE_DB_BASE_URL + movie.getId() + REVIEW_BASE_URL + API_KEY;
            Log.d(LOG_TAG, "Final review URL = " + reviewUrl);


            // Load Title text
            detailTitle.setText(movie.getTitle());

            // Load image in Image View
            Picasso.with(this)
                    .load(movie.getPoster())
                    .placeholder(R.mipmap.ic_launcher)
                    .into(detailImage);

            //Load release date
            String unformattedDate = movie.getReleaseDate();
            String formattedDate = formateDateFromString("yyyy-MM-dd", "MMM dd, yyyy", unformattedDate);
            detailReleaseDate.setText(formattedDate);

            //Load user rating
            String userRating = formatUserRating(movie.getUserRating());
            detailUserRating.setText(RATING_BASE + userRating);

            //Load overview
            detailOverview.setText(movie.getDescription());


            // Get a reference to the ConnectivityManager to check state of network connectivity
            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            // Get details on the currently active default data network
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

            // If there is a network connection, fetch data
            if (networkInfo != null && networkInfo.isConnected()) {

                loadTrailerData(trailerUrl);
                new ReviewAsyncTask().execute(reviewUrl);

            } else {


                showErrorMessage("No Internet Connection");
            }


        } else {
            Toast.makeText(this, "ERROR No data was read", Toast.LENGTH_LONG).show();
        }


    }

    public void updateFavoriteButtons() {
        // Needed to avoid "skip frames".
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... params) {
                return isFavorite();
            }

            @Override
            protected void onPostExecute(Boolean isFavorite) {
                if (isFavorite) {
                    mButtonRemoveFromFavorites.setVisibility(View.VISIBLE);
                    mButtonMarkAsFavorite.setVisibility(View.GONE);

                } else {
                    mButtonMarkAsFavorite.setVisibility(View.VISIBLE);
                    mButtonRemoveFromFavorites.setVisibility(View.GONE);

                }

            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        mButtonMarkAsFavorite.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        markAsFavorite();
                    }
                });


        mButtonRemoveFromFavorites.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        removeFromFavorites();
                    }
                });





    }


    private boolean isFavorite() {
        Cursor movieCursor = getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                new String[]{MovieContract.MovieEntry.COLUMN_MOVIE_ID},
                MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = " + movie.getId(),
                null,
                null);

        if (movieCursor != null && movieCursor.moveToFirst()) {
            movieCursor.close();
            return true;
        } else {
            return false;
        }
    }



    private void loadTrailerData(String requestUrl) {
        showTrailerDataView();

        new TrailerAsyncTask().execute(requestUrl);
    }

    private void showTrailerDataView() {
        /* First, make sure the error is invisible */
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        /* Then, make sure the movie data is visible */
        tRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage(String message) {
        /* First, hide the currently visible data */
        tRecyclerView.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageDisplay.setText(message);
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    private void setup() {

        mButtonMarkAsFavorite = (Button) findViewById(R.id.button_mark_as_favorite);
        mButtonRemoveFromFavorites = (Button) findViewById(R.id.button_remove_from_favorites);






        /* This TextView is used to display errors and will be hidden if there are no errors */
        mErrorMessageDisplay = (TextView) findViewById(R.id.trailer_error_message_display);


        tRecyclerView = (RecyclerView) findViewById(R.id.trailer_recycler_view);


        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        tRecyclerView.setLayoutManager(layoutManager);

        tAdapter = new TrailerAdapter(this, this);

        tRecyclerView.setAdapter(tAdapter);


        reviewRecyclerView = (RecyclerView) findViewById(R.id.review_list);


        LinearLayoutManager reviewLayoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        reviewRecyclerView.setLayoutManager(reviewLayoutManager);

        reviewAdapter = new ReviewsAdapter(this);

        reviewRecyclerView.setAdapter(reviewAdapter);


        detailTitle = (TextView) findViewById(R.id.detail_title);
        detailImage = (ImageView) findViewById(R.id.detail_image);
        detailReleaseDate = (TextView) findViewById(R.id.detail_release_date);
        detailUserRating = (TextView) findViewById(R.id.detail_user_rating);
        detailOverview = (TextView) findViewById(R.id.detail_overview);
    }

    /**
     * Return the formatted magnitude string showing 1 decimal place (i.e. "3.2")
     * from a decimal magnitude value.
     */
    private String formatUserRating(double userRating) {
        DecimalFormat userRatingFormat = new DecimalFormat("0.0");
        return userRatingFormat.format(userRating);
    }

    public static String formateDateFromString(String inputFormat, String outputFormat, String inputDate) {

        Date parsed = null;
        String outputDate = "";

        SimpleDateFormat df_input = new SimpleDateFormat(inputFormat, java.util.Locale.getDefault());
        SimpleDateFormat df_output = new SimpleDateFormat(outputFormat, java.util.Locale.getDefault());

        try {
            parsed = df_input.parse(inputDate);
            outputDate = df_output.format(parsed);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return outputDate;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void markAsFavorite() {

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                if (!isFavorite()) {
                    ContentValues movieValues = new ContentValues();
                    movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID,
                            movie.getId());
                    movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE,
                            movie.getTitle());
                    movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_POSTER_PATH,
                            movie.getPoster());
                    movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_DESCRIPTION,
                            movie.getDescription());
                    movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_RATING,
                            movie.getUserRating());
                    movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_RELEASE_DATE,
                            movie.getReleaseDate());

                    // Insert the content values via a ContentResolver
                   Uri uri =  getContentResolver().insert(
                            MovieContract.MovieEntry.CONTENT_URI,
                            movieValues
                    );

                    // Display the URI that's returned with a Toast
                    if(uri != null) {
                        Log.d(LOG_TAG,"Uri = " + uri.toString());
                    }



                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                updateFavoriteButtons();
                Toast.makeText(getBaseContext(), "Movie added to favourites!", Toast.LENGTH_LONG).show();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    public void removeFromFavorites() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                if (isFavorite()) {
                    int moviesDeleted = getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI,
                            MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = " + movie.getId(), null);

                    Log.d(LOG_TAG,"No of movies deleted = " + moviesDeleted);

                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                updateFavoriteButtons();
                Toast.makeText(getBaseContext(), "Movie removed from favourites!", Toast.LENGTH_LONG).show();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    @Override
    public void onClick(Trailer trailer) {
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + trailer.gettKey()));
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://www.youtube.com/watch?v=" + trailer.gettKey()));
        try {
            startActivity(appIntent);
        } catch (ActivityNotFoundException ex) {
            startActivity(webIntent);
        }
    }

    private class TrailerAsyncTask extends AsyncTask<String, Void, List<Trailer>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected List<Trailer> doInBackground(String... urls) {
            // Don't perform the request if there are no URLs, or the first URL is null.
            if (urls.length < 1 || urls[0] == null) {
                return null;
            }

            trailerList = MovieUtils.fetchTrailerData(urls[0]);
            return trailerList;
        }

        @Override
        protected void onPostExecute(List<Trailer> trailers) {


            if (trailers != null) {
                showTrailerDataView();
                tAdapter.setTrailerList(trailers);
            } else {
                showErrorMessage("Problem getting movies data");
            }

        }
    }


    private class ReviewAsyncTask extends AsyncTask<String, Void, List<Review>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected List<Review> doInBackground(String... urls) {
            // Don't perform the request if there are no URLs, or the first URL is null.
            if (urls.length < 1 || urls[0] == null) {
                return null;
            }

            reviewList = MovieUtils.fetchReviewData(urls[0]);
            return reviewList;
        }

        @Override
        protected void onPostExecute(List<Review> reviews) {


            if (reviews != null) {
//                showTrailerDataView();
                reviewAdapter.setReviewList(reviews);
            }
//            else {
//                showErrorMessage("Problem getting movies data");
//            }

        }
    }


}
