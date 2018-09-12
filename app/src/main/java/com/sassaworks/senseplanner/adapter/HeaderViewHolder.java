package com.sassaworks.senseplanner.adapter;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.sassaworks.senseplanner.R;
import com.sassaworks.senseplanner.data.CollectionItem;
import com.sassaworks.senseplanner.data.HeaderRecord;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HeaderViewHolder extends BaseViewHolder {

    @BindView(R.id.tv_activity_header) TextView mHeaderTextView;

    public HeaderViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void bindValues(CollectionItem record, Context context, ActivityViewHolder.OnMoreClickListener popupListener) {
        HeaderRecord headerRecord = (HeaderRecord)record;
        mHeaderTextView.setText(record.getFormattedDate());
    }
}
