package com.example.android.popularmovies.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.data.MovieContract;
import com.example.android.popularmovies.models.Movie;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by ANSHDEEP on 23-03-2017.
 */

public class CustomCursorAdapter extends RecyclerView.Adapter<CustomCursorAdapter.FavoriteMovieViewHolder> {


    // Class variables for the Cursor that holds task data and the Context
    private Cursor mCursor;
    private Context mContext;
    private ArrayList<Movie> fMovies;

    private final CustomCursorAdapterOnClickHandler mClickHandler;

    /**
     * The interface that receives onClick messages.
     */
    public interface CustomCursorAdapterOnClickHandler {
        void onFavoriteMovieClick(Movie movie);
    }


    /**
     * Constructor for the CustomCursorAdapter that initializes the Context.
     *
     * @param mContext the current Context
     */
    public CustomCursorAdapter(Context mContext, CustomCursorAdapterOnClickHandler clickHandler) {
        this.mContext = mContext;
        mClickHandler = clickHandler;
        fMovies = new ArrayList<>();
    }


    /**
     * Called when ViewHolders are created to fill a RecyclerView.
     *
     * @return A new TaskViewHolder that holds the view for each task
     */
    @Override
    public FavoriteMovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        // Inflate the task_layout to a view
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.row_movie, parent, false);

        return new FavoriteMovieViewHolder(view);
    }


    /**
     * Called by the RecyclerView to display data at a specified position in the Cursor.
     *
     * @param holder   The ViewHolder to bind Cursor data to
     * @param position The position of the data in the Cursor
     */
    @Override
    public void onBindViewHolder(FavoriteMovieViewHolder holder, int position) {

        if (mCursor != null && getItemCount() > 0) {


            int moviePosterIndex = mCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_POSTER_PATH);

            mCursor.moveToPosition(position); // get to the right location in the cursor

            String posterPath = mCursor.getString(moviePosterIndex);

            // This is how we use Picasso to load images from the internet.
            Picasso.with(mContext)
                    .load(posterPath)
                    .placeholder(R.mipmap.ic_launcher)
                    .into(holder.mImageView);

        }
    }


    /**
     * Returns the number of items to display.
     */
    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();

    }


    /**
     * When data changes and a re-query occurs, this function swaps the old Cursor
     * with a newly updated Cursor (Cursor c) that is passed in.
     */
    public Cursor swapCursor(Cursor c) {
        // check if this cursor is the same as the previous cursor (mCursor)
        if (mCursor == c) {
            return null; // bc nothing has changed
        }


        Cursor temp = mCursor;
        this.mCursor = c; // new cursor value assigned
        this.fMovies.clear();

        //check if this is a valid cursor, then update the cursor
        if (c != null && c.moveToFirst()) {
            int movieIdIndex = c.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_ID);
            int movieTitleIndex = c.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE);
            int moviePosterIndex = c.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_POSTER_PATH);
            int movieBackdropIndex = c.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_BACKDROP_PATH);
            int movieDescriptionIndex = c.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_DESCRIPTION);
            int movieRatingIndex = c.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_RATING);
            int movieDateIndex = c.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_RELEASE_DATE);

            do {

                // Determine the values of the wanted data
                String movieId = c.getString(movieIdIndex);
                String movieTitle = c.getString(movieTitleIndex);
                String posterPath = c.getString(moviePosterIndex);
                String backdropPath = c.getString(movieBackdropIndex);
                String movieDescription = c.getString(movieDescriptionIndex);
                String movieRating = c.getString(movieRatingIndex);
                String movieDate = c.getString(movieDateIndex);

                Movie movie = new Movie();
                movie.setId(movieId);
                movie.setTitle(movieTitle);
                movie.setPoster(posterPath);
                movie.setBackdrop(backdropPath);
                movie.setDescription(movieDescription);
                movie.setUserRating(Double.parseDouble(movieRating));
                movie.setReleaseDate(movieDate);
                fMovies.add(movie);
            }
            while (c.moveToNext());

            this.notifyDataSetChanged();

        }
        return temp;
    }


    // Inner class for creating ViewHolders
    class FavoriteMovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final ImageView mImageView;

        /**
         * Constructor for the TaskViewHolders.
         *
         * @param itemView The view inflated in onCreateViewHolder
         */
        public FavoriteMovieViewHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.movie_image_view);
            itemView.setOnClickListener(this);
        }

        /**
         * This gets called by the child views during a click.
         *
         * @param view The View that was clicked
         */
        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();

            Movie movie = fMovies.get(clickedPosition);
            mClickHandler.onFavoriteMovieClick(movie);
        }
    }
}
