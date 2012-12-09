package edu.cmu.glimpse.entry;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import edu.cmu.glimpse.utils.Utils;

public class EntryPlace implements Parcelable {

    private String mName;
    private String mGooglePlaceReference;
    private Location mLocation;

    public EntryPlace(String name, String googlePlaceReference) {
        this(name, googlePlaceReference, null);
    }

    public EntryPlace(String name, String googlePlaceReference, Location location) {
        mName = name;
        mGooglePlaceReference = googlePlaceReference;
        mLocation = location;
    }

    public EntryPlace(Parcel in) {
        readFromParcel(in);
    }

    public static final Parcelable.Creator<EntryPlace> CREATOR = new Parcelable.Creator<EntryPlace>() {
        public EntryPlace createFromParcel(Parcel in) {
            return new EntryPlace(in);
        }

        public EntryPlace[] newArray(int size) {
            return new EntryPlace[size];
        }
    };

    public String getName() {
        return mName;
    }

    public String getGooglePlaceReference() {
        return mGooglePlaceReference;
    }

    public Location getLocation() {
        return mLocation;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeString(mGooglePlaceReference);
        dest.writeParcelable(mLocation, flags);
    }

    private void readFromParcel(Parcel in) {
        mName = in.readString();
        mGooglePlaceReference = in.readString();
        mLocation = in.readParcelable(Location.class.getClassLoader());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof EntryPlace)) {
            return false;
        }

        EntryPlace place = (EntryPlace) o;

        return Utils.stringEquals(mName, place.getName())
                && Utils.stringEquals(mGooglePlaceReference, place.getGooglePlaceReference());
    }

    @Override
    public String toString() {
        return mName;
    }
}
