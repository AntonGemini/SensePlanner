package com.sassaworks.senseplanner.ui;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.sassaworks.senseplanner.R;

public class CategoryViewHolder extends RecyclerView.ViewHolder {

    public TextView mCategoryName;
    public CategoryViewHolder(View itemView) {
        super(itemView);
        mCategoryName = itemView.findViewById(R.id.item_tv_category);
    }

}
