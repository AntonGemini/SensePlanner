package com.sassaworks.senseplanner;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sassaworks.senseplanner.adapter.SectionsPageAdapter;
import com.sassaworks.senseplanner.data.Activity;
import com.sassaworks.senseplanner.ui.ActivityTypeFragment;
import com.sassaworks.senseplanner.ui.CalendarFragment;
import com.sassaworks.senseplanner.ui.ChartFragment;
import com.sassaworks.senseplanner.ui.CreateTaskFragment;
import com.sassaworks.senseplanner.ui.DailyChartFragment;
import com.sassaworks.senseplanner.ui.EventsFragment;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements ActivityTypeFragment.OnListFragmentInteractionListener,
        CalendarFragment.OnFragmentInteractionListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPageAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private String[] tabNames;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Toolbar toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPageAdapter(getSupportFragmentManager(),this);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        tabNames = getResources().getStringArray(R.array.tabs_name);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sign_out)
        {
            AuthUI.getInstance().signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                            startActivity(intent);
                        }
                    });
        }
        else if (id == R.id.action_edit_category)
        {

        }



        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListFragmentInteraction(Activity item) {

        Intent intent = new Intent(this,CreateTaskActivity.class);
        intent.putExtra("activity",item.getName());
        startActivity(intent);
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Save user id to shared preferences.
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onBackPressed() {
//        int fragmentsCount = getSupportFragmentManager().getBackStackEntryCount();
//        super.onBackPressed();
        finish();
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }



    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
//    public class SectionsPagerAdapter extends FragmentPagerAdapter {
//
//        private Fragment fragmentPos3;
//        private FragmentManager mFragmentManager;
//
//        public SectionsPagerAdapter(FragmentManager fm) {
//            super(fm);
//            mFragmentManager = fm;
//        }
//
//        @Override
//        public Fragment getItem(int position) {
//            // getItem is called to instantiate the fragment for the given page.
//            // Return a PlaceholderFragment (defined as a static inner class below).
//            switch (position) {
//                case 0:
//                    return ActivityTypeFragment.newInstance(position);
//                case 1:
//                    return EventsFragment.newInstance(null,null);
//                case 2:
//                    return CalendarFragment.newInstance();
//                case 3:
//                    if (fragmentPos3 == null)
//                    {
//                        fragmentPos3 = ChartFragment.newInstance(new ChartFragmentListener() {
//
//                            @Override
//                            public void onSwitchToDailyChartFragment() {
//                                mFragmentManager.beginTransaction()
//                                        .remove(fragmentPos3).commit();
//                                fragmentPos3 = DailyChartFragment.newInstance();
//                                notifyDataSetChanged();
//                            }
//                        });
//                    }
//                    return fragmentPos3;
//                default:
//                    return PlaceholderFragment.newInstance(position + 1);
//            }
//        }
//
//        @Override
//        public int getCount() {
//            // Show 4 total pages.
//            return 4;
//        }
//
//        @Nullable
//        @Override
//        public CharSequence getPageTitle(int position) {
//            return tabNames[position];
//        }
//
//        @Override
//        public int getItemPosition(@NonNull Object object) {
//
//            if (object instanceof ChartFragment && fragmentPos3 instanceof DailyChartFragment)
//            {
//                return POSITION_NONE;
//            }
//            return POSITION_UNCHANGED;
//        }
//    }
//
//    public interface ChartFragmentListener
//    {
//        void onSwitchToDailyChartFragment();
//    }
}
