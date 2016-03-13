package com.asus.wellness.fragment;

import com.asus.wellness.MeasureActivity;
import com.asus.wellness.R;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

public class StartMeasureFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view=inflater.inflate(R.layout.start_measure_layout, null);
		ImageView image=(ImageView)view.findViewById(R.id.start_measure);
		image.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent=new Intent(getActivity(), MeasureActivity.class);
				startActivity(intent);
			}
			
		});
		return view;
	}

}
