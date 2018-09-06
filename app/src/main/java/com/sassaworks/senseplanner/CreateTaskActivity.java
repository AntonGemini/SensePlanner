package com.sassaworks.senseplanner;


import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.sassaworks.senseplanner.R;
import com.sassaworks.senseplanner.data.ActivityRecord;
import com.sassaworks.senseplanner.ui.CreateTaskFragment;

public class CreateTaskActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);

        if (savedInstanceState == null) {
            ActivityRecord record = getIntent().getExtras().getParcelable(CreateTaskFragment.ACTIVITY_RECORD);
            CreateTaskFragment taskFragment;
            String activity = getIntent().getExtras().getString("activity");
            int startingDay;

            if (record != null) {
                taskFragment = CreateTaskFragment.newInstance(record);
            } else if (activity != null) {
                taskFragment = CreateTaskFragment.newInstance(activity);
            }
            else if(getIntent().getExtras().containsKey(CreateTaskFragment.STARTING_DAY))
            {
                startingDay = getIntent().getExtras().getInt(CreateTaskFragment.STARTING_DAY);
                taskFragment = CreateTaskFragment.newInstance(startingDay);
            }
            else
            {
                taskFragment = CreateTaskFragment.newInstance("");
            }

            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction().replace(R.id.frameTask, taskFragment).commit();
        }
    }
}
