package com.asus.wellness.utils;

import com.google.gson.Gson;

/**
 * Created by smile_gao on 2015/7/1.
 */
public class EBCommand {
    public static final String COMMAND_REGISTER_COACH_SENSOR = "register_coach_sensor";
    public static final String COMMAND_UNREGISTER_COACH_SENSOR = "unregister_coach_sensor";
    public static final String COMMAND_COACH_STATE_CHANGED = "coach_state_changed";
    public static final String COMMAND_CANCEL_NOTIFICATION = "cancel_notification";
    public static final String COMMAND_SHOW_COACH_NOTIFICATION = "show_notification";
    public static final String COMMAND_FITNESS_ACTIVITY  ="fitness_activity";
    public static final String COMMAND_COACH_DATA  ="coach_data";
    public static final String COMMAND_COACH_TYPE_CHANGED ="coach_type_changed";
    public static final String COMMAND_COACH_GOAL_CHANGED ="coach_goal_changed";
    public static final String COMMAND_COACH_TARGET_CHANGED ="coach_target_changed";
    public static final String COMMAND_NEXT_PAGE ="goto_next_page";
    public static final String COMMAND_START_ACTIVITY ="start_activity";
    public static final String COMMAND_START_SLEEP="start_sleep";
    public static final  String COMMAND_SLEEP_VALUE  ="sleep_value";
    public static final String COMMAND_SLEEP_STATE="sleep_state";
    public static final String COMMAND_AMBIENT_MODE="ambient_mode";
    public static final String COMMAND_SHOW_SLEEP_NOTIFICATION="show_sleep_notification";
    public static final String COMMAND_TODAY_STEP_SYNCDONE="today_step_sync_done";
    public static final String COMMAND_GET_FITNESS_STEP="get_fitness_step";

    public String sender ;
    public String receiver;
    public String command;
    public Object param ;


    public EBCommand(String sender, String receiver, String command, Object param){
        this.sender = sender;
        this.receiver = receiver;
        this.param = param;
        this.command = command;
    }

    @Override
    public String toString(){
        return new Gson().toJson(this).toString();
    }

    public  static EBCommand fromJson(String json){
        return  new Gson().fromJson(json,EBCommand.class);
    }

}
