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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import com.amazonaws.mobile.samples.mynotes.R;
import com.amazonaws.mobile.samples.mynotes.models.RemoveCallback;

public class SwipeToDelete extends ItemTouchHelper.SimpleCallback {
    private RemoveCallback callback;
    private Drawable background, xMark;
    private int xMarkMargin;

    SwipeToDelete(Context context, RemoveCallback callback) {
        super(0, ItemTouchHelper.LEFT);

        this.callback = callback;
        this.background = new ColorDrawable(Color.RED);
        this.xMark = ContextCompat.getDrawable(context, R.drawable.ic_clear_24dp);
        this.xMark.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        this.xMarkMargin = (int) context.getResources().getDimension(R.dimen.ic_clear_margin);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        callback.onRemove(((NoteListViewHolder) viewHolder).getNote());
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (viewHolder != null) {
            // If the item has already been swiped away,ignore it
            if (viewHolder.getAdapterPosition() == -1) return;

            int vr = viewHolder.itemView.getRight();
            int vt = viewHolder.itemView.getTop();
            int vb = viewHolder.itemView.getBottom();
            int vh = vb - vt;
            int iw = xMark.getIntrinsicWidth();
            int ih = xMark.getIntrinsicHeight();
            background.setBounds(vr + (int)dX, vt, vr, vb);
            background.draw(c);

            int xml = vr - xMarkMargin - iw;
            int xmr = vr - xMarkMargin;
            int xmt = vt + (vh - ih) / 2;
            int xmb = xmt + ih;
            xMark.setBounds(xml, xmt, xmr, xmb);
            xMark.draw(c);
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
}
