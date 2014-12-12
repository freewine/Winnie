package me.freewine.winnie;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by wuwantao on 14-11-16.
 */
public final class HistoryContract implements BaseColumns{
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public HistoryContract() {}

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "History.db";

    public static final String TABLE_NAME = "history";
    public static final String COLUMN_NAME_TIME = "time";
    public static final String COLUMN_NAME_CNT = "count";
    public static final String COLUMN_NAME_CHANGE = "change";

    public static final String AUTHORITY = "me.freewine.winnie";


    // The URI scheme used for content URIs
    public static final String SCHEME = "content";

    /**
     * The BDProvider content URI
     */
    public static final Uri CONTENT_URI = Uri.parse(SCHEME + "://" + AUTHORITY);

    /**
     * Contact table content URI
     */
    public static final Uri HISTORY_TABLE_CONTENTURI =
            Uri.withAppendedPath(CONTENT_URI, TABLE_NAME);
}
