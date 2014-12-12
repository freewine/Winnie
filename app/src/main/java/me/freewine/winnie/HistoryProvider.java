/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.freewine.winnie;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;
import android.util.SparseArray;

/**
 * Defines a ContentProvider that stores persons and messages
 * The provider also has a table that store points
 */
public class HistoryProvider extends ContentProvider {
    // Indicates that the incoming query is for a person
    public static final int HISTORY_QUERY = 1;

    // Indicates an invalid content URI
    public static final int INVALID_URI = -1;

    // Constants for building SQLite tables during initialization
    private static final String PRIMARY_KEY_TYPE = "INTEGER PRIMARY KEY";

    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String LONG_TYPE = " LONG";

    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + HistoryContract.TABLE_NAME + " (" +
                    HistoryContract._ID + " INTEGER PRIMARY KEY," +
                    HistoryContract.COLUMN_NAME_CNT + INTEGER_TYPE + " NOT NULL " + COMMA_SEP +
                    HistoryContract.COLUMN_NAME_CHANGE + INTEGER_TYPE + " NOT NULL " + COMMA_SEP +
                    HistoryContract.COLUMN_NAME_TIME + LONG_TYPE + " NOT NULL " +
                    " )";

    // Identifies log statements issued by this component
    public static final String LOG_TAG = "HistoryProvider";

    // Defines an helper object for the backing database
    private SQLiteOpenHelper mHelper;

    // Defines a helper object that matches content URIs to table-specific parameters
    private static final UriMatcher sUriMatcher;

    // Stores the MIME types served by this provider
    private static final SparseArray<String> sMimeTypes;

    /*
     * Initializes meta-data used by the content provider:
     * - UriMatcher that maps content URIs to codes
     * - MimeType array that returns the custom MIME type of a table
     */
    static {

        // Creates an object that associates content URIs with numeric codes
        sUriMatcher = new UriMatcher(0);

        /*
         * Sets up an array that maps content URIs to MIME types, via a mapping between the
         * URIs and an integer code. These are custom MIME types that apply to tables and rows
         * in this particular provider.
         */
        sMimeTypes = new SparseArray<String>();


        // Adds a URI "match" entry that maps picture URL content URIs to a numeric code
        sUriMatcher.addURI(
                HistoryContract.AUTHORITY,
                HistoryContract.TABLE_NAME,
                HISTORY_QUERY);


        // Specifies a custom MIME type for the picture URL table
        sMimeTypes.put(
                HISTORY_QUERY,
                "vnd.android.cursor.dir/vnd." +
                        HistoryContract.AUTHORITY + "." +
                        HistoryContract.TABLE_NAME
        );
    }

    // Closes the SQLite database helper class, to avoid memory leaks
    public void close() {
        mHelper.close();
    }

    /**
     * Defines a helper class that opens the SQLite database for this provider when a request is
     * received. If the database doesn't yet exist, the helper creates it.
     */
    private class DataProviderHelper extends SQLiteOpenHelper {
        /**
         * Instantiates a new SQLite database using the supplied database name and version
         *
         * @param context The current context
         */
        DataProviderHelper(Context context) {
            super(context,
                    HistoryContract.DATABASE_NAME,
                    null,
                    HistoryContract.DATABASE_VERSION);
        }


        /**
         * Executes the queries to drop all of the tables from the database.
         *
         * @param db A handle to the provider's backing database.
         */
        private void dropTables(SQLiteDatabase db) {

            // If the table doesn't exist, don't throw an error
            db.execSQL("DROP TABLE IF EXISTS " + HistoryContract.TABLE_NAME);
        }

        /**
         * Does setup of the database. The system automatically invokes this method when
         * SQLiteDatabase.getWriteableDatabase() or SQLiteDatabase.getReadableDatabase() are
         * invoked and no db instance is available.
         *
         * @param db the database instance in which to create the tables.
         */
        @Override
        public void onCreate(SQLiteDatabase db) {
            // Creates the tables in the backing database for this provider
            db.execSQL(SQL_CREATE_ENTRIES);
        }

        /**
         * Handles upgrading the database from a previous version. Drops the old tables and creates
         * new ones.
         *
         * @param db       The database to upgrade
         * @param version1 The old database version
         * @param version2 The new database version
         */
        @Override
        public void onUpgrade(SQLiteDatabase db, int version1, int version2) {
            Log.w(DataProviderHelper.class.getName(),
                    "Upgrading database from version " + version1 + " to "
                            + version2 + ", which will destroy all the existing data"
            );

            // Drops all the existing tables in the database
            dropTables(db);

            // Invokes the onCreate callback to build new tables
            onCreate(db);
        }

        /**
         * Handles downgrading the database from a new to a previous version. Drops the old tables
         * and creates new ones.
         *
         * @param db       The database object to downgrade
         * @param version1 The old database version
         * @param version2 The new database version
         */
        @Override
        public void onDowngrade(SQLiteDatabase db, int version1, int version2) {
            Log.w(DataProviderHelper.class.getName(),
                    "Downgrading database from version " + version1 + " to "
                            + version2 + ", which will destroy all the existing data"
            );

            // Drops all the existing tables in the database
            dropTables(db);

            // Invokes the onCreate callback to build new tables
            onCreate(db);

        }
    }

    /**
     * Initializes the content provider. Notice that this method simply creates a
     * the SQLiteOpenHelper instance and returns. You should do most of the initialization of a
     * content provider in its static initialization block or in SQLiteDatabase.onCreate().
     */
    @Override
    public boolean onCreate() {

        // Creates a new database helper object
        mHelper = new DataProviderHelper(getContext());

        return true;
    }

    /**
     * Returns the result of querying the chosen table.
     *
     * @param uri           The content URI of the table
     * @param projection    The names of the columns to return in the cursor
     * @param selection     The selection clause for the query
     * @param selectionArgs An array of Strings containing search criteria
     * @param sortOrder     A clause defining the order in which the retrieved rows should be sorted
     * @return The query results, as a {@link android.database.Cursor} of rows and columns
     * @see android.content.ContentProvider#query(android.net.Uri, String[], String, String[], String)
     */
    @Override
    public Cursor query(
            Uri uri,
            String[] projection,
            String selection,
            String[] selectionArgs,
            String sortOrder) {

        SQLiteDatabase db = mHelper.getReadableDatabase();
        // Decodes the content URI and maps it to a code
        switch (sUriMatcher.match(uri)) {

            // If the query is for a picture URL
            case HISTORY_QUERY:
                // Does the query against a read-only version of the database
                Cursor returnCursor = db.query(
                        HistoryContract.TABLE_NAME,
                        projection,
                        selection, selectionArgs, null, null, sortOrder);

                // Sets the ContentResolver to watch this content URI for data changes
                returnCursor.setNotificationUri(getContext().getContentResolver(), uri);
                return returnCursor;
            case INVALID_URI:

                throw new IllegalArgumentException("Query -- Invalid URI:" + uri);
        }

        return null;
    }

    /**
     * Returns the mimeType associated with the Uri (query).
     *
     * @param uri the content URI to be checked
     * @return the corresponding MIMEtype
     * @see android.content.ContentProvider#getType(android.net.Uri)
     */
    @Override
    public String getType(Uri uri) {

        return sMimeTypes.get(sUriMatcher.match(uri));
    }

    /**
     * Insert a single row into a table
     *
     * @param uri    the content URI of the table
     * @param values a {@link android.content.ContentValues} object containing the row to insert
     * @return the content URI of the new row
     * @see android.content.ContentProvider#insert(android.net.Uri, android.content.ContentValues)
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase localSQLiteDatabase;
        long id;

        // Decode the URI to choose which action to take
        switch (sUriMatcher.match(uri)) {
            // For the contact date table
            case HISTORY_QUERY:

                // Creates a writeable database or gets one from cache
                localSQLiteDatabase = mHelper.getWritableDatabase();

                // Inserts the row into the table and returns the new row's _id value
                id = localSQLiteDatabase.insertWithOnConflict(
                        HistoryContract.TABLE_NAME,
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_REPLACE
                );

                // If the insert succeeded, notify a change and return the new row's content URI.
                if (-1 != id) {
                    getContext().getContentResolver().notifyChange(uri, null);
                    return Uri.withAppendedPath(uri, String.valueOf(id));
                } else {

                    throw new SQLiteException("Insert error:" + uri);
                }

            case INVALID_URI:

                throw new IllegalArgumentException("Insert: Invalid URI" + uri);
        }
        return null;

    }


    /**
     * Returns an UnsupportedOperationException if delete is called
     *
     * @param uri           The content URI
     * @param selection     The SQL WHERE string. Use "?" to mark places that should be substituted by
     *                      values in selectionArgs.
     * @param selectionArgs An array of values that are mapped to each "?" in selection. If no "?"
     *                      are used, set this to NULL.
     * @return the number of rows deleted
     * @see android.content.ContentProvider#delete(android.net.Uri, String, String[])
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Gets a writeable database instance if one is not already cached
        SQLiteDatabase localSQLiteDatabase;
        int rows;

        switch (sUriMatcher.match(uri)) {
            case HISTORY_QUERY:
                localSQLiteDatabase = mHelper.getWritableDatabase();
                rows = localSQLiteDatabase.delete(HistoryContract.TABLE_NAME,
                        selection,
                        selectionArgs);
                // If the insert succeeded, notify a change and return the new row's content URI.
                if (-1 != rows) {
                    getContext().getContentResolver().notifyChange(uri, null);
                    return rows;
                } else {

                    throw new SQLiteException("Delete person error:" + uri);
                }

            case INVALID_URI:
                throw new UnsupportedOperationException("Delete -- unsupported operation " + uri);
        }

        return -1;
    }

    /**
     * Updates one or more rows in a table.
     *
     * @param uri           The content URI for the table
     * @param values        The values to use to update the row or rows. You only need to specify column
     *                      names for the columns you want to change. To clear the contents of a column, specify the
     *                      column name and NULL for its value.
     * @param selection     An SQL WHERE clause (without the WHERE keyword) specifying the rows to
     *                      update. Use "?" to mark places that should be substituted by values in selectionArgs.
     * @param selectionArgs An array of values that are mapped in order to each "?" in selection.
     *                      If no "?" are used, set this to NULL.
     * @return int The number of rows updated.
     * @see android.content.ContentProvider#update(android.net.Uri, android.content.ContentValues, String, String[])
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        SQLiteDatabase localSQLiteDatabase;
        int rows;

        // Decodes the content URI and choose which insert to use
        switch (sUriMatcher.match(uri)) {
            case HISTORY_QUERY:
                // Creats a new writeable database or retrieves a cached one
                localSQLiteDatabase = mHelper.getWritableDatabase();

                // Updates the table
                rows = localSQLiteDatabase.update(
                        HistoryContract.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);

                // If the update succeeded, notify a change and return the number of updated rows.
                if (0 != rows) {
                    getContext().getContentResolver().notifyChange(uri, null);
                    return rows;
                } else {

                    throw new SQLiteException("Update error:" + uri);
                }

            case INVALID_URI:
                throw new IllegalArgumentException("Update: Invalid URI: " + uri);
        }

        return -1;
    }
}
