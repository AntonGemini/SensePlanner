package com.sassaworks.senseplanner;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sassaworks.senseplanner.adapter.SectionsPageAdapter;
import com.sassaworks.senseplanner.data.Activity;
import com.sassaworks.senseplanner.ui.ActivityTypeFragment;
import com.sassaworks.senseplanner.ui.CalendarFragment;
import com.sassaworks.senseplanner.ui.ChartFragment;

import static com.sassaworks.senseplanner.ui.ActivityTypeFragment.WELCOME_LABEL;

public class MainActivity extends AppCompatActivity
        implements ActivityTypeFragment.OnListFragmentInteractionListener,
        CalendarFragment.OnFragmentInteractionListener, ChartFragment.OnChartNameSelected,
        ActivityTypeFragment.OnLoadCompleted
{

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
  private AdView mAdBanner;
  FirebaseUser user;


  public static final String ACTIVITY_EXTRA = "activity";

  public static SectionsPageAdapter.nextFragmentListener chartListener;

  @Override
  public void onDataLoadCompleted() {
      //dialog2.dismiss();
  }

  @Override
  public void onDataLoadStarted() {

  }






  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    checkConnection(this);


    user = FirebaseAuth.getInstance().getCurrentUser();


    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    // Create the adapter that will return a fragment for each of the three
    // primary sections of the activity.
    String lastFrag = "";
    if (savedInstanceState!=null)
    {
      lastFrag = savedInstanceState.getString("SAVED_FRAGMENT");
    }
    mSectionsPagerAdapter = new SectionsPageAdapter(getSupportFragmentManager(), this, lastFrag);
    chartListener = mSectionsPagerAdapter.listener;

    // Set up the ViewPager with the sections adapter.
    mViewPager = findViewById(R.id.container);

    mViewPager.setAdapter(mSectionsPagerAdapter);
    tabNames = getResources().getStringArray(R.array.tabs_name);

    //Testing adId
    MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");

    mAdBanner = findViewById(R.id.adView);
    AdRequest adRequest = new AdRequest.Builder().build();
    mAdBanner.loadAd(adRequest);

    Context context = this;

    ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
      @Override
      public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

      }

      @Override
      public void onPageSelected(int position) {
        SharedPreferences.Editor editor
                = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean(WELCOME_LABEL,true).apply();
      }

      @Override
      public void onPageScrollStateChanged(int state) {

      }
    };

    mViewPager.addOnPageChangeListener(pageChangeListener);

//    dialog2 = new IOSDialog.Builder(MainActivity.this)
//            .setOnCancelListener(new DialogInterface.OnCancelListener() {
//              @Override
//              public void onCancel(DialogInterface dialog) {
//                //dialog1.show();
//              }
//            })
//            .setSpinnerColorRes(R.color.ios_gray_text)
//            .setMessageColorRes(R.color.primaryDark)
//            .setTitleColorRes(R.color.accent)
//            .setMessageContent("My message")
//            //.setCancelable(true)
//            .setSpinnerClockwise(false)
//            .setSpinnerDuration(120)
//            .setMessageContentGravity(Gravity.END)
//            //.setOneShot(false)
//            .build();
//    dialog2.show();
  }




  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    String menuSignItem = menu.getItem(1).getTitle().toString();
    menu.getItem(1).setTitle(menuSignItem + "(" + user.getDisplayName() + ")" );

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
      Intent intent = new Intent(this,EditCategoryActivity.class);
      startActivity(intent);
    }


    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onListFragmentInteraction(Activity item) {

    Intent intent = new Intent(this,CreateTaskActivity.class);
    intent.putExtra(ACTIVITY_EXTRA,item.getName());
    startActivity(intent);
  }


  @Override
  public void onFragmentInteraction(Uri uri) {

  }

  @Override
  public void onBackPressed() {
    finish();
  }

  @Override
  public void onSwipeListener(String category) {
    //String st = category;
  }


  public void checkConnection(Context context)
  {
    DatabaseReference connectionRef = FirebaseDatabase.getInstance().getReference(".info/connected");
    connectionRef.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        boolean connected = dataSnapshot.getValue(Boolean.class);
        if (connected)
        {
          Snackbar.make(findViewById(R.id.main_content), getString(R.string.connection_on),
                  Snackbar.LENGTH_SHORT)
                  .show();
        }
        else
        {
          Snackbar.make(findViewById(R.id.main_content), getString(R.string.connection_off),
                  Snackbar.LENGTH_SHORT)
                  .show();
        }
      }

      @Override
      public void onCancelled(@NonNull DatabaseError databaseError) {

      }
    });
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    String fragName = mSectionsPagerAdapter.getFragmentName();
    outState.putString("SAVED_FRAGMENT",fragName);
    Log.d("MainActivity7","saveInstance");
  }


  @Override
  public void onChartNameSelected(String chartName) {
    chartListener.fragment0Changed(chartName,getSupportFragmentManager());
  }
}