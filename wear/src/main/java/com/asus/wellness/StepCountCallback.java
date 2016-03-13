package com.asus.wellness;

public interface StepCountCallback {
	public void onWalkStart(long time);
	public void onWalkEnd(int stepNumbers, long time);
	public void onWalking(float step);
	//public void restoreLastMissingSteps(float steps, long startTimeMilli, long endTimeMilli);
}
