package edu.cmu.glimpse.sqlite;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import edu.cmu.glimpse.entry.GlimpseEntry;
import edu.cmu.glimpse.entry.GlimpseEntryPreview;

public class GlimpseDataSource {
    // Database fields
    private SQLiteDatabase mDatabase;
    private final GlimpseSQLiteHelper mDbHelper;
    private final String[] mAllColumns = {
            GlimpseSQLiteHelper.COLUMN_ID,
            GlimpseSQLiteHelper.COLUMN_CREATED,
            GlimpseSQLiteHelper.COLUMN_EDITED,
            GlimpseSQLiteHelper.COLUMN_CONTENT
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
        values.put(GlimpseSQLiteHelper.COLUMN_CREATED, System.currentTimeMillis());
        values.put(GlimpseSQLiteHelper.COLUMN_EDITED, System.currentTimeMillis());
        values.put(GlimpseSQLiteHelper.COLUMN_CONTENT, content);
        long insertId = mDatabase.insert(GlimpseSQLiteHelper.TABLE_ENTRY, null, values);
        Cursor cursor = mDatabase.query(GlimpseSQLiteHelper.TABLE_ENTRY, mAllColumns, GlimpseSQLiteHelper.COLUMN_ID
                + " = " + insertId, null, null, null, null);
        cursor.moveToFirst();
        GlimpseEntry newEntry = cursorToGlimpseEntry(cursor);
        cursor.close();
        return newEntry;
    }

    public void updateEntry(int id, String content) {
        Log.d(this.getClass().getName(), "Update entry id: " + id + " with: " + content);

        Cursor cursor = mDatabase.query(GlimpseSQLiteHelper.TABLE_ENTRY, mAllColumns, GlimpseSQLiteHelper.COLUMN_ID
                + " = " + id, null, null, null, null);
        cursor.moveToFirst();
        GlimpseEntry entry = cursorToGlimpseEntry(cursor);
        cursor.close();

        ContentValues values = new ContentValues();
        values.put(GlimpseSQLiteHelper.COLUMN_CREATED, entry.getCreatedTime());
        values.put(GlimpseSQLiteHelper.COLUMN_EDITED, System.currentTimeMillis());
        values.put(GlimpseSQLiteHelper.COLUMN_CONTENT, content);

        mDatabase.update(GlimpseSQLiteHelper.TABLE_ENTRY, values, GlimpseSQLiteHelper.COLUMN_ID + " = " + id, null);
    }

    public void deleteEntry(GlimpseEntry entry) {
        int id = entry.getId();
        mDatabase.delete(GlimpseSQLiteHelper.TABLE_ENTRY, GlimpseSQLiteHelper.COLUMN_ID + " = " + id, null);
    }

    public List<GlimpseEntry> getAllEntries() {
        List<GlimpseEntry> entries = new ArrayList<GlimpseEntry>();

        Log.d(this.getClass().getName(), "Query all entries");

        Cursor cursor = mDatabase.query(GlimpseSQLiteHelper.TABLE_ENTRY, mAllColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            GlimpseEntry entry = cursorToGlimpseEntry(cursor);
            entries.add(entry);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return entries;
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

    private GlimpseEntry cursorToGlimpseEntry(Cursor cursor) {
        return new GlimpseEntry(cursor.getInt(0), cursor.getLong(1), cursor.getLong(2), cursor.getString(3));
    }
}
