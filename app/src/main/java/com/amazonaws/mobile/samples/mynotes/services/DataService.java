/*
Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.

Permission is hereby granted, free of charge, to any person obtaining a copy of this
software and associated documentation files (the "Software"), to deal in the Software
without restriction, including without limitation the rights to use, copy, modify,
merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.amazonaws.mobile.samples.mynotes.services;

import com.amazonaws.mobile.samples.mynotes.models.Note;
import com.amazonaws.mobile.samples.mynotes.models.PagedListConnectionResponse;
import com.amazonaws.mobile.samples.mynotes.models.ResultCallback;

/**
 * Definition of a data service.  This maps to an API definition on the cloud backend.
 * Each call should be async and run on a background thread.
 */
public interface DataService {
    /**
     * Load a single page of notes.
     *
     * @param limit the requested number of items
     * @param after the "next token" from a prior call
     * @param callback the response from the server
     */
    void loadNotes(int limit, String after, ResultCallback<PagedListConnectionResponse<Note>> callback);

    /**
     * Load a single note
     *
     * @param noteId the request ID
     * @param callback the response from the server
     */
    void getNote(String noteId, ResultCallback<Note> callback);

    /**
     * Save a note to the backing store
     *
     * @param note the note to be saved
     * @param callback the response from the server (null would indicate that the operation failed)
     */
    void saveNote(Note note, ResultCallback<Note> callback);

    /**
     * Delete a note from the backing store
     *
     * @param noteId the ID of the note to be deleted
     * @param callback the response from the server (Boolean = true indicates success)
     */
    void deleteNote(String noteId, ResultCallback<Boolean> callback);

}
