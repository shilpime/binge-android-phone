package com.tatasky.binge.customviews;

import android.content.Context;
import android.util.AttributeSet;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tatasky.binge.utils.LoggerKt;

final public class RVLinearLayoutManager extends LinearLayoutManager {
    public RVLinearLayoutManager(Context context) {
        super(context);
    }

    public RVLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public RVLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        try {
            super.onLayoutChildren(recycler, state);
        } catch (IndexOutOfBoundsException e) {
            LoggerKt.e("RVLinearLayout","Exception is : "+e.getMessage());
        }
    }
}