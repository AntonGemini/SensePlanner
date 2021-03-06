package com.sassaworks.senseplanner.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sassaworks.senseplanner.R;

public class CategoriesAdapter extends ArrayAdapter<String> {

  String[] categories;
  Context context;


  public CategoriesAdapter(@NonNull Context context, String[] data) {
    super(context, R.layout.item_category);
    this.categories = data;
    this.context = context;
    Log.d("Adap1","a1");
  }


  @NonNull
  @Override
  public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View view = inflater.inflate(R.layout.item_category,parent,false);
    TextView tv = view.findViewById(R.id.item_tv_category);
    tv.setText(categories[position]);
    Log.d("Adap3",categories[position]);
    return view;
  }

  @Override
  public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View view = inflater.inflate(R.layout.item_dropdown_category,parent,false);
    TextView tv = view.findViewById(R.id.item_tv_category);
    tv.setText(categories[position]);
    Log.d("Adap3",categories[position]);
    return view;
  }

  @Override
  public int getCount() {
    return categories.length;
  }


  @Override
  public int getPosition(@Nullable String item) {
    int position = -1;
    for (int s=0; s< categories.length; s++)
    {
      if (categories[s].equals(item))
      {
        return s;
      }
    }
    return position;
  }

  public void updateData(String[] data)
  {
    this.categories = data;
    notifyDataSetChanged();
  }
}