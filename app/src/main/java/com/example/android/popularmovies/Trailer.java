package com.example.android.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ANSHDEEP on 18-03-2017.
 */

public class Trailer implements Parcelable {


    private String tKey;

    private String tName;


    // Normal actions performed by class, since this is still a normal object!
    public Trailer() {

    }

    // Using the `in` variable, we can retrieve the values that
    // we originally wrote into the `Parcel`.  This constructor is usually
    // private so that only the `CREATOR` field can access.
    private Trailer(Parcel in) {
        tKey = in.readString();
        tName = in.readString();

    }

    public String gettKey() {
        return tKey;
    }

    public void settKey(String tKey) {
        this.tKey = tKey;
    }

    public String gettName() {
        return tName;
    }

    public void settName(String tName) {
        this.tName = tName;
    }

    public String getTrailerUrl() {
        return "http://www.youtube.com/watch?v=" + tKey;
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
        parcel.writeString(tKey);
        parcel.writeString(tName);

    }

    // After implementing the `Parcelable` interface, we need to create the
    // `Parcelable.Creator<Movie> CREATOR` constant for our class;
    // Notice how it has our class specified as its type.
    public static final Parcelable.Creator<Trailer> CREATOR
            = new Parcelable.Creator<Trailer>() {

        // This simply calls our new constructor (typically private) and
        // passes along the unmarshalled `Parcel`, and then returns the new object!
        @Override
        public Trailer createFromParcel(Parcel in) {
            return new Trailer(in);
        }

        // We just need to copy this and change the type to match our class.
        @Override
        public Trailer[] newArray(int size) {
            return new Trailer[size];
        }
    };

}
