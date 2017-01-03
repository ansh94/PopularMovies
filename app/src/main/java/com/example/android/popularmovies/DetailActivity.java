package com.example.android.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DetailActivity extends AppCompatActivity {

    private Movie movie;

    private TextView detailTitle;
    private ImageView detailImage;
    private TextView detailReleaseDate;
    private TextView detailUserRating;
    private TextView detailOverview;

    private static final String RATING_BASE = "User Rating: ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        movie = intent.getParcelableExtra("Movie");


        if (movie != null) {
            setup();
            // Load Title text
            detailTitle.setText(movie.getTitle());

            // Load image in Image View
            Picasso.with(this)
                    .load(movie.getPoster())
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
}
