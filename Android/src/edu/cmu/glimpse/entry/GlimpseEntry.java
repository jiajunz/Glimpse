package edu.cmu.glimpse.entry;

import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Class represents an entry
 * 
 * @author hanqingl
 * 
 */
public class GlimpseEntry implements Parcelable {

    private long mId;
    private long mCreate;
    private long mLastEdit;
    private String mContent;
    private GlimpseEntryPreview mPreviewContent;
    private List<EntryImage> mImageList;

    /**
     * Initiate a new entry with create time, last edit time and content
     * 
     * @param id
     *            id of the entry
     * @param create
     *            create time of the entry
     * @param lastEdit
     *            last edit time of the entry
     * @param content
     *            content of the entry
     */
    public GlimpseEntry(long id, long create, long lastEdit, String content) {
        mId = id;
        mCreate = create;
        mLastEdit = lastEdit;
        mContent = content;
        mPreviewContent = new GlimpseEntryPreview(id, this, content);
    }

    public GlimpseEntry(Parcel in) {
        readFromParcel(in);
    }

    public static final Parcelable.Creator<GlimpseEntry> CREATOR = new Parcelable.Creator<GlimpseEntry>() {
        public GlimpseEntry createFromParcel(Parcel in) {
            return new GlimpseEntry(in);
        }

        public GlimpseEntry[] newArray(int size) {
            return new GlimpseEntry[size];
        }
    };

    public void setImageList(List<EntryImage> imageList) {
        mImageList = imageList;
    }

    public long getId() {
        return mId;
    }

    public long getCreatedTime() {
        return mCreate;
    }

    public long getLastEditTime() {
        return mLastEdit;
    }

    public String getContent() {
        return mContent;
    }

    public GlimpseEntryPreview getPreview() {
        return mPreviewContent;
    }

    public List<EntryImage> getImageList() {
        return mImageList;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mId);
        dest.writeLong(mCreate);
        dest.writeLong(mLastEdit);
        dest.writeString(mContent);
    }

    private void readFromParcel(Parcel in) {
        mId = in.readLong();
        mCreate = in.readLong();
        mLastEdit = in.readLong();
        mContent = in.readString();
        mPreviewContent = new GlimpseEntryPreview(mId, this, mContent);
    }
}
