package com.sassaworks.senseplanner.ui;


import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.sassaworks.senseplanner.R;
import com.sassaworks.senseplanner.data.Activity;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EditCategoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditCategoryFragment extends DialogFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    @BindView(R.id.editText) EditText mCategory;
    @BindView(R.id.button2) Button mButtonSave;


    public EditCategoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment EditCategoryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EditCategoryFragment newInstance() {
        EditCategoryFragment fragment = new EditCategoryFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_category, container, false);
        ButterKnife.bind(this,view);
        mButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(mCategory.getText().toString())) {
                    FirebaseDatabase db = FirebaseDatabase.getInstance();
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    Activity activity = new Activity();
                    activity.setName(mCategory.getText().toString());
                    db.getReference(getString(R.string.ref_planner)).child(user.getUid()).child(getString(R.string.ref_activities))
                            .child(activity.getName().toLowerCase()).setValue(activity);
                    dismiss();
                }

            }
        });
        return view;
    }

}
