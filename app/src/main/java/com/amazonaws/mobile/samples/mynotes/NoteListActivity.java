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
package com.amazonaws.mobile.samples.mynotes;

import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amazonaws.mobile.samples.mynotes.data.Note;
import com.amazonaws.mobile.samples.mynotes.data.NoteViewHolder;
import com.amazonaws.mobile.samples.mynotes.data.NotesContentContract;

/**
 * An activity representing a list of Notes. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link NoteDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class NoteListActivity
        extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>
{
    /**
     * The unique identifier for the loader
     */
    private static final int NOTES_LOADER = 10;

    /**
     * The listview
     */
    RecyclerView notesList;

    /**
     * The Add New Note button
     */
    FloatingActionButton addNoteButton;

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    /**
     * Activity lifecycle event handler - called when the activity is first created.
     * @param savedInstanceState the saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);

        // Install the application crash handler.  This is only done on the first activity.
        ApplicationCrashHandler.installHandler();

        // Work out if we are in 2-pane (tablet) mode or not
        mTwoPane = (findViewById(R.id.note_detail_container) != null);

        // Initialize the Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        // Initialize the floating action button
        addNoteButton = (FloatingActionButton) findViewById(R.id.addNoteButton);
        addNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTwoPane) {
                    NoteDetailFragment fragment = new NoteDetailFragment();
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.note_detail_container, fragment)
                            .commit();
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, NoteDetailActivity.class);
                    context.startActivity(intent);
                }
            }
        });

        // Update the ListView with the CursorAdapter
        notesList = (RecyclerView) findViewById(R.id.note_list);
        NotesAdapter adapter = new NotesAdapter(this, null);
        notesList.setAdapter(adapter);

        // Initialize the swipe-to-delete handler
        ItemTouchHelper.SimpleCallback swipeHandler = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            Drawable background, xMark;
            int xMarkMargin;
            boolean initialized;

            private void initialize() {
                background = new ColorDrawable(Color.RED);
                xMark = ContextCompat.getDrawable(NoteListActivity.this, R.drawable.ic_clear_24dp);
                xMark.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                xMarkMargin = (int) NoteListActivity.this.getResources().getDimension(R.dimen.ic_clear_margin);
                initialized = true;
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                final NoteViewHolder noteHolder = (NoteViewHolder) viewHolder;
                ((NotesAdapter) notesList.getAdapter()).remove(noteHolder);
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                // If the item has already been swiped away, ignore
                if (viewHolder.getAdapterPosition() == -1) return;

                // If not initialized, then do so
                if (!initialized) initialize();

                int vr = viewHolder.itemView.getRight();
                int vt = viewHolder.itemView.getTop();
                int vb = viewHolder.itemView.getBottom();
                int vh = vb - vt;
                int iw = xMark.getIntrinsicWidth();
                int ih = xMark.getIntrinsicWidth();

                background.setBounds(vr + (int)dX, vt, vr, vb);
                background.draw(c);

                int xMarkLeft = vr - xMarkMargin - iw;
                int xMarkRight = vr - xMarkMargin;
                int xMarkTop = vt + (vh - ih)/2;
                int xMarkBottom = xMarkTop + ih;
                xMark.setBounds(xMarkLeft, xMarkTop, xMarkRight, xMarkBottom);
                xMark.draw(c);

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeHandler);
        itemTouchHelper.attachToRecyclerView(notesList);

        // Kick off the data loader for the RecyclerView
        getLoaderManager().initLoader(NOTES_LOADER, null, this);
    }

    /**
     * Event handler callback for the loader manager.  Called when creating the loader
     * manager.
     *
     * @param id The ID - should always be NOTES_LOADER in this edition
     * @param args any arguments - should always be null in this edition
     * @return the loader
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                NotesContentContract.Notes.CONTENT_URI,
                NotesContentContract.Notes.PROJECTION_ALL,
                null,
                null,
                NotesContentContract.Notes.SORT_ORDER_DEFAULT);
    }

    /**
     * Event handler callback for the loader manager.  Called when data has finished loading.
     * @param loader the loader that finished loading
     * @param data a cursor to the data that was loaded
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        ((NotesAdapter) notesList.getAdapter()).swapCursor(data);
    }

    /**
     * Event handler callback for the loader manager.  Called when the loader is reset.
     * @param loader the loader that was reset
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        ((NotesAdapter) notesList.getAdapter()).swapCursor(null);
    }

    /**
     * The NotesAdapter is a data provider for linking the notes content provider to the UI.
     */
    private class NotesAdapter extends RecyclerView.Adapter<NoteViewHolder> {
        Cursor dataCursor;
        Context context;

        NotesAdapter(Context mContext, Cursor cursor) {
            dataCursor = cursor;
            context = mContext;
        }

        @Override
        public NoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.note_list_content, parent, false);
            return new NoteViewHolder(view);
        }

        /**
         * The main part of the NotesAdapter - this is called once for each element in the
         * list of data that is returned.
         * @param holder the ViewHolder (which is a NoteViewHolder) for the record
         * @param position the position in the list.
         */
        @Override
        public void onBindViewHolder(final NoteViewHolder holder, int position) {
            dataCursor.moveToPosition(position);
            Note note = Note.fromCursor(dataCursor);
            holder.setNote(note);

            // Install a click-handler for clicking on the row
            holder.getView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle arguments = new Bundle();
                    arguments.putString(NoteDetailFragment.ARG_ITEM_ID, holder.getNote().getNoteId());
                    if (mTwoPane) {
                        NoteDetailFragment fragment = new NoteDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.note_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, NoteDetailActivity.class);
                        intent.putExtras(arguments);
                        context.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return (dataCursor == null) ? 0 : dataCursor.getCount();
        }

        /**
         * Used to support the loader framework for loading data
         * @param cursor the new cursor
         * @return the old cursor
         */
        Cursor swapCursor(Cursor cursor) {
            if (dataCursor == cursor) {
                return null;
            }
            Cursor oldCursor = dataCursor;
            this.dataCursor = cursor;
            if (cursor != null) {
                this.notifyDataSetChanged();
            }
            return oldCursor;
        }

        /**
         * Remove the element in the list.
         * @param holder the viewholder to delete
         */
        void remove(final NoteViewHolder holder) {
            if (mTwoPane) {
                // Check to see if the current fragment is the record we are deleting
                Fragment currentFragment = NoteListActivity.this
                        .getSupportFragmentManager()
                        .findFragmentById(R.id.note_detail_container);
                if (currentFragment instanceof NoteDetailFragment) {
                    String deletedNote = holder.getNote().getNoteId();
                    String displayedNote = ((NoteDetailFragment) currentFragment).getNote().getNoteId();
                    if (deletedNote.equals(displayedNote)) {
                        getSupportFragmentManager().beginTransaction().remove(currentFragment).commit();
                    }
                }
            }

            // Remove the item from the database
            ContentResolver resolver = getContentResolver();
            int position = holder.getAdapterPosition();
            Uri itemUri = NotesContentContract.Notes.uriBuilder(holder.getNote().getNoteId());
            int count = resolver.delete(itemUri, null, null);
            if (count > 0) {
                notifyItemRemoved(position);
            }
        }
    }
}
