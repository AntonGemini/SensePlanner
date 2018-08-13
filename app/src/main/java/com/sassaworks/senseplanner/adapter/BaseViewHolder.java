package com.sassaworks.senseplanner.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.sassaworks.senseplanner.data.CollectionItem;

public abstract class BaseViewHolder extends RecyclerView.ViewHolder {
    public BaseViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void bindValues(CollectionItem record, Context context);

}
