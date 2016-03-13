package com.asus.wellness;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class IndependentMeasureActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start_measure_layout);
		ImageView image=(ImageView)findViewById(R.id.start_measure);
		image.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent=new Intent(IndependentMeasureActivity.this, MeasureActivity.class);
				startActivity(intent);
			}
			
		});
	}
	
}
