package com.sassaworks.senseplanner.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

import com.sassaworks.senseplanner.MainActivity;
import com.sassaworks.senseplanner.R;
import com.sassaworks.senseplanner.ui.ActivityTypeFragment;
import com.sassaworks.senseplanner.ui.CalendarFragment;
import com.sassaworks.senseplanner.ui.ChartFragment;
import com.sassaworks.senseplanner.ui.DailyChartFragment;
import com.sassaworks.senseplanner.ui.EventsFragment;
import com.sassaworks.senseplanner.ui.HourChartFragment;

public class SectionsPageAdapter extends FragmentStatePagerAdapter {

  private String fragment0 = "";
  private Fragment fragAt0;
  private FragmentManager manager;
  private String[] tabNames;
  Context context;


  public SectionsPageAdapter(FragmentManager fm, Context ctx, String lastFrag) {
    super(fm);
    context = ctx;
    tabNames = ctx.getResources().getStringArray(R.array.tabs_name);
    this.manager = fm;
    this.listener = new fragmentChangeListener();
    fragment0 = lastFrag;
  }


  @Override
  public Fragment getItem(int position) {
    switch (position) {
      case 0:
        return ActivityTypeFragment.newInstance(0);
      case 1:
        return EventsFragment.newInstance();
      case 2:
        return CalendarFragment.newInstance();
      case 3:
        if(fragAt0 == null){

          switch(fragment0){
            case "Chart":
              fragAt0 = ChartFragment.newInstance();
              break;
            case "Hour":
              fragAt0 = HourChartFragment.newInstance();
              break;
            case "Daily":
              fragAt0 = DailyChartFragment.newInstance();
              break;
            default:
              fragAt0 = ChartFragment.newInstance();
          }
        }
        return fragAt0;
      default:
        return ActivityTypeFragment.newInstance(0);
    }
  }

  @Override
  public int getCount() {
    return 4;
  }

  @Nullable
  @Override
  public CharSequence getPageTitle(int position) {
    return tabNames[position];
  }

  @Override
  public int getItemPosition(@NonNull Object object) {
    if (object instanceof ChartFragment && fragAt0 instanceof DailyChartFragment
            || object instanceof HourChartFragment && fragAt0 instanceof DailyChartFragment)
    {
      return POSITION_NONE;
    }
    else if (object instanceof DailyChartFragment && fragAt0 instanceof ChartFragment
            || object instanceof HourChartFragment && fragAt0 instanceof ChartFragment){
      return POSITION_NONE;
    }
    else if (object instanceof HourChartFragment && fragAt0 instanceof ChartFragment)
    {
      return POSITION_NONE;
    }
    else if (object instanceof ChartFragment && fragAt0 instanceof HourChartFragment
            || object instanceof DailyChartFragment && fragAt0 instanceof HourChartFragment)
    {
      return POSITION_NONE;
    }

    return POSITION_NONE;
  }

  public interface nextFragmentListener {
    void fragment0Changed(String newFragmentIdentification, FragmentManager fragmentManager);
  }
  public nextFragmentListener listener;

  private final class fragmentChangeListener implements nextFragmentListener {


    @Override
    public void fragment0Changed(String fragment, FragmentManager fm) {
      fragment0 = fragment;

      if (fragAt0!=null)
      {
        manager.beginTransaction().remove(fragAt0).commitNow();
      }
      switch (fragment) {
        case "Chart":
          fragAt0 = ChartFragment.newInstance();
          break;
        case "Daily":
          fragAt0 = DailyChartFragment.newInstance();
          break;
        case "Hour":
          fragAt0 = HourChartFragment.newInstance();
          break;
      }
      SectionsPageAdapter.this.notifyDataSetChanged();
    }
  }

  public String getFragmentName()
  {
    return fragment0;
  }
}