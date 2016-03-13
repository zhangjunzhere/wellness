package com.asus.wellness.ui;

import android.content.Context;
import android.test.mock.MockApplication;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.asus.wellness.R;
import com.asus.wellness.WApplication;
import com.asus.wellness.dbhelper.Device;
import com.asus.wellness.dbhelper.DeviceDao;
import com.asus.wellness.utils.DeviceHelper;

import java.util.List;

/**
 * Created by jz on 2015/7/8.
 */
public class DeviceSpinnerAdapter extends BaseAdapter {

    private List<Device> devices;
    private DeviceDao deviceDao ;
    private Context mContext;
    public DeviceSpinnerAdapter(Context context){
        mContext = context;
        deviceDao = WApplication.getInstance().getDataHelper().getDaoSession().getDeviceDao();
        devices = DeviceHelper.getConnectedDevices();
    }

    @Override
    public void notifyDataSetChanged(){
        devices = DeviceHelper.getConnectedDevices();
        Log.i("SpinnerAdapter","notifyDataSetChanged devcie size: "+devices.size());
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public Device getItem(int position) {
        return devices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getDropDownView(int position, View cnvtView, ViewGroup prnt) {
        View view =  getView(position, cnvtView, prnt);
        ((TextView)view).setTextSize(17);
        ((TextView)view).setPadding(15,15,15,15);
        return view;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.simple_spinner_item,parent,false);
        }
        Device device = getItem(position);
        ((TextView)convertView).setText(device.getName());
        return convertView;
    }
}
