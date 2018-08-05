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

import java.util.ArrayList;
import java.util.List;

public class CategoriesAdapter extends ArrayAdapter<String> {

    ArrayList<String> categories = new ArrayList<String>();
    Context context;


    public CategoriesAdapter(@NonNull Context context, ArrayList<String> data) {
        super(context, R.layout.item_category,data);
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
        tv.setText(categories.get(position));
        Log.d("Adap3",categories.get(position));
        return view;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.item_category,parent,false);
        TextView tv = view.findViewById(R.id.item_tv_category);
        tv.setText(categories.get(position));
        Log.d("Adap3",categories.get(position));
        return view;
    }

    @Override
    public int getCount() {
        return categories.size();
    }

    public void updateData(ArrayList<String> data)
    {
        this.categories = data;
        notifyDataSetChanged();
        Log.d("upd1",String.valueOf(data.size()));
    }
}
