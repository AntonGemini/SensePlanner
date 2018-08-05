package com.sassaworks.senseplanner;


import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.sassaworks.senseplanner.R;
import com.sassaworks.senseplanner.ui.CreateTaskFragment;

public class CreateTaskActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);

        CreateTaskFragment taskFragment = CreateTaskFragment.newInstance();
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().add(R.id.frameTask,taskFragment).commit();
    }
}
