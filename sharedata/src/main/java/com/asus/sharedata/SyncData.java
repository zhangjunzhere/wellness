package com.asus.sharedata;

/**
 * Created by smile_gao on 2015/8/10.
 */
public class SyncData {
    public static  final String Sync_Alldata_Path = "/sync_alldata_path";
    public static  final String Sync_Ecg_Path = "/sync_ecg_path";
    public static  final String Sync_Step_Path = "/sync_step_path";

    public static  final String Request_Sync_Data_Path ="/request_sync_data_path";

    public static  final String Request_Sync_Db_Path = "/request_sync_db_path";
    public static  final String Sync_Db_Path = "/sync_db_path";

    public static  final String Sync_Step_Data_Key = "/sync_step_data_key";
    public static  final String Sync_Ecg_Data_Key = "/sync_ecg_data_key";
    public static  final String Sync_Data_Key = "/data";

    public static final String Sync_Step_Tag_Key = "stepcount";
    public static final String Sync_Ecg_Tag_Key = "ecgdata";
    public static final String Sync_Coach_Tag_Key = "coachdata";
    public static final String Sync_Sleep_Tag_Key = "sleepdata";
    public static final String Sync_Sleep_New_Tag_Key = "sleepnewdata";

    public static final String Sync_Item_In_Sepration = "#";
    public static final String Sync_Item_Out_Sepration = "&";

    public static  final String Sync_Step_EndTime_Key = "sync_step_starttime_key";
    public static  final String Sync_Ecg_MeasureTime_Key = "sync_ecg_starttime_key";
    public static  final String Sync_Coach_EndTime_Key = "sync_coach_endtime_key";

    public static final String Sync_Today_Step_Key = "sync_today_step_key";

    //fix 638563
    public static final String STEP_COUNT_DEIVCE_NAME="device_name";
    //end smile

    public static final String STEP_COUNT_START_TIME="step_count_start_time";
    public static final String STEP_COUNT_END_TIME="step_count_end_time";
    public static final String STEP_COUNT_NUMBERS="step_count_numbers";
    public static final String ECG_MEASURE_TYPE="ecg_measure_type";
    public static final String ECG_MEASURE_TIME="ecg_measure_time";
    public static final String ECG_MEASURE_VALUE="ecg_measure_value";
    public static final String ECG_MEASURE_COMMENT="ecg_measure_comment";
    public static  final String PEER_CONNECT = "peer_connect";
    //sync db versioncode
    public static final String SYNC_DB_VERSION_CODE = "syncdb_versioncode";
}
