package edu.cmu.glimpse.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class GlimpseSQLiteHelper extends SQLiteOpenHelper {
    public static final String TABLE_ENTRY = "glimpse";
    public static final String TABLE_IMG = "image";

    public static final String ENTRY_COLUMN_ID = "id";
    public static final String ENTRY_COLUMN_CONTENT = "content";
    public static final String ENTRY_COLUMN_CREATED = "created";
    public static final String ENTRY_COLUMN_EDITED = "edited";

    public static final String IMG_COLUMN_ENTRYID = "entry_id";
    public static final String IMG_COLUMN_IMAGEID = "img_id";
    public static final String IMG_COLUMN_IMGDATA = "image_data";

    private static final String DATABASE_NAME = "glimpse.db";
    private static final int DATABASE_VERSION = 2;

    // Database creation SQL statement
    private static final String DATABASE_ENTRY_CREATE = "create table " + TABLE_ENTRY
            + "(" + ENTRY_COLUMN_ID + " integer primary key autoincrement, "
            + ENTRY_COLUMN_CREATED + " integer, "
            + ENTRY_COLUMN_EDITED + " integer, "
            + ENTRY_COLUMN_CONTENT + " text);";

    private static final String DATABASE_IMAGE_CREATE = "create table " + TABLE_IMG
            + "(" + IMG_COLUMN_ENTRYID + " integer, "
            + IMG_COLUMN_IMAGEID + " integer, "
            + IMG_COLUMN_IMGDATA + " blob, "
            + "primary key (" + IMG_COLUMN_ENTRYID + ", " + IMG_COLUMN_IMAGEID + "));";

    public GlimpseSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_ENTRY_CREATE);
        database.execSQL(DATABASE_IMAGE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(GlimpseSQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ENTRY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_IMG);
        onCreate(db);
    }
}
