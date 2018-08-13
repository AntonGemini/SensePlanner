package com.sassaworks.senseplanner.adapter;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.sassaworks.senseplanner.R;
import com.sassaworks.senseplanner.data.ActivityRecord;
import com.sassaworks.senseplanner.data.CollectionItem;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ActivityViewHolder extends BaseViewHolder {

    @BindView(R.id.tv_full_description) TextView mDescription;
    @BindView(R.id.tv_category) TextView mCategoryText;
    @BindView(R.id.tv_mood) TextView mMoodText;
    @BindView(R.id.tv_job_appealing) TextView mAppealingText;
    @BindView(R.id.mood_color) View mMoodColor;
    @BindView(R.id.job_appealing_color) View mAppealingColor;


    public ActivityViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this,itemView);
    }

    @Override
    public void bindValues(CollectionItem record, Context context) {
        ActivityRecord activityRecord = (ActivityRecord)record;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(activityRecord.getTimestamp());
        String time = calendar.get(Calendar.HOUR_OF_DAY) + " : " + calendar.get(Calendar.MINUTE);
        mDescription.setText(context.getString(R.string.description_format,activityRecord.getName(),
                time,"",activityRecord.getDesciption()));
    }


}
