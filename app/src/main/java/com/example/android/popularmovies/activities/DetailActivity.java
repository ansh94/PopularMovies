package com.example.android.popularmovies.activities;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popularmovies.BuildConfig;
import com.example.android.popularmovies.R;
import com.example.android.popularmovies.adapters.ReviewsAdapter;
import com.example.android.popularmovies.adapters.TrailerAdapter;
import com.example.android.popularmovies.data.MovieContract;
import com.example.android.popularmovies.models.Movie;
import com.example.android.popularmovies.models.Review;
import com.example.android.popularmovies.models.Trailer;
import com.example.android.popularmovies.utils.MovieUtils;
import com.like.LikeButton;
import com.like.OnLikeListener;
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

    private View parentView;

    private Movie movie;
    private String posterPath;
    private String backdropPath;

    private CollapsingToolbarLayout collapsingToolbar;

    private ImageView movieBackdrop;


    private ImageView detailImage;
    private TextView detailReleaseDate;
    private TextView detailUserRating;
    private TextView detailOverview;

    private TextView mErrorMessageDisplay;
    private TextView rErrorMessageDisplay;


    private LikeButton likeButton;


    private static final String RATING_BASE = "User Rating: ";

    private static final String MOVIE_DB_BASE_URL =
            "https://api.themoviedb.org/3/movie/";

    private static final String TRAILER_BASE_URL = "/videos";
    private static final String REVIEW_BASE_URL = "/reviews";

    private static final String API_BASE = "?api_key=";
    private static final String API_KEY = BuildConfig.THE_MOVIE_DATABASE_API_KEY;
    private static final String API = API_BASE + API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);


        Intent intent = getIntent();

        movie = intent.getParcelableExtra("Movie");


        if (movie != null) {
            setup();
            updateFavoriteButtons();

            String trailerUrl = MOVIE_DB_BASE_URL + movie.getId() + TRAILER_BASE_URL + API;
            String reviewUrl = MOVIE_DB_BASE_URL + movie.getId() + REVIEW_BASE_URL + API;


            // Set title of Detail page
            collapsingToolbar.setTitle(movie.getTitle());

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            String sortBy = preferences.getString(
                    getString(R.string.settings_sort_by_key),
                    getString(R.string.settings_sort_by_default)
            );

            if (sortBy.equals("favorites")) {
                posterPath = movie.getFavoritePoster();
                backdropPath = movie.getFavoriteBackdrop();
            } else {
                posterPath = movie.getPoster();
                backdropPath = movie.getBackdrop();
            }


            Log.d(DetailActivity.class.getSimpleName(), "backdrop Path: " + backdropPath);
            // Load image in Image View
            Picasso.with(this)
                    .load(backdropPath)
                    .placeholder(R.mipmap.ic_launcher)
                    .into(movieBackdrop);


            // Load image in Image View
            Picasso.with(this)
                    .load(posterPath)
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


            // If there is a network connection, fetch data
            if (MovieUtils.checkConnection(this)) {

                loadTrailerData(trailerUrl);

                loadReviewData(reviewUrl);

            } else {
                showErrorMessage(getString(R.string.no_internet));
            }


        } else {
            Toast.makeText(this, R.string.error_no_data_detail, Toast.LENGTH_LONG).show();
        }


    }

    public void updateFavoriteButtons() {

        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... params) {
                return isFavorite();
            }

            @Override
            protected void onPostExecute(Boolean isFavorite) {
                if (isFavorite) {
                    likeButton.setLiked(true);
                } else {
                    likeButton.setLiked(false);
                }

            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


        likeButton.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {
                markAsFavorite();
            }

            @Override
            public void unLiked(LikeButton likeButton) {
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

    private void loadReviewData(String requestUrl) {
        showReviewDataView();

        new ReviewAsyncTask().execute(requestUrl);
    }

    private void showReviewDataView() {
        /* First, make sure the error is invisible */
        rErrorMessageDisplay.setVisibility(View.INVISIBLE);
        /* Then, make sure the movie data is visible */
        reviewRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage(String message) {
        /* First, hide the currently visible data */
        tRecyclerView.setVisibility(View.INVISIBLE);
        reviewRecyclerView.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageDisplay.setText(message);
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
        rErrorMessageDisplay.setText(message);
        rErrorMessageDisplay.setVisibility(View.VISIBLE);
    }


    private void setup() {

        //set the toolbar
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set Collapsing Toolbar layout to the screen
        collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);


        movieBackdrop = (ImageView) findViewById(R.id.movie_backdrop);


        likeButton = (LikeButton) findViewById(R.id.star_button);


        parentView = findViewById(R.id.root_view);


        /* This TextView is used to display errors and will be hidden if there are no errors */
        mErrorMessageDisplay = (TextView) findViewById(R.id.trailer_error_message_display);
        rErrorMessageDisplay = (TextView) findViewById(R.id.review_error_message_display);


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

        //for smooth scrolling
        reviewRecyclerView.setNestedScrollingEnabled(false);

        reviewAdapter = new ReviewsAdapter(this);

        reviewRecyclerView.setAdapter(reviewAdapter);


//        detailTitle = (TextView) findViewById(R.id.detail_title);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_share) {
            shareTrailerUrl();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void shareTrailerUrl() {
        String mimeType = "text/plain";
        String title = "Share Movie Trailer";

        if (trailerList != null) {
            if (trailerList.size() == 0) {
                Toast.makeText(this, R.string.no_trailer_share, Toast.LENGTH_SHORT).show();
            } else if (trailerList.size() > 0) {
                //gets the first trailer video
                Trailer trailer = trailerList.get(0);

                //gets the trailer name
                String trailerName = trailer.gettName();
                //gets the first trailer url
                String trailerUrl = trailer.getTrailerUrl();

                ShareCompat.IntentBuilder.from(this)
                        .setChooserTitle(title)
                        .setType(mimeType)
                        .setText(trailerName + "\n" + trailerUrl)
                        .startChooser();
            }
        } else {
            Toast.makeText(this, R.string.no_internet_share, Toast.LENGTH_SHORT).show();
        }
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
                    movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_BACKDROP_PATH,
                            movie.getBackdrop());
                    movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_DESCRIPTION,
                            movie.getDescription());
                    movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_RATING,
                            movie.getUserRating());
                    movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_RELEASE_DATE,
                            movie.getReleaseDate());

                    // Insert the content values via a ContentResolver
                    Uri uri = getContentResolver().insert(
                            MovieContract.MovieEntry.CONTENT_URI,
                            movieValues
                    );


                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                updateFavoriteButtons();
                Snackbar.make(parentView, R.string.movie_added_favorites, Snackbar.LENGTH_LONG).show();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    public void removeFromFavorites() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                if (isFavorite()) {
                    getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI,
                            MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = " + movie.getId(), null);

                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                updateFavoriteButtons();
                Snackbar.make(parentView, R.string.movie_removed_favorites, Snackbar.LENGTH_LONG).show();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    @Override
    public void onClick(Trailer trailer) {
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.youtube_player_base) + trailer.gettKey()));
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(getString(R.string.youtube_base) + trailer.gettKey()));
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


            if (trailers.size() == 0) {
                mErrorMessageDisplay.setText(R.string.no_trailer_error_msg);
                mErrorMessageDisplay.setVisibility(View.VISIBLE);
            }
            if (trailers.size() > 0) {
                showTrailerDataView();
                tAdapter.setTrailerList(trailers);
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

            if (reviews.size() == 0) {
                rErrorMessageDisplay.setText(R.string.no_review_error_msg);
                rErrorMessageDisplay.setVisibility(View.VISIBLE);
            }
            if (reviews.size() > 0) {
                showReviewDataView();
                reviewAdapter.setReviewList(reviews);
            }

        }
    }


}
