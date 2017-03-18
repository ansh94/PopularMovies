package com.example.android.popularmovies;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class DetailActivity extends AppCompatActivity implements TrailerAdapter.TrailerAdapterOnClickHandler{

    public static final String LOG_TAG = DetailActivity.class.getSimpleName();


    private RecyclerView tRecyclerView;

    public TrailerAdapter tAdapter;
    private List<Trailer> trailerList;

    private Movie movie;

    private TextView detailTitle;
    private ImageView detailImage;
    private TextView detailReleaseDate;
    private TextView detailUserRating;
    private TextView detailOverview;

    private static final String RATING_BASE = "User Rating: ";

    private static final String MOVIE_DB_BASE_URL =
            "https://api.themoviedb.org/3/movie/";

    private static final String TRAILER_BASE_URL = "/videos";

    private static final String API_KEY = "?api_key=81e7fc2c7ca7d07a315d5209367438ce";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        movie = intent.getParcelableExtra("Movie");





        if (movie != null) {
            setup();

            String requestUrl = MOVIE_DB_BASE_URL + movie.getId()  + TRAILER_BASE_URL + API_KEY;
            Log.d(LOG_TAG, "Final trailer URL = " + requestUrl);
            new TrailerAsyncTask().execute(requestUrl);

            // Load Title text
            detailTitle.setText(movie.getTitle());

            // Load image in Image View
            Picasso.with(this)
                    .load(movie.getPoster())
                    .placeholder(R.mipmap.ic_launcher)
                    .into(detailImage);

            //Load release date
            String unformattedDate = movie.getReleaseDate();
            String formattedDate = formateDateFromString("yyyy-MM-dd","MMM dd, yyyy",unformattedDate);
            detailReleaseDate.setText(formattedDate);

            //Load user rating
            String userRating = formatUserRating(movie.getUserRating());
            detailUserRating.setText(RATING_BASE + userRating);

            //Load overview
            detailOverview.setText(movie.getDescription());
        } else {
            Toast.makeText(this, "ERROR No data was read", Toast.LENGTH_LONG).show();
        }


    }

    private void setup() {

        tRecyclerView = (RecyclerView) findViewById(R.id.trailer_recycler_view);


        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        tRecyclerView.setLayoutManager(layoutManager);

        tAdapter = new TrailerAdapter(this,this);

        tRecyclerView.setAdapter(tAdapter);



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

    public static String formateDateFromString(String inputFormat, String outputFormat, String inputDate){

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
        if(id == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
//            mLoadingIndicator.setVisibility(View.VISIBLE);
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

//            mLoadingIndicator.setVisibility(View.INVISIBLE);


            if (trailers != null) {
                //showMovieDataView();
                tAdapter.setTrailerList(trailers);
            }
//            else {
//                showErrorMessage("Problem getting movies data");
//            }

        }
    }




}
