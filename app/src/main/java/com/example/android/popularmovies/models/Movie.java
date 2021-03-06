package com.example.android.popularmovies.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ANSHDEEP on 27-12-2016.
 */

public class Movie implements Parcelable {

    private String mId;

    private String mTitle;

    private String mPoster;

    private String mBackdrop;

    private String mDescription;

    private double mUserRating;

    private String mReleaseDate;

    private static final String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/";

    private static final String IMAGE_SIZE = "w185/";

    private static final String BACKDROP_SIZE = "w342/";


    // Normal actions performed by class, since this is still a normal object!
    public Movie() {

    }

    // Using the `in` variable, we can retrieve the values that
    // we originally wrote into the `Parcel`.  This constructor is usually
    // private so that only the `CREATOR` field can access.
    private Movie(Parcel in) {
        mId = in.readString();
        mTitle = in.readString();
        mPoster = in.readString();
        mBackdrop = in.readString();
        mDescription = in.readString();
        mReleaseDate = in.readString();
        mUserRating = in.readDouble();
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public double getUserRating() {
        return mUserRating;
    }

    public String getReleaseDate() {
        return mReleaseDate;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setPoster(String poster) {
        mPoster = poster;
    }

    public void setBackdrop(String backdrop) {
        mBackdrop = backdrop;
    }


    public void setDescription(String description) {
        mDescription = description;
    }

    public void setUserRating(double userRating) {
        mUserRating = userRating;
    }

    public void setReleaseDate(String releaseDate) {
        mReleaseDate = releaseDate;
    }


    public String getPoster() {
        return IMAGE_BASE_URL + IMAGE_SIZE + mPoster;
    }

    public String getBackdrop() {
        return IMAGE_BASE_URL + BACKDROP_SIZE + mBackdrop;
    }

    public String getFavoritePoster() {
        return  mPoster;
    }

    public String getFavoriteBackdrop() {
        return  mBackdrop;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    // This is where you write the values you want to save to the `Parcel`.
    // The `Parcel` class has methods defined to help you save all of your values.
    // Note that there are only methods defined for simple values, lists, and other Parcelable objects.
    // You may need to make several classes Parcelable to send the data you want.
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mId);
        parcel.writeString(mTitle);
        parcel.writeString(mPoster);
        parcel.writeString(mBackdrop);
        parcel.writeString(mDescription);
        parcel.writeString(mReleaseDate);
        parcel.writeDouble(mUserRating);
    }

    // After implementing the `Parcelable` interface, we need to create the
    // `Parcelable.Creator<Movie> CREATOR` constant for our class;
    // Notice how it has our class specified as its type.
    public static final Parcelable.Creator<Movie> CREATOR
            = new Parcelable.Creator<Movie>() {

        // This simply calls our new constructor (typically private) and
        // passes along the unmarshalled `Parcel`, and then returns the new object!
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        // We just need to copy this and change the type to match our class.
        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

}
