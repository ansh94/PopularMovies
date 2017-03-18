package com.example.android.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ANSHDEEP on 18-03-2017.
 */

public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.TrailerViewHolder>{

    private List<Trailer> mTrailerList;
    private LayoutInflater mInflater;
    private Context mContext;

    private final TrailerAdapterOnClickHandler mClickHandler;

    /**
     * The interface that receives onClick messages.
     */
    public interface TrailerAdapterOnClickHandler {
        void onClick(Trailer trailer);
    }


    public TrailerAdapter(Context context, TrailerAdapterOnClickHandler clickHandler)
    {
        this.mContext = context;
        mClickHandler = clickHandler;
        this.mInflater = LayoutInflater.from(context);
        this.mTrailerList = new ArrayList<>();
    }

    @Override
    public TrailerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.trailer_list_content,parent,false);
        TrailerViewHolder viewHolder = new TrailerViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(TrailerViewHolder holder, int position) {

        Trailer trailer = mTrailerList.get(position);

        String thumbnailUrl = "http://img.youtube.com/vi/" + trailer.gettKey() + "/0.jpg";


        // This is how we use Picasso to load images from the internet.
        Picasso.with(mContext)
                .load(thumbnailUrl)
                .placeholder(R.mipmap.ic_launcher)
                .into(holder.mThumbnail);
    }

    @Override
    public int getItemCount() {
        if(mTrailerList!=null){
            return mTrailerList.size();
        }
        else {
            return 0;
        }
    }

    public void setTrailerList(List<Trailer> trailerList){
        this.mTrailerList.clear();
        this.mTrailerList.addAll(trailerList);
        // The adapter needs to know that the data has changed. If we don't call this, app will crash.
        notifyDataSetChanged();
    }



    public class TrailerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public final ImageView mThumbnail;


        public TrailerViewHolder(View itemView) {
            super(itemView);
            mThumbnail = (ImageView) itemView.findViewById(R.id.trailer_thumbnail);
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
            Trailer trailer = mTrailerList.get(clickedPosition);
            mClickHandler.onClick(trailer);
        }
    }
}
