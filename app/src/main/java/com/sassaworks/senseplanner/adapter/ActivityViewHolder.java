package com.sassaworks.senseplanner.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.ImageView;
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
    @BindView(R.id.iv_more) ImageView mMoreImage;

    public interface OnMoreClickListener
    {
        void onMoreClick(ActivityRecord record, View v);
    }
    private OnMoreClickListener moreListener;


    public ActivityViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this,itemView);
    }

    @Override
    public void bindValues(CollectionItem record, Context context, ActivityViewHolder.OnMoreClickListener moreClickListener) {

        ActivityRecord activityRecord = (ActivityRecord)record;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(activityRecord.getTimestamp());
        String time = context.getString(R.string.time_format,calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE));

        //Setting ending date
        calendar.setTimeInMillis(activityRecord.getTimestampF());
        String timeF = context.getString(R.string.time_format,calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE));

        mDescription.setText(context.getString(R.string.description_format,activityRecord.getName(),
                time,timeF,activityRecord.getDesciption()));

        mCategoryText.setText(activityRecord.getCategory());
        Drawable moodDrawable = context.getResources().getDrawable(R.drawable.ic_baseline_sentiment_satisfied_24px);
        Drawable appealingDrawable = context.getResources().getDrawable(R.drawable.ic_baseline_sentiment_satisfied_24px);

        if (activityRecord.getMoodType().equals(context.getString(R.string.lb_bad))
                || activityRecord.getMoodType().equals(context.getString(R.string.lb_low)))
        {
            moodDrawable = context.getResources().getDrawable(R.drawable.ic_baseline_sentiment_very_dissatisfied_24px);
        }
        else if (activityRecord.getMoodType().equals(context.getString(R.string.lb_good)))
        {
            moodDrawable = context.getResources().getDrawable(R.drawable.ic_baseline_sentiment_very_satisfied_24px);
        }
        if (activityRecord.getJobAddiction().equals(context.getString(R.string.lb_bad))
                || activityRecord.getJobAddiction().equals(context.getString(R.string.lb_low)))
        {
            appealingDrawable = context.getResources().getDrawable(R.drawable.ic_baseline_sentiment_very_dissatisfied_24px);
        }
        else if (activityRecord.getJobAddiction().equals(context.getString(R.string.lb_high)))
        {
            appealingDrawable = context.getResources().getDrawable(R.drawable.ic_baseline_sentiment_very_satisfied_24px);
        }
//        GradientDrawable colorDrawable = (GradientDrawable)moodDrawable;
//        GradientDrawable colorAppealingDrawable = (GradientDrawable)appealingDrawable;

//        int resId = context.getResources().getIdentifier(activityRecord.getMoodType(),"color",context.getPackageName());
//        colorDrawable.setColor(context.getResources().getColor(resId));
        mMoodColor.setBackground(moodDrawable);

//        resId = context.getResources().getIdentifier(activityRecord.getJobAddiction(),"color",context.getPackageName());
//        colorAppealingDrawable.setColor(context.getResources().getColor(resId));
        mAppealingColor.setBackground(appealingDrawable);

        mMoreImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreClickListener.onMoreClick(activityRecord,v);
            }
        });

    }



}
