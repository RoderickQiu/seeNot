package com.scrisstudio.seenot.ui.rule;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.scrisstudio.seenot.MainActivity;

import java.util.Objects;

public class RuleInfoCardDecoration extends RecyclerView.ItemDecoration {

    @Override
    public void getItemOffsets(Rect outRect, @NonNull View view, RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);

        outRect.top = position == 0 ? MainActivity.dip2px(16) : MainActivity.dip2px(8);
        outRect.bottom = position == (Objects.requireNonNull(parent.getAdapter()).getItemCount() - 1) ? MainActivity.dip2px(88) : MainActivity.dip2px(8);
    }

}
