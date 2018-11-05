package com.sassaworks.senseplanner;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sassaworks.senseplanner.data.Activity;
import com.sassaworks.senseplanner.ui.ActivityTypeFragment;
import com.sassaworks.senseplanner.ui.EditCategoryFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class EditCategoryActivity extends AppCompatActivity implements ActivityTypeFragment.OnListFragmentInteractionListener,
        ActivityTypeFragment.OnAddClickListener{

    public static final String ONBOARDING_SCREEN = "onboarding_shown";

    @BindView(R.id.buttonGotIt) Button startButton;
    @BindView(R.id.onboardingLayout) ConstraintLayout onboardingLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_category);
        ButterKnife.bind(this);

        getSupportActionBar().setTitle(getString(R.string.edit_cat_label));
        SharedPreferences sharedPreferences =
                getDefaultSharedPreferences(this);
        if (!sharedPreferences.getBoolean(ONBOARDING_SCREEN,false))
        {
            onboardingLayout.setVisibility(View.VISIBLE);
        }

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

    @OnClick(R.id.buttonGotIt)
    public void closeOnboard()
    {
        onboardingLayout.setVisibility(View.GONE);
    }




    @Override
    public void onSwipeListener(String category) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference dbRefDel = db.getReference(getString(R.string.ref_planner)).child(user.getUid()).child(getString(R.string.ref_activities))

                .child(category.toLowerCase().replaceAll("\\s",""));

        db.getReference(getString(R.string.ref_planner)).child(user.getUid()).child(getString(R.string.ref_activities))
                .child(category.toLowerCase().replaceAll("\\s","")).removeValue();

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

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences.Editor editor =
                PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putBoolean(ONBOARDING_SCREEN,true).apply();
    }
}
