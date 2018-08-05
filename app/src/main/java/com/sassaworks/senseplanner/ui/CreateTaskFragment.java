package com.sassaworks.senseplanner.ui;


import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sassaworks.senseplanner.R;
import com.sassaworks.senseplanner.adapter.CategoriesAdapter;
import com.sassaworks.senseplanner.firebaseutils.FirebaseDatabaseHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CreateTaskFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateTaskFragment extends Fragment implements FirebaseDatabaseHelper.OnGetItem {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    FirebaseUser user;

    Spinner mCategorySpinner;
    Spinner mAppealingSpinner;
    Spinner mMoodSpinner;

    ArrayList<String> activities;
    ArrayList<String> appealing;
    ArrayList<String> mood;

    CategoriesAdapter mCategoryAdapter;
    CategoriesAdapter mAppealingAdapter;
    CategoriesAdapter mMoodAdapter;



    public CreateTaskFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CreateTaskFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CreateTaskFragment newInstance() {
        CreateTaskFragment fragment = new CreateTaskFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        Log.d("RSC2","instance");
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_task, container, false);
        mCategorySpinner = view.findViewById(R.id.sp_activities);
        mAppealingSpinner = view.findViewById(R.id.sp_appealing);
        mMoodSpinner = view.findViewById(R.id.sp_mood);

        activities = new ArrayList<>();
        appealing = new ArrayList<>();
        mood = new ArrayList<>();

        mCategoryAdapter = new CategoriesAdapter(getActivity(), activities);
        mAppealingAdapter = new CategoriesAdapter(getActivity(), appealing);
        mMoodAdapter = new CategoriesAdapter(getActivity(), mood);

        user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference refCategories = db.getReference("planner").child(user.getUid());

        FirebaseDatabaseHelper helperActivities = new FirebaseDatabaseHelper(refCategories,this, "activities");
        FirebaseDatabaseHelper helperAppealing = new FirebaseDatabaseHelper(refCategories,this, "appealing");
        FirebaseDatabaseHelper helperMood = new FirebaseDatabaseHelper(refCategories,this, "mood");
        //mCategorySpinner.setAdapter(adapter);

        mCategorySpinner.setAdapter(mCategoryAdapter);
        mAppealingSpinner.setAdapter(mAppealingAdapter);
        mMoodSpinner.setAdapter(mMoodAdapter);


        helperActivities.getCategoriesList();
        helperAppealing.getCategoriesList();
        helperMood.getCategoriesList();

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void getItem(String category, String type) {
        String st = category;

        Log.d("CATEG",String.valueOf(activities.size()));
        if (type=="activities") {
            activities.add(st);
            mCategoryAdapter.updateData(activities);
        }
        else if (type=="appealing") {
            appealing.add(st);
            mAppealingAdapter.updateData(appealing);
        }
        else if (type=="mood") {
            mood.add(st);
            mMoodAdapter.updateData(mood);
        }
    }
}
