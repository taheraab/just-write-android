package com.ekavali.justwrite;

/**
 * Created by tbodabha on 12/12/2014.
 */
import android.provider.BaseColumns;
import android.net.Uri;

/**
 * This class defines the justwrite database table schema
 */
public final class StoryContract {
    public static final String AUTHORITY = "com.ekavali.justwrite.provider.story";
    public static final String CONTENT_TYPE_DIR_PREFIX = "vnd.android.cursor.dir/vnd." + AUTHORITY + ".";
    public static final String CONTENT_TYPE_ITEM_PREFIX = "vnd.android.cursor.item/vnd." + AUTHORITY + ".";

    //common column names for all tables
    public static final String COLUMN_NAME_CREATE_DATE = "created";
    public static final String COLUMN_NAME_MODIFY_DATE = "modified";
    public static final String DEFAULT_SORT_ORDER = "modified DESC";

    // Private constructor to prevent instantiation of this class
    private StoryContract() {}

    /* Story table */
    public static abstract class Story implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/stories");
        public static final Uri CONTENT_ID_URI = Uri.parse("content://" + AUTHORITY + "/story/");
        public static final String TABLE_NAME = "story";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
    }
}