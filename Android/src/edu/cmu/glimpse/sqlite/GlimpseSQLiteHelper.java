package edu.cmu.glimpse.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class GlimpseSQLiteHelper extends SQLiteOpenHelper {
    public static final String TABLE_ENTRY = "glimpse";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_CREATED = "created";
    public static final String COLUMN_EDITED = "edited";

    private static final String DATABASE_NAME = "glimpse.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation SQL statement
    private static final String DATABASE_CREATE = "create table " + TABLE_ENTRY
            + "(" + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_CREATED + " integer, "
            + COLUMN_EDITED + " integer, "
            + COLUMN_CONTENT + " text);";

    public GlimpseSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(GlimpseSQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ENTRY);
        onCreate(db);
    }
}
