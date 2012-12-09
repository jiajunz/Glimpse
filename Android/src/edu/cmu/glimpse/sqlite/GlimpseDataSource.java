package edu.cmu.glimpse.sqlite;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import edu.cmu.glimpse.entry.EntryImage;
import edu.cmu.glimpse.entry.EntryPlace;
import edu.cmu.glimpse.entry.GlimpseEntry;
import edu.cmu.glimpse.entry.GlimpseEntryPreview;
import edu.cmu.glimpse.modules.GlimpseAccountManager;

public class GlimpseDataSource {
    private static final String TAG = "GlimpseDataSource";

    private SQLiteDatabase mDatabase;
    private final GlimpseSQLiteHelper mDbHelper;
    private static final String[] mEntryColumns = {
            GlimpseSQLiteHelper.ENTRY_COLUMN_ID,
            GlimpseSQLiteHelper.ENTRY_COLUMN_CREATED,
            GlimpseSQLiteHelper.ENTRY_COLUMN_EDITED,
            GlimpseSQLiteHelper.ENTRY_COLUMN_CONTENT,
            GlimpseSQLiteHelper.ENTRY_COLUMN_LOCATION_NAME,
            GlimpseSQLiteHelper.ENTRY_COLUMN_LOCATION_GOOGLE_REFERENCE
    };
    private static final String[] mImageColumns = {
            GlimpseSQLiteHelper.IMG_COLUMN_ENTRYID,
            GlimpseSQLiteHelper.IMG_COLUMN_IMAGEID,
            GlimpseSQLiteHelper.IMG_COLUMN_IMGDATA
    };

    public GlimpseDataSource(Context context) {
        mDbHelper = new GlimpseSQLiteHelper(context);
    }

    public void open() {
        mDatabase = mDbHelper.getWritableDatabase();
        GlimpseAccountManager.syncDropbox();
    }

    public void close() {
        mDbHelper.close();
        GlimpseAccountManager.syncDropbox();
    }

    public GlimpseEntry createEntry(String content, EntryPlace place) {
        Log.d(TAG, "Create new entry with: " + content);

        ContentValues values = new ContentValues();
        values.put(GlimpseSQLiteHelper.ENTRY_COLUMN_CREATED, System.currentTimeMillis());
        values.put(GlimpseSQLiteHelper.ENTRY_COLUMN_EDITED, System.currentTimeMillis());
        if (place != null) {
            values.put(GlimpseSQLiteHelper.ENTRY_COLUMN_LOCATION_NAME, place.getName());
            values.put(GlimpseSQLiteHelper.ENTRY_COLUMN_LOCATION_GOOGLE_REFERENCE, place.getGooglePlaceReference());
        }
        values.put(GlimpseSQLiteHelper.ENTRY_COLUMN_CONTENT, content);
        long insertId = mDatabase.insert(GlimpseSQLiteHelper.TABLE_ENTRY, null, values);
        return getEntryWithId(insertId);
    }

    public void insertImage(long entryId, EntryImage entryImage) {
        Log.d(TAG, "Insert new image: " + entryImage);

        ContentValues values = new ContentValues();
        values.put(GlimpseSQLiteHelper.IMG_COLUMN_ENTRYID, entryId);
        values.put(GlimpseSQLiteHelper.IMG_COLUMN_IMAGEID, entryImage.getImageId());
        values.put(GlimpseSQLiteHelper.IMG_COLUMN_IMGDATA, entryImage.getImageData());

        mDatabase.insert(GlimpseSQLiteHelper.TABLE_IMG, null, values);
    }

    /**
     * Remove an image from database
     * 
     * @param entryId
     * @param entryImage
     * @return the number of rows affected if a whereClause is passed in, 0
     *         otherwise. To remove all rows and get a count pass "1" as the
     *         whereClause.
     */
    public int deleteImage(long entryId, EntryImage entryImage) {
        Log.d(TAG, "Remove image: " + entryImage);

        return mDatabase.delete(GlimpseSQLiteHelper.TABLE_IMG, GlimpseSQLiteHelper.IMG_COLUMN_ENTRYID + " = " + entryId
                + " and " + GlimpseSQLiteHelper.IMG_COLUMN_IMAGEID + " = " + entryImage.getImageId(), null);
    }

    public GlimpseEntry updateEntry(long id, String content, EntryPlace place) {
        Log.d(TAG, "Update entry id: " + id + " with: " + content);

        GlimpseEntry entry = getEntryWithId(id);

        ContentValues values = new ContentValues();
        values.put(GlimpseSQLiteHelper.ENTRY_COLUMN_CREATED, entry.getCreatedTime());
        values.put(GlimpseSQLiteHelper.ENTRY_COLUMN_EDITED, System.currentTimeMillis());
        values.put(GlimpseSQLiteHelper.ENTRY_COLUMN_CONTENT, content);

        if (place != null && !place.equals(entry.getPlace())) {
            values.put(GlimpseSQLiteHelper.ENTRY_COLUMN_LOCATION_NAME, place.getName());
            values.put(GlimpseSQLiteHelper.ENTRY_COLUMN_LOCATION_GOOGLE_REFERENCE, place.getGooglePlaceReference());
        }

        mDatabase.update(GlimpseSQLiteHelper.TABLE_ENTRY, values, GlimpseSQLiteHelper.ENTRY_COLUMN_ID + " = " + id,
                null);

        return getEntryWithId(id);
    }

    public void deleteEntry(GlimpseEntry entry) {
        long id = entry.getId();
        deleteEntry(id);
    }

    public void deleteEntry(long entryId) {
        Log.d(TAG, "Delete entry with id: " + entryId);

        mDatabase.delete(GlimpseSQLiteHelper.TABLE_ENTRY, GlimpseSQLiteHelper.ENTRY_COLUMN_ID + " = " + entryId, null);
    }

    public List<GlimpseEntry> getAllEntries() {
        List<GlimpseEntry> entries = new ArrayList<GlimpseEntry>();

        Log.d(TAG, "Query all entries");

        Cursor cursor = mDatabase.query(GlimpseSQLiteHelper.TABLE_ENTRY, mEntryColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            entries.add(cursorToGlimpseEntry(cursor));
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return entries;
    }

    public List<EntryImage> getImagesForEntry(long entryId) {
        Log.d(TAG, "Query all images for entryId = " + entryId);

        List<EntryImage> images = new ArrayList<EntryImage>();
        String selection = GlimpseSQLiteHelper.IMG_COLUMN_ENTRYID + " = " + entryId;
        Cursor cursor = mDatabase
                .query(GlimpseSQLiteHelper.TABLE_IMG, mImageColumns, selection, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            images.add(cursorToEntryImage(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return images;
    }

    public List<GlimpseEntryPreview> getEntryForOneDay(Calendar calendar) {
        List<GlimpseEntryPreview> previews = new ArrayList<GlimpseEntryPreview>();

        List<GlimpseEntry> enties = getAllEntries();
        for (GlimpseEntry entry : enties) {
            Calendar entryCal = Calendar.getInstance();
            entryCal.setTimeInMillis(entry.getCreatedTime());
            if (entryCal.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                    && entryCal.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR))
            {
                previews.add(entry.getPreview());
            }
        }

        return previews;
    }

    private GlimpseEntry getEntryWithId(long id) {
        Cursor cursor = mDatabase.query(GlimpseSQLiteHelper.TABLE_ENTRY, mEntryColumns,
                GlimpseSQLiteHelper.ENTRY_COLUMN_ID
                        + " = " + id, null, null, null, null);
        cursor.moveToFirst();
        GlimpseEntry entry = cursorToGlimpseEntry(cursor);
        cursor.close();

        return entry;
    }

    private GlimpseEntry cursorToGlimpseEntry(Cursor cursor) {
        EntryPlace place = new EntryPlace(cursor.getString(4), cursor.getString(5));
        return new GlimpseEntry(cursor.getLong(0), cursor.getLong(1), cursor.getLong(2), cursor.getString(3), place);
    }

    private EntryImage cursorToEntryImage(Cursor cursor) {
        return new EntryImage(cursor.getLong(1), cursor.getBlob(2));
    }
}
