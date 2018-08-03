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
package com.amazonaws.mobile.samples.mynotes.services.mock;

import com.amazonaws.mobile.samples.mynotes.models.Note;
import com.amazonaws.mobile.samples.mynotes.models.PagedListConnectionResponse;
import com.amazonaws.mobile.samples.mynotes.models.ResultCallback;
import com.amazonaws.mobile.samples.mynotes.services.DataService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * A mock data store.  This will create 30 notes so you can see the scrolling action, but
 * otherwise acts as a data service.  This should be easily rewritten to use an actual cloud API
 */
public class MockDataService implements DataService {
    private ArrayList<Note> items;

    public MockDataService() {
        items = new ArrayList<>();
        for (int i = 0 ; i < 30 ; i++) {
            Note item = new Note();
            item.setTitle(String.format(Locale.US, "Note %d", i));
            item.setContent(String.format(Locale.US, "Content for note %d", i));
            items.add(item);
        }
    }

    /**
     * Simulate an API call to a network service that returns paged data.
     *
     * @param limit the requested number of items
     * @param after the "next token" from a prior call
     * @param callback the response from the server
     */
    @Override
    public void loadNotes(int limit, String after, ResultCallback<PagedListConnectionResponse<Note>> callback) {
        if (limit < 1 || limit > 100) throw new IllegalArgumentException("Limit must be between 1 and 100");

        int firstItem = 0;
        if (after != null) {
            firstItem = indexOfFirst(after);
            if (firstItem < 0) {
                callback.onResult(new PagedListConnectionResponse<>(Collections.<Note>emptyList(), null));
                return;
            }
            firstItem++;
        }
        if (firstItem > items.size() - 1) {
            callback.onResult(new PagedListConnectionResponse<>(Collections.<Note>emptyList(), null));
            return;
        }
        int nItems = Math.min(limit, items.size() - firstItem);
        if (nItems == 0) {
            callback.onResult(new PagedListConnectionResponse<>(Collections.<Note>emptyList(), null));
            return;
        }

        List<Note> sublist = new ArrayList<>(items.subList(firstItem, firstItem + nItems));
        String nextToken = (firstItem + nItems - 1 == items.size()) ? null : sublist.get(sublist.size() - 1).getNoteId();
        callback.onResult(new PagedListConnectionResponse<>(sublist, nextToken));
    }

    /**
     * Load a single note from the current list of notes
     *
     * @param noteId the request ID
     * @param callback the response from the server
     */
    @Override
    public void getNote(String noteId, ResultCallback<Note> callback) {
        if (noteId == null || noteId.isEmpty()) throw new IllegalArgumentException();

        int idx = indexOfFirst(noteId);
        callback.onResult(idx >= 0 ? items.get(idx) : null);
    }

    /**
     * Save a note to the backing store
     *
     * @param note the note to be saved
     * @param callback the response from the server (null would indicate that the operation failed)
     */
    @Override
    public void saveNote(Note note, ResultCallback<Note> callback) {
        if (note == null || note.getNoteId().isEmpty()) throw new IllegalArgumentException();

        int idx = indexOfFirst(note.getNoteId());
        if (idx >= 0)
            items.set(idx, note);
        else
            items.add(note);
        callback.onResult(note);
    }

    /**
     * Delete a note from the backing store
     *
     * @param noteId the ID of the note to be deleted
     * @param callback the response from the server (Boolean = true indicates success)
     */
    @Override
    public void deleteNote(String noteId, ResultCallback<Boolean> callback) {
        if (noteId == null || noteId.isEmpty()) throw new IllegalArgumentException();

        int idx = indexOfFirst(noteId);
        if (idx >= 0) items.remove(idx);
        callback.onResult(idx >= 0);
    }

    /**
     * Returns the index of the first note that matches
     * @param noteId the note to match
     * @return the index of the note, or -1 if not found
     */
    private int indexOfFirst(String noteId) {
        if (items.isEmpty()) throw new IndexOutOfBoundsException();
        for (int i = 0 ; i < items.size() ; i++) {
            if (items.get(i).getNoteId().equals(noteId))
                return i;
        }
        return -1;
    }
}
