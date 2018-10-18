package com.sassaworks.senseplanner;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.sassaworks.senseplanner.data.Activity;
import com.sassaworks.senseplanner.ui.ActivityTypeFragment;
import com.sassaworks.senseplanner.ui.EditCategoryFragment;

public class EditCategoryActivity extends AppCompatActivity implements ActivityTypeFragment.OnListFragmentInteractionListener,
        ActivityTypeFragment.OnAddClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_category);
        getSupportActionBar().setTitle(getString(R.string.edit_cat_label));
        if (savedInstanceState == null)
        {
            ActivityTypeFragment fragment = ActivityTypeFragment.newInstance(1);
            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction().add(R.id.frameEditCategory,fragment).commit();
        }
    }

    @Override
    public void onListFragmentInteraction(Activity item) {
    }

    @Override
    public void onSwipeListener(String category) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        db.getReference(getString(R.string.ref_planner)).child(user.getUid()).child(getString(R.string.ref_activities))
                .child(category.toLowerCase()).removeValue();
    }

    @Override
    public void onAddClickListener() {
        showDialog();
    }

    private void showDialog()
    {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        DialogFragment dialogFragment = EditCategoryFragment.newInstance();
        dialogFragment.show(ft,"dialog");
    }


}
