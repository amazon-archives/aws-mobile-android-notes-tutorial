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
package com.amazonaws.mobile.samples.mynotes.ui;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.amazonaws.mobile.samples.mynotes.R;
import com.amazonaws.mobile.samples.mynotes.models.Note;

public class NoteListViewHolder extends RecyclerView.ViewHolder {
    private TextView titleField;
    private TextView idField;
    private Note note;

    public NoteListViewHolder(View view) {
        super(view);

        titleField = view.findViewById(R.id.list_title);
        idField = view.findViewById(R.id.list_id);
    }

    public Note getNote() {
        return note;
    }

    public void setNote(Note note) {
        this.note = note;
        String title = note.getTitle();
        titleField.setText(title == null ? "null" : title);
        idField.setText(note.getNoteId());
    }
}
