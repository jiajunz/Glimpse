package edu.cmu.glimpse.entry;

import java.io.ByteArrayOutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class EntryImage {

    private long mImageId;
    private final byte[] mImageData;

    public EntryImage(long imageId, Bitmap image) {
        this(imageId, bitmapToByteArray(image));
    }

    public EntryImage(long imageId, byte[] imageData) {
        mImageId = imageId;
        mImageData = imageData;
    }

    private static byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }

    public void setImageId(long imageId) {
        mImageId = imageId;
    }

    public long getImageId() {
        return mImageId;
    }

    public byte[] getImageData() {
        return mImageData;
    }

    public Bitmap getImage() {
        return BitmapFactory.decodeByteArray(mImageData, 0, mImageData.length);
    }

    @Override
    public String toString() {
        return "image id: " + mImageId;
    }
}
