package com.asus.wellness.detectactivity;

import android.location.Location;

public interface DetectActivityCallback {
	public void onStartDriving(long time);
	public void onEndDriving(long time);
	public void onStartBicycle(long time);
	public void onEndBicycle(long time);
	public void onStartWalk(long time);
	public void onEndWalk(long time);
	public void onActivityTypeReceive(String name, int nowStage);
	public void onStartMoving(long time);
	public void onGetLastLocation(Location location);
}
