package com.sassaworks.senseplanner.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sassaworks.senseplanner.FirebaseIdlingResource;
import com.sassaworks.senseplanner.R;
import com.sassaworks.senseplanner.adapter.MyActivityTypeRecyclerViewAdapter;
import com.sassaworks.senseplanner.data.Activity;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class ActivityTypeFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String OPEN_MODE = "open_mode";

    private static final int READ_MODE = 0;
    private static final int EDIT_MODE = 1;

    // TODO: Customize parameters
    private int mOpenMode = -1;
    private OnListFragmentInteractionListener mListener;
    private OnAddClickListener mClickListener;

    private ArrayList<Activity> activities;
    private MyActivityTypeRecyclerViewAdapter adapter;

    FirebaseUser user;

    @BindView(R.id.list) RecyclerView recyclerView;
    @BindView(R.id.fabAddCategory) FloatingActionButton mAddButton;
    @BindView(R.id.tv_welcome_text) TextView mIntroText;




    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ActivityTypeFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static ActivityTypeFragment newInstance(int openMode) {
        ActivityTypeFragment fragment = new ActivityTypeFragment();
        Bundle args = new Bundle();
        args.putInt(OPEN_MODE, openMode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mOpenMode = getArguments().getInt(OPEN_MODE);
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activitytype_list, container, false);
        ButterKnife.bind(this,view);


        user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase db = FirebaseDatabase.getInstance();

        FirebaseIdlingResource.increment();
        DatabaseReference dbRef = db.getReference(getString(R.string.ref_planner)).child(user.getUid())
                .child(getString(R.string.ref_activities));
        activities = new ArrayList<>();

        if (mOpenMode == READ_MODE)
        {
            mAddButton.setVisibility(View.GONE);
            mIntroText.setVisibility(View.VISIBLE);
        }
        else
        {
            mAddButton.setVisibility(View.VISIBLE);
            mIntroText.setVisibility(View.GONE);
        }

        Context context = view.getContext();
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        adapter = new MyActivityTypeRecyclerViewAdapter(activities, mListener);
        recyclerView.setAdapter(adapter);
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                TextView tv = viewHolder.itemView.findViewById(R.id.content);
                String st = tv.getText().toString();
                if (mOpenMode == EDIT_MODE)
                {
                    mListener.onSwipeListener(st);
                }
            }
        }).attachToRecyclerView(recyclerView);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                ((LinearLayoutManager)recyclerView.getLayoutManager()).getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);


        ChildEventListener childEventListener = new ChildEventListener() {

            boolean mIsIdleSet = false;
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Activity activity = dataSnapshot.getValue(Activity.class);
                activities.add(activity);
                adapter.updateViewData(activities);
                if (!mIsIdleSet)
                {
                    FirebaseIdlingResource.decrement();
                    mIsIdleSet = true;
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Activity activity = dataSnapshot.getValue(Activity.class);
                for (int i=0; i<activities.size(); i++)
                {
                    if (activities.get(i).getName().equals(activity.getName()))
                    {
                        activities.remove(i);
                        break;
                    }
                }
                adapter.updateViewData(activities);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (!mIsIdleSet) {
                    FirebaseIdlingResource.decrement();
                }
            }
        };
        dbRef.addChildEventListener(childEventListener);

        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClickListener.onAddClickListener();
            }
        });

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
        if (context instanceof OnAddClickListener)
        {
            mClickListener = (OnAddClickListener) context;
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(Activity item);
        void onSwipeListener(String category);
    }

    public interface OnAddClickListener {
        void onAddClickListener();
    }




}
