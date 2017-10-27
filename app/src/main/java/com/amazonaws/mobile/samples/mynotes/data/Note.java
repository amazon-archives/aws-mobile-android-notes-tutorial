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

import android.content.ContentValues;
import android.database.Cursor;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.UUID;

/**
 * The Note model
 *
 * _id      The internal ID - only relevant to the current device
 * noteId   The global ID - should be unique globally
 * title    The note title
 * content  The note content
 * created  The timestamp (# ms since epoch UTC) that the note was created
 * updated  The timestamp (# ms since epoch UTC) that the note was last updated
 */
public class Note {
    private long id = -1;
    private String noteId;
    private String title;
    private String content;
    private DateTime created;
    private DateTime updated;

    /**
     * Create a new Note from a Cursor object.  This version provides default values for
     * any information that is missing; hopefully, this ensures that the method never crashes
     * the app.
     *
     * @param c the cursor to read from - it must be set to the right position.
     * @return the note stored in the cursor.
     */
    public static Note fromCursor(Cursor c) {
        Note note = new Note();

        note.setId(getLong(c, NotesContentContract.Notes._ID, -1));
        note.setNoteId(getString(c, NotesContentContract.Notes.NOTEID, ""));
        note.setTitle(getString(c, NotesContentContract.Notes.TITLE, ""));
        note.setContent(getString(c, NotesContentContract.Notes.CONTENT, ""));
        note.setCreated(new DateTime(getLong(c, NotesContentContract.Notes.CREATED, 0), DateTimeZone.UTC));
        note.setUpdated(new DateTime(getLong(c, NotesContentContract.Notes.UPDATED, 0), DateTimeZone.UTC));

        return note;
    }

    /**
     * Read a string from a key in the cursor
     *
     * @param c the cursor to read from
     * @param col the column key
     * @param defaultValue the default value if the column key does not exist in the cursor
     * @return the value of the key
     */
    private static String getString(Cursor c, String col, String defaultValue) {
        if (c.getColumnIndex(col) >= 0) {
            return c.getString(c.getColumnIndex(col));
        } else {
            return defaultValue;
        }
    }

    /**
     * Read a long value from a key in the cursor
     *
     * @param c the cursor to read from
     * @param col the column key
     * @param defaultValue the default value if the column key does not exist in the cursor
     * @return the value of the key
     */
    private static long getLong(Cursor c, String col, long defaultValue) {
        if (c.getColumnIndex(col) >= 0) {
            return c.getLong(c.getColumnIndex(col));
        } else {
            return defaultValue;
        }
    }

    /**
     * Create a new blank note
     */
    public Note() {
        setNoteId(UUID.randomUUID().toString());
        setTitle("");
        setContent("");
        setCreated(DateTime.now(DateTimeZone.UTC));
        setUpdated(DateTime.now(DateTimeZone.UTC));
    }

    /**
     * Returns the internal ID
     * @return the internal ID
     */
    public long getId() { return id; }

    /**
     * Sets the internal ID
     * @param id the new internal ID
     */
    public void setId(long id) { this.id = id;}

    /**
     * Returns the noteId
     * @return the note ID
     */
    public String getNoteId() { return noteId; }

    /**
     * Sets the noteId
     * @param noteId the new note ID
     */
    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }

    /**
     * Returns the title
     * @return the title
     */
    public String getTitle() { return title; }

    /**
     * Sets the title
     * @param title the new title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the note content
     * @return the note content
     */
    public String getContent() { return content; }

    /**
     * Sets the note content
     * @param content the note content
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Returns the created date
     * @return the created date
     */
    public DateTime getCreated() { return created; }

    /**
     * Sets the created date
     * @param created the created date
     */
    public void setCreated(DateTime created) {
        this.created = created;
    }

    /**
     * Returns the last updated date
     * @return the last updated date
     */
    public DateTime getUpdated() { return updated; }

    /**
     * Sets the last updated date
     * @param updatedDate the new last updated date
     */
    public void setUpdated(DateTime updatedDate) {
        this.updated = updatedDate;
    }

    /**
     * Updates the note
     * @param title the new title
     * @param content the new content
     */
    public void updateNote(String title, String content) {
        setTitle(title);
        setContent(content);
        setUpdated(DateTime.now(DateTimeZone.UTC));
    }

    /**
     * The string version of the class
     * @return the class unique descriptor
     */
    @Override
    public String toString() {
        return String.format("[note#%s] %s", noteId, title);
    }

    /**
     * Return the ContentValue object for this record.  This should not include the _id
     * field as it is stored for us.
     */
    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();

        values.put(NotesContentContract.Notes.NOTEID, noteId);
        values.put(NotesContentContract.Notes.TITLE, title);
        values.put(NotesContentContract.Notes.CONTENT, content);
        values.put(NotesContentContract.Notes.CREATED, created.toDateTime(DateTimeZone.UTC).getMillis());
        values.put(NotesContentContract.Notes.UPDATED, updated.toDateTime(DateTimeZone.UTC).getMillis());

        return values;
    }
}
