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

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.amazonaws.mobile.samples.mynotes.R;

/**
 * A ViewHolder for the NotesAdapter; used for linking each row in the notes list
 * to a view for display
 */
public class NoteViewHolder extends RecyclerView.ViewHolder {
    /**
     * The containing view
     */
    private final View mView;

    /**
     * The ID field
     */
    private final TextView mIdView;

    /**
     * The Title field
     */
    private final TextView mTitleView;

    /**
     * The Note model that this view represents
     */
    private Note mItem;

    /**
     * initialize the viewholder with a specific view
     * @param view the view to link the data to.
     */
    public NoteViewHolder(View view) {
        super(view);
        mView = view;

        mIdView = (TextView) view.findViewById(R.id.list_id);
        mTitleView = (TextView) view.findViewById(R.id.list_title);
    }

    @Override
    public String toString() {
        return super.toString() + " '" + mIdView.getText() + "'";
    }

    /**
     * Obtain a reference to the containing view
     * @return a View object
     */
    public View getView() {
        return mView;
    }

    /**
     * Obtain a reference to the data for this view
     * @return a Note object
     */
    public Note getNote() {
        return mItem;
    }

    /**
     * Set the data for this view.  Also adjusts the UI of the containing view
     * @param note The data
     */
    public void setNote(Note note) {
        mItem = note;
        mIdView.setText(note.getNoteId());
        mTitleView.setText(note.getTitle());
    }
}
