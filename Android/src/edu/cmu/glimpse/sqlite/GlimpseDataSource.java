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
import edu.cmu.glimpse.entry.GlimpseEntry;
import edu.cmu.glimpse.entry.GlimpseEntryPreview;

public class GlimpseDataSource {
    // Database fields
    private SQLiteDatabase mDatabase;
    private final GlimpseSQLiteHelper mDbHelper;
    private static final String[] mEntryColumns = {
            GlimpseSQLiteHelper.ENTRY_COLUMN_ID,
            GlimpseSQLiteHelper.ENTRY_COLUMN_CREATED,
            GlimpseSQLiteHelper.ENTRY_COLUMN_EDITED,
            GlimpseSQLiteHelper.ENTRY_COLUMN_CONTENT
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
    }

    public void close() {
        mDbHelper.close();
    }

    public GlimpseEntry createEntry(String content) {
        Log.d(this.getClass().getName(), "Create new entry with: " + content);

        ContentValues values = new ContentValues();
        values.put(GlimpseSQLiteHelper.ENTRY_COLUMN_CREATED, System.currentTimeMillis());
        values.put(GlimpseSQLiteHelper.ENTRY_COLUMN_EDITED, System.currentTimeMillis());
        values.put(GlimpseSQLiteHelper.ENTRY_COLUMN_CONTENT, content);
        long insertId = mDatabase.insert(GlimpseSQLiteHelper.TABLE_ENTRY, null, values);
        return getEntryWithId(insertId);
    }

    public void insertImage(long entryId, EntryImage entryImage) {
        Log.d(this.getClass().getName(), "Insert new image: " + entryImage);

        ContentValues values = new ContentValues();
        values.put(GlimpseSQLiteHelper.IMG_COLUMN_ENTRYID, entryId);
        values.put(GlimpseSQLiteHelper.IMG_COLUMN_IMAGEID, entryImage.getImageId());
        values.put(GlimpseSQLiteHelper.IMG_COLUMN_IMGDATA, entryImage.getImageData());

        mDatabase.insert(GlimpseSQLiteHelper.TABLE_IMG, null, values);
    }

    public GlimpseEntry updateEntry(long id, String content) {
        Log.d(this.getClass().getName(), "Update entry id: " + id + " with: " + content);

        GlimpseEntry entry = getEntryWithId(id);

        ContentValues values = new ContentValues();
        values.put(GlimpseSQLiteHelper.ENTRY_COLUMN_CREATED, entry.getCreatedTime());
        values.put(GlimpseSQLiteHelper.ENTRY_COLUMN_EDITED, System.currentTimeMillis());
        values.put(GlimpseSQLiteHelper.ENTRY_COLUMN_CONTENT, content);

        mDatabase.update(GlimpseSQLiteHelper.TABLE_ENTRY, values, GlimpseSQLiteHelper.ENTRY_COLUMN_ID + " = " + id,
                null);

        return getEntryWithId(id);
    }

    public void deleteEntry(GlimpseEntry entry) {
        long id = entry.getId();
        mDatabase.delete(GlimpseSQLiteHelper.TABLE_ENTRY, GlimpseSQLiteHelper.ENTRY_COLUMN_ID + " = " + id, null);
    }

    public List<GlimpseEntry> getAllEntries() {
        List<GlimpseEntry> entries = new ArrayList<GlimpseEntry>();

        Log.d(this.getClass().getName(), "Query all entries");

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
        Log.d(this.getClass().getName(), "Query all images for entryId = " + entryId);

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
        return new GlimpseEntry(cursor.getLong(0), cursor.getLong(1), cursor.getLong(2), cursor.getString(3));
    }

    private EntryImage cursorToEntryImage(Cursor cursor) {
        return new EntryImage(cursor.getLong(1), cursor.getBlob(2));
    }
}
