package com.example.android.popularmovies.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ANSHDEEP on 21-03-2017.
 */

public class Review implements Parcelable {

    private String rAuthor;

    private String rContent;

    private String rUrl;


    // Normal actions performed by class, since this is still a normal object!
    public Review() {

    }

    // Using the `in` variable, we can retrieve the values that
    // we originally wrote into the `Parcel`.  This constructor is usually
    // private so that only the `CREATOR` field can access.
    private Review(Parcel in) {
        rAuthor = in.readString();
        rContent = in.readString();
        rUrl = in.readString();

    }

    public String getAuthor() {
        return rAuthor;
    }

    public void setAuthor(String rAuthor) {
        this.rAuthor = rAuthor;
    }

    public String getContent() {
        return rContent;
    }

    public void setContent(String rContent) {
        this.rContent = rContent;
    }

    public String getUrl() {
        return rUrl;
    }

    public void setUrl(String rUrl) {
        this.rUrl = rUrl;
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
        parcel.writeString(rAuthor);
        parcel.writeString(rContent);
        parcel.writeString(rUrl);

    }

    // After implementing the `Parcelable` interface, we need to create the
    // `Parcelable.Creator<Movie> CREATOR` constant for our class;
    // Notice how it has our class specified as its type.
    public static final Parcelable.Creator<Review> CREATOR
            = new Parcelable.Creator<Review>() {

        // This simply calls our new constructor (typically private) and
        // passes along the unmarshalled `Parcel`, and then returns the new object!
        @Override
        public Review createFromParcel(Parcel in) {
            return new Review(in);
        }

        // We just need to copy this and change the type to match our class.
        @Override
        public Review[] newArray(int size) {
            return new Review[size];
        }
    };

}
