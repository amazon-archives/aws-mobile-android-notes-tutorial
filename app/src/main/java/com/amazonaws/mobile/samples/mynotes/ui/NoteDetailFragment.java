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

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.amazonaws.mobile.samples.mynotes.NotesApp;
import com.amazonaws.mobile.samples.mynotes.R;
import com.amazonaws.mobile.samples.mynotes.databinding.NoteDetailBinding;
import com.amazonaws.mobile.samples.mynotes.models.Note;
import com.amazonaws.mobile.samples.mynotes.viewmodels.NoteDetailViewModel;

public class NoteDetailFragment extends Fragment {
    NoteDetailViewModel viewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = ViewModelProviders.of(this).get(NoteDetailViewModel.class);
        Bundle arguments = getArguments();
        if (arguments != null) {
            viewModel.setNoteId(arguments.getString(NotesApp.ITEM_ID));
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        NoteDetailBinding binding = DataBindingUtil.inflate(inflater, R.layout.note_detail, container, false);
        binding.setLifecycleOwner(this);
        binding.setVm(viewModel);

        // We need to be careful about doing a "two-way binding" in this case.  We don't want
        // that because it can cause cyclical API calls, which can cascade out of control.
        // Instead, we use one-way data binding and trap the changes the other way so that we
        // do the appropriate API calls
        viewModel.getTitle().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String title) {
                Note note = viewModel.getNote().getValue();
                note.setTitle(title);
                viewModel.saveNote(note);
            }
        });

        viewModel.getContent().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String content) {
                Note note = viewModel.getNote().getValue();
                note.setContent(content);
                viewModel.saveNote(note);
            }
        });

        return binding.getRoot();
    }
}
