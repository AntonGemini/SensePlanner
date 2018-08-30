package com.sassaworks.senseplanner.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sassaworks.senseplanner.R;
import com.sassaworks.senseplanner.data.CollectionItem;

import java.util.ArrayList;
import java.util.zip.Inflater;

public class ActivityRecordAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    Context context;
    ArrayList<CollectionItem> records;
    ActivityViewHolder.OnMoreClickListener moreClickListener;

    public ActivityRecordAdapter(Context context, ArrayList<CollectionItem> records, ActivityViewHolder.OnMoreClickListener listener)
    {
        this.context = context;
        this.records = records;
        this.moreClickListener = listener;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType)
        {
            case CollectionItem.TYPE_DATE:
                view = LayoutInflater.from(context).inflate(R.layout.item_header,parent,false);
                return new HeaderViewHolder(view);
            case CollectionItem.TYPE_DESC:
                view = LayoutInflater.from(context).inflate(R.layout.item_record,parent,false);
                return new ActivityViewHolder(view);
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {

        return records.get(position).getItemType();
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        //bind data of list(position)
        holder.bindValues(records.get(position),context, moreClickListener);
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

}
