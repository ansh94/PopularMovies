package com.example.android.popularmovies.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

import com.example.android.popularmovies.models.Movie;
import com.example.android.popularmovies.models.Review;
import com.example.android.popularmovies.models.Trailer;

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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ANSHDEEP on 27-12-2016.
 */

public final class MovieUtils {

    public boolean emptyReview = false;

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = MovieUtils.class.getSimpleName();

    /**
     * Create a private constructor because no one should ever create a {@link } object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name MovieUtils (and an object instance of MovieUtils is not needed).
     */
    private MovieUtils() {
    }


    public static List<Movie> fetchMovieData(String requestUrl) {
        //Log.i(LOG_TAG, "TEST: fetchMovieData() called.....");

        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        // Extract relevant fields from the JSON response and create a list of {@link Earthquake}s
        List<Movie> movies = extractFeatureFromJson(jsonResponse);

        // Return the list of {@link Earthquake}s
        return movies;
    }

    public static List<Trailer> fetchTrailerData(String requestUrl) {
        //Log.i(LOG_TAG, "TEST: fetchTrailerData() called.....");

        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        // Extract relevant fields from the JSON response and create a list of {@link Earthquake}s
        List<Trailer> trailers = extractTrailerFeatureFromJson(jsonResponse);

        // Return the list of {@link Earthquake}s
        return trailers;
    }

    public static List<Review> fetchReviewData(String requestUrl) {
        //Log.i(LOG_TAG, "TEST: fetchReviewData() called.....");

        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        // Extract relevant fields from the JSON response and create a list of {@link Earthquake}s
        List<Review> reviews = extractReviewFeatureFromJson(jsonResponse);
        Log.d(LOG_TAG, "Reviews size: " + reviews.size());


        // Return the list of {@link Earthquake}s
        return reviews;
    }


    private static List<Trailer> extractTrailerFeatureFromJson(String jsonResponse) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(jsonResponse)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding movies to it
        List<Trailer> trailers = new ArrayList<>();

        try {
            JSONObject baseJsonResponse = new JSONObject(jsonResponse);
            JSONArray resultsArray = baseJsonResponse.getJSONArray("results");

            // For each earthquake in the earthquakeArray, create an {@link Earthquake} object
            for (int i = 0; i < resultsArray.length(); i++) {

                // Get a single earthquake at position i within the list of earthquakes
                JSONObject currentTrailer = resultsArray.getJSONObject(i);


                // Extract the value for the key called "mag"
                String trailerKey = currentTrailer.getString("key");
                String trailerName = currentTrailer.getString("name");


                //Log.d(LOG_TAG,"Movie id = " + id);
                // Create a new {@link Earthquake} object with the magnitude, location, time,
                // and url from the JSON response.
                Trailer trailer = new Trailer();
                trailer.settKey(trailerKey);
                trailer.settName(trailerName);

                // Add the new {@link Earthquake} to the list of earthquakes.
                trailers.add(trailer);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing the movie JSON results", e);
        }
        return trailers;
    }

    private static List<Review> extractReviewFeatureFromJson(String jsonResponse) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(jsonResponse)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding movies to it
        List<Review> reviews = new ArrayList<>();

        try {
            JSONObject baseJsonResponse = new JSONObject(jsonResponse);
            JSONArray resultsArray = baseJsonResponse.getJSONArray("results");

            // For each earthquake in the earthquakeArray, create an {@link Earthquake} object
            for (int i = 0; i < resultsArray.length(); i++) {

                // Get a single earthquake at position i within the list of earthquakes
                JSONObject currentReview = resultsArray.getJSONObject(i);


                // Extract the value for the key called "mag"
                String reviewAuthor = currentReview.getString("author");
                String reviewContent = currentReview.getString("content");
                String reviewUrl = currentReview.getString("url");


                //Log.d(LOG_TAG,"Movie id = " + id);
                // Create a new {@link Earthquake} object with the magnitude, location, time,
                // and url from the JSON response.
                Review review = new Review();
                review.setAuthor(reviewAuthor);
                review.setContent(reviewContent);
                review.setUrl(reviewUrl);

                // Add the new {@link Earthquake} to the list of earthquakes.
                reviews.add(review);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing the movie JSON results", e);
        }
        return reviews;
    }


    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error with creating URL ", e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(30000 /* milliseconds */);
            urlConnection.setConnectTimeout(35000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the popular movies JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * Return an {@link Movie} object by parsing out information
     * about the first earthquake from the input earthquakeJSON string.
     */
    private static List<Movie> extractFeatureFromJson(String movieJSON) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(movieJSON)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding movies to it
        List<Movie> movies = new ArrayList<>();

        try {
            JSONObject baseJsonResponse = new JSONObject(movieJSON);
            JSONArray resultsArray = baseJsonResponse.getJSONArray("results");

            // For each earthquake in the earthquakeArray, create an {@link Earthquake} object
            for (int i = 0; i < resultsArray.length(); i++) {

                // Get a single earthquake at position i within the list of earthquakes
                JSONObject currentMovie = resultsArray.getJSONObject(i);


                // Extract the value for the key called "mag"
                String posterPath = currentMovie.getString("poster_path");
                String backdropPath = currentMovie.getString("backdrop_path");
                String overview = currentMovie.getString("overview");
                String releaseDate = currentMovie.getString("release_date");
                String id = currentMovie.getString("id");
                String title = currentMovie.getString("original_title");
                double userRating = currentMovie.getDouble("vote_average");


                //Log.d(LOG_TAG,"Movie id = " + id);
                // Create a new {@link Earthquake} object with the magnitude, location, time,
                // and url from the JSON response.
                Movie movie = new Movie();
                movie.setId(id);

                movie.setPoster(posterPath);
                movie.setBackdrop(backdropPath);
                movie.setDescription(overview);
                movie.setReleaseDate(releaseDate);
                movie.setTitle(title);
                movie.setUserRating(userRating);

                // Add the new {@link Earthquake} to the list of earthquakes.
                movies.add(movie);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing the movie JSON results", e);
        }
        return movies;
    }

    public static boolean checkConnection(Context context) {
        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        } else {
            return false;
        }

    }

}
