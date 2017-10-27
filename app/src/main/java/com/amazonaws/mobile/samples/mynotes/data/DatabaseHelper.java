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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Android has a base class for dealing with SQLite databases called SQLiteOpenHelper.
 * This is a derived class.  Its main purpose is to set up the database on first access.
 */
class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DBNAME = "notes.db";
    private static final int DBVERSION = 1;

    /**
     * Create a new SQLiteOpenHelper object for this database.
     * @param context the application context
     */
    DatabaseHelper(Context context) {
        super(context, DBNAME, null, DBVERSION);
    }

    /**
     * Called when the database needs to be created
     * @param db the database handle
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(NotesContentContract.Notes.CREATE_SQLITE_TABLE);
    }

    /**
     * Called when the database needs to be updated
     * @param db the database handle
     * @param oldVersion the old database version
     * @param newVersion the new database version
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Don't do anything here - we don't support upgrade yet.
    }
}
