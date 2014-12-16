package com.ekavali.justwrite;

/**
 * Created by tbodabha on 12/12/2014.
 */
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.UriMatcher;
import android.util.Log;
import android.net.Uri;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;


/**
 This class implements data provider for the Just Write app, for stories, chapters and notes
 */
public class StoryProvider extends ContentProvider {
    private static final String TAG = "StoryProvider";
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int STORIES = 1;
    private DatabaseHelper mDbHelper;

    //Initialize UriMatcher
    static {
        //Identifies the Uri to return all stories
        sURIMatcher.addURI(StoryContract.AUTHORITY, "stories", STORIES);

    }

    /**
     * This helper class creates the justwrite database table schema
     */
    private static class DatabaseHelper extends SQLiteOpenHelper{
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "JustWrite.db";

        public static final String CREATE_STORY_TABLE =
                "CREATE TABLE " + StoryContract.Story.TABLE_NAME +  "(" + StoryContract.Story._ID + " INTEGER PRIMARY KEY," +
                        StoryContract.Story.COLUMN_NAME_TITLE + " TEXT," + StoryContract.Story.COLUMN_NAME_DESCRIPTION + " TEXT," +
                        StoryContract.COLUMN_NAME_CREATE_DATE + " INTEGER," + StoryContract.COLUMN_NAME_MODIFY_DATE + " INTEGER)";

        public static final String DROP_STORY_TABLE =
                "DROP TABLE IF EXISTS " + StoryContract.Story.TABLE_NAME;

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_STORY_TABLE);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            //Destroy the old data and start over.
            db.execSQL(DROP_STORY_TABLE);
            onCreate(db);
        }
    }

    /**
     * Initialize database helper
     */
    public boolean onCreate() {
        mDbHelper = new DatabaseHelper(getContext());
        return true;
    }

    /**
     * Insert a new record specified by the Uri.
     * Returns Uri of new record.
     */
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        switch (sURIMatcher.match(uri)) {
            case STORIES: //insert new story record
                long now = System.currentTimeMillis();
                values.put(StoryContract.COLUMN_NAME_CREATE_DATE, now);
                values.put(StoryContract.COLUMN_NAME_MODIFY_DATE, now);
                long newRowId = db.insert(StoryContract.Story.TABLE_NAME, null, values);
                if (newRowId > 0) {
                    Uri storyUri = ContentUris.withAppendedId(StoryContract.Story.CONTENT_ID_URI, newRowId);
                    Log.v(TAG, "newStoryUri: " + storyUri);
                    return storyUri;
                }else
                    throw new SQLException("Failed to insert story into  " + uri);
            default:
                throw new IllegalArgumentException("Not a valid URI: " + uri);
        }
    }

    /**
     * Return a cursor with results of a query. The cursor exists but is empty if query returns no result
     * or when an exception is thrown
     */
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        switch (sURIMatcher.match(uri)) {
            case STORIES:
                //Create a query based on the content url
                SQLiteDatabase db = mDbHelper.getReadableDatabase();
                Cursor cursor = db.query(
                        StoryContract.Story.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                //cursor.moveToFirst();
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    Log.v(TAG, "columns: " + cursor.getColumnName(i));
                }
                return cursor;
            default:
                throw new IllegalArgumentException("Uri not valid: " + uri);
        }
    }

    /**
     * Update a record
     * Returns the number of rows affected.
     */
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    /**
     * Delete a record
     * Returns the number of rows affected.
     */
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    /**
     * Return mime-type for content
     */
    public String getType (Uri uri) {
        switch(sURIMatcher.match(uri)) {
            case STORIES:
                return StoryContract.CONTENT_TYPE_DIR_PREFIX + StoryContract.Story.TABLE_NAME;
            default:
                throw new IllegalArgumentException("Not a valid URI: " + uri);
        }
    }

}