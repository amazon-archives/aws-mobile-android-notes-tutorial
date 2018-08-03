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
package com.amazonaws.mobile.samples.mynotes.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.paging.PagedList;

import com.amazonaws.mobile.samples.mynotes.Injection;
import com.amazonaws.mobile.samples.mynotes.models.Note;
import com.amazonaws.mobile.samples.mynotes.models.ResultCallback;
import com.amazonaws.mobile.samples.mynotes.repository.NotesRepository;

public class NoteListViewModel extends ViewModel {
    private NotesRepository notesRepository;

    public NoteListViewModel() {
        this.notesRepository = Injection.getNotesRepository();
    }

    public LiveData<PagedList<Note>> getNotesList() {
        return notesRepository.getPagedList();
    }

    public void removeNote(String noteId) {
        notesRepository.delete(noteId, new ResultCallback<Boolean>() {
            @Override
            public void onResult(Boolean result) {
                /* Do nothing */
            }
        });
    }
}
