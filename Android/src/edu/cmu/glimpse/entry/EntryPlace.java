package edu.cmu.glimpse.entry;

import android.os.Parcel;
import android.os.Parcelable;
import edu.cmu.glimpse.utils.Utils;

public class EntryPlace implements Parcelable {

    private String mName;
    private String mGooglePlaceReference;

    public EntryPlace(String name, String googlePlaceReference) {
        mName = name;
        mGooglePlaceReference = googlePlaceReference;
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

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeString(mGooglePlaceReference);
    }

    private void readFromParcel(Parcel in) {
        mName = in.readString();
        mGooglePlaceReference = in.readString();
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
