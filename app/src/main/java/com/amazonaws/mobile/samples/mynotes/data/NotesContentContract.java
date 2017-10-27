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

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Per the official Android documentation, this class defines all publically available
 * elements, like the authority, the content URIs, columns, and content types for each
 * element
 */
public class NotesContentContract {
    /**
     * The authority of the notes content provider - this must match the authority
     * specified in the AndroidManifest.xml provider section
     */
    public static final String AUTHORITY = "com.amazonaws.mobile.samples.mynotes.provider";

    /**
     * The content URI for the top-level notes authority
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    /**
     * Constants for the Notes table
     */
    public static final class Notes implements BaseColumns {
        /**
         * The Table Name
         */
        public static final String TABLE_NAME = "notes";

        /**
         * The internal ID
         */
        public static final String _ID = "id";

        /**
         * The noteId field
         */
        public static final String NOTEID = "noteId";

        /**
         * The title field
         */
        public static final String TITLE = "title";

        /**
         * The content field
         */
        public static final String CONTENT = "content";

        /**
         * The created field
         */
        public static final String CREATED = "created";

        /**
         * The updated field
         */
        public static final String UPDATED = "updated";

        /**
         * The directory base-path
         */
        public static final String DIR_BASEPATH = "notes";

        /**
         * The items base-path
         */
        public static final String ITEM_BASEPATH = "notes/*";

        /**
         * The SQLite database command to create the table
         */
        public static final String CREATE_SQLITE_TABLE =
                "CREATE TABLE " + TABLE_NAME + "("
                        + _ID + " INTEGER PRIMARY KEY, "
                        + NOTEID + " TEXT UNIQUE NOT NULL, "
                        + TITLE + " TEXT NOT NULL DEFAULT '', "
                        + CONTENT + " TEXT NOT NULL DEFAULT '', "
                        + CREATED + " BIGINT NOT NULL DEFAULT 0, "
                        + UPDATED + " BIGINT NOT NULL DEFAULT 0)";

        /**
         * The content URI for this table
         */
        public static final Uri CONTENT_URI =
                Uri.withAppendedPath(NotesContentContract.CONTENT_URI, TABLE_NAME);

        /**
         * The mime type of a directory of items
         */
        public static final String CONTENT_DIR_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.com.amazonaws.mobile.samples.notes";

        /**
         * The mime type of a single item
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.com.amazonaws.mobile.samples.notes";

        /**
         * A projection of all columns in the items table
         */
        public static final String[] PROJECTION_ALL = {
                _ID,
                NOTEID,
                TITLE,
                CONTENT,
                CREATED,
                UPDATED
        };

        /**
         * The default sort order (SQLite syntax)
         */
        public static final String SORT_ORDER_DEFAULT = CREATED + " ASC";

        /**
         * Build a URI for the provided note
         * @param noteId the ID of the provided note
         * @return the URI of the provided note
         */
        public static Uri uriBuilder(String noteId) {
            Uri item = new Uri.Builder()
                    .scheme("content")
                    .authority(NotesContentContract.AUTHORITY)
                    .appendPath(NotesContentContract.Notes.DIR_BASEPATH)
                    .appendPath(noteId)
                    .build();
            return item;
        }
    }
}
