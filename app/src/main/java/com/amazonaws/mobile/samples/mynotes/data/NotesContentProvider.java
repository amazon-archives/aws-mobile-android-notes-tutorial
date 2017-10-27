/*
 * Copyright 2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file
 * except in compliance with the License. A copy of the License is located at
 *
 *    http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the License.
 */
package com.amazonaws.mobile.samples.mynotes.data;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.Locale;

/**
 * The Content Provider for the internal Notes database
 */
public class NotesContentProvider extends ContentProvider {
    /**
     * Creates a UriMatcher for matching the path elements for this content provider
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    /**
     * The code for the UriMatch matching all notes
     */
    private static final int ALL_ITEMS = 10;

    /**
     * The code for the UriMatch matching a single note
     */
    private static final int ONE_ITEM = 20;

    /**
     * The database helper for this content provider
     */
    private DatabaseHelper databaseHelper;

    /*
     * Initialize the UriMatcher with the URIs that this content provider handles
     */
    static {
        sUriMatcher.addURI(
                NotesContentContract.AUTHORITY,
                NotesContentContract.Notes.DIR_BASEPATH,
                ALL_ITEMS);
        sUriMatcher.addURI(
                NotesContentContract.AUTHORITY,
                NotesContentContract.Notes.ITEM_BASEPATH,
                ONE_ITEM);
    }

    /**
     * Part of the Content Provider interface.  The system calls onCreate() when it starts up
     * the provider.  You should only perform fast-running initialization tasks in this method.
     * Defer database creation and data loading until the provider actually receives a request
     * for the data.  This runs on the UI thread.
     *
     * @return true if the provider was successfully loaded; false otherwise
     */
    @Override
    public boolean onCreate() {
        databaseHelper = new DatabaseHelper(getContext());
        return true;
    }

    /**
     * Query for a (number of) records.
     *
     * @param uri The URI to query
     * @param projection The fields to return
     * @param selection The WHERE clause
     * @param selectionArgs Any arguments to the WHERE clause
     * @param sortOrder the sort order for the returned records
     * @return a Cursor that can iterate over the results
     */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        int uriType = sUriMatcher.match(uri);
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        switch (uriType) {
            case ALL_ITEMS:
                queryBuilder.setTables(NotesContentContract.Notes.TABLE_NAME);
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = NotesContentContract.Notes.SORT_ORDER_DEFAULT;
                }
                break;
            case ONE_ITEM:
                String where = getOneItemClause(uri.getLastPathSegment());
                queryBuilder.setTables(NotesContentContract.Notes.TABLE_NAME);
                queryBuilder.appendWhere(where);
                break;
        }

        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    /**
     * The content provider must return the content type for its supported URIs.  The supported
     * URIs are defined in the UriMatcher and the types are stored in the NotesContentContract.
     *
     * @param uri the URI for typing
     * @return the type of the URI
     */
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case ALL_ITEMS:
                return NotesContentContract.Notes.CONTENT_DIR_TYPE;
            case ONE_ITEM:
                return NotesContentContract.Notes.CONTENT_ITEM_TYPE;
            default:
                return null;
        }
    }

    /**
     * Insert a new record into the database.
     *
     * @param uri the base URI to insert at (must be a directory-based URI)
     * @param values the values to be inserted
     * @return the URI of the inserted item
     */
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        int uriType = sUriMatcher.match(uri);
        switch (uriType) {
            case ALL_ITEMS:
                SQLiteDatabase db = databaseHelper.getWritableDatabase();
                long id = db.insert(NotesContentContract.Notes.TABLE_NAME, null, values);
                if (id > 0) {
                    String noteId = values.getAsString(NotesContentContract.Notes.NOTEID);
                    Uri item = NotesContentContract.Notes.uriBuilder(noteId);
                    notifyAllListeners(item);
                    return item;
                }
                throw new SQLException(String.format(Locale.US, "Error inserting for URI %s - id = %d", uri, id));
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    /**
     * Delete one or more records from the SQLite database.
     *
     * @param uri the URI of the record(s) to delete
     * @param selection A WHERE clause to use for the deletion
     * @param selectionArgs Any arguments to replace the ? in the selection
     * @return the number of rows deleted.
     */
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int uriType = sUriMatcher.match(uri);
        int rows;
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        switch (uriType) {
            case ALL_ITEMS:
                rows = db.delete(
                        NotesContentContract.Notes.TABLE_NAME,  // The table name
                        selection, selectionArgs);              // The WHERE clause
                break;
            case ONE_ITEM:
                String where = getOneItemClause(uri.getLastPathSegment());
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                rows = db.delete(
                        NotesContentContract.Notes.TABLE_NAME,  // The table name
                        where, selectionArgs);                  // The WHERE clause
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        if (rows > 0) {
            notifyAllListeners(uri);
        }
        return rows;
    }

    /**
     * Part of the ContentProvider implementation.  Updates the record (based on the record URI)
     * with the specified ContentValues
     *
     * @param uri The URI of the record(s)
     * @param values The new values for the record(s)
     * @param selection If the URI is a directory, the WHERE clause
     * @param selectionArgs Arguments for the WHERE clause
     * @return the number of rows updated
     */
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int uriType = sUriMatcher.match(uri);
        int rows;
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        switch (uriType) {
            case ALL_ITEMS:
                rows = db.update(
                        NotesContentContract.Notes.TABLE_NAME,  // The table name
                        values,                                 // The values to replace
                        selection, selectionArgs);              // The WHERE clause
                break;
            case ONE_ITEM:
                String where = getOneItemClause(uri.getLastPathSegment());
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                rows = db.update(
                        NotesContentContract.Notes.TABLE_NAME,  // The table name
                        values,                                 // The values to replace
                        where, selectionArgs);                  // The WHERE clause
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        if (rows > 0) {
            notifyAllListeners(uri);
        }
        return rows;
    }

    /**
     * Notify all listeners that the specified URI has changed
     * @param uri the URI that changed
     */
    private void notifyAllListeners(Uri uri) {
        ContentResolver resolver = getContext().getContentResolver();
        if (resolver != null) {
            resolver.notifyChange(uri, null);
        }
    }

    private String getOneItemClause(String id) {
        return String.format("%s = \"%s\"", NotesContentContract.Notes.NOTEID, id);
    }
}
