package com.example.android.popularmovies.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.models.Review;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ANSHDEEP on 21-03-2017.
 */

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder> {

    private List<Review> mReviewList;
    private LayoutInflater mInflater;
    private Context mContext;


    public ReviewsAdapter(Context context) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.mReviewList = new ArrayList<>();
    }

    @Override
    public ReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.review_list_content, parent, false);
        ReviewViewHolder viewHolder = new ReviewViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ReviewViewHolder holder, int position) {

        Review review = mReviewList.get(position);

        final TextView rAuthor = holder.rAuthor;
        final TextView rContent = holder.rContent;
        final TextView expandMore = holder.expandMore;
        final TextView expandLess = holder.expandLess;

        rAuthor.setText(review.getAuthor());
        rContent.setText(review.getContent());


        expandMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rContent.setMaxLines(100);
                expandMore.setVisibility(View.GONE);
                expandLess.setVisibility(View.VISIBLE);
            }
        });

        expandLess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rContent.setMaxLines(3);
                expandLess.setVisibility(View.GONE);
                expandMore.setVisibility(View.VISIBLE);
            }
        });


    }


    @Override
    public int getItemCount() {
        if (mReviewList != null) {
            return mReviewList.size();
        } else {
            return 0;
        }
    }

    public void setReviewList(List<Review> reviewList) {
        this.mReviewList.clear();
        this.mReviewList.addAll(reviewList);
        // The adapter needs to know that the data has changed. If we don't call this, app will crash.
        notifyDataSetChanged();
    }


    public class ReviewViewHolder extends RecyclerView.ViewHolder {

        public final TextView rAuthor;
        public final TextView rContent;
        public final TextView expandMore;
        public final TextView expandLess;


        public ReviewViewHolder(View itemView) {
            super(itemView);
            rAuthor = (TextView) itemView.findViewById(R.id.review_author);
            rContent = (TextView) itemView.findViewById(R.id.review_content);
            expandMore = (TextView) itemView.findViewById(R.id.review_expand_more);
            expandLess = (TextView) itemView.findViewById(R.id.review_expand_less);
        }


    }
}
