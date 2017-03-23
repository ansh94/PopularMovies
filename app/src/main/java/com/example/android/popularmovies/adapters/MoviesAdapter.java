package com.example.android.popularmovies.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.models.Movie;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ANSHDEEP on 27-12-2016.
 */

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MovieViewHolder> {

    private List<Movie> mMoviesList;
    private LayoutInflater mInflater;
    private Context mContext;

    private final MoviesAdapterOnClickHandler mClickHandler;

    /**
     * The interface that receives onClick messages.
     */
    public interface MoviesAdapterOnClickHandler {
        void onClick(Movie movie);
    }


    public MoviesAdapter(Context context, MoviesAdapterOnClickHandler clickHandler)
    {
        this.mContext = context;
        mClickHandler = clickHandler;
        this.mInflater = LayoutInflater.from(context);
        this.mMoviesList = new ArrayList<>();
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.row_movie,parent,false);
        MovieViewHolder viewHolder = new MovieViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {

        Movie movie = mMoviesList.get(position);

        // This is how we use Picasso to load images from the internet.
        Picasso.with(mContext)
                .load(movie.getPoster())
                .placeholder(R.mipmap.ic_launcher)
                .into(holder.mImageView);
    }

    @Override
    public int getItemCount() {
        if(mMoviesList!=null){
            return mMoviesList.size();
        }
        else {
            return 0;
        }
    }

    public void setMovieList(List<Movie> moviesList){
        this.mMoviesList.clear();
        this.mMoviesList.addAll(moviesList);
        // The adapter needs to know that the data has changed. If we don't call this, app will crash.
        notifyDataSetChanged();
    }




    public class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public final ImageView mImageView;


        public MovieViewHolder(View itemView) {
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
            Movie movie = mMoviesList.get(clickedPosition);
            mClickHandler.onClick(movie);
        }
    }
}
