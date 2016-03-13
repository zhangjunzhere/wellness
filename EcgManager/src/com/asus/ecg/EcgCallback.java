package com.asus.ecg;

public interface EcgCallback {
	/**
	 * User contacts ECG sensor
	 */
	public void onTouchSensor();
	/**
	 * User detaches from ECG sensor
	 */
	public void onDetachSensor();
	/**
	 * When data appears, such as heart rate(TGDevice.MSG_ECG_HEARTRATE) or mood(TGDevice.MSG_ECG_MOOD), this callback will be triggered.
	 * @param type Two data types, TGDevice.MSG_ECG_HEARTRATE and TGDevice.MSG_ECG_MOOD
	 * @param value Measured value of TGDevice.MSG_ECG_HEARTRATE and TGDevice.MSG_ECG_MOOD at that time.
	 */
	public void onMeasureing(int type, int value);
	public void onGetHeartRate(int value);
	public void onGetEnergy(int value);
	public void onNoUpdateTimeout();
	public void onNoFisrtRRTimeout();
}
