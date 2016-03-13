package com.asus.wellness.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.asus.wellness.R;

/**
 * Created by smile_gao on 2015/7/29.
 */
public class ProfileArrayAdapter extends ArrayAdapter {
    private String[] items;
    public ProfileArrayAdapter(Context context, int resource, String[] objects) {
        super(context, resource, objects);
        items = objects;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = View.inflate(getContext(), R.layout.setup_profile_spinner_item,null);
        TextView tv =(TextView) view.findViewById(android.R.id.text1);
        tv.setText(items[position]);
        return  view;
    }
}
