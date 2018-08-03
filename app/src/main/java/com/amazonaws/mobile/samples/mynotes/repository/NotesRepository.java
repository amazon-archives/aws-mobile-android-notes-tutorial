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
package com.amazonaws.mobile.samples.mynotes.repository;

import android.arch.lifecycle.LiveData;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;

import com.amazonaws.mobile.samples.mynotes.models.Note;
import com.amazonaws.mobile.samples.mynotes.models.ResultCallback;
import com.amazonaws.mobile.samples.mynotes.services.DataService;

public class NotesRepository {
    private DataService dataService;
    private LiveData<PagedList<Note>> pagedList;
    private LiveData<NotesDataSource> dataSource;

    public NotesRepository(DataService dataService) {
        this.dataService = dataService;
        NotesDataSourceFactory factory = new NotesDataSourceFactory(dataService);
        dataSource = factory.getCurrentDataSource();
        pagedList = new LivePagedListBuilder<>(factory, 20).build();
    }

    /**
     * An observable lifecycle-aware version of the paged list of notes.  This is used
     * to render a RecyclerView of all the notes.
     */
    public LiveData<PagedList<Note>> getPagedList() {
        return pagedList;
    }

    /**
     * API operation to save an item to the data store
     */
    public void save(Note note, ResultCallback<Note> callback) {
        dataSource.getValue().saveItem(note, callback);
    }

    /**
     * API operation to delete an item from the data store
     */
    public void delete(String noteId, ResultCallback<Boolean> callback) {
        dataSource.getValue().deleteItem(noteId, callback);
    }

    /**
     * API operation to get an item from the data store
     */
    public void get(String noteId, ResultCallback<Note> callback) {
        dataSource.getValue().getItem(noteId, callback);
    }
}
