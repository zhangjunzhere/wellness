/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.asus.wellness.coach.setup;

import android.content.Context;
import android.os.Bundle;
import android.support.wearable.view.WearableListView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.asus.wellness.R;

public class WearableListItem extends LinearLayout
        implements WearableListView.OnCenterProximityListener {

    private Context mContext;

    private int main_style_selected  = -1;
    private int main_style_normal  = -1;
    private Boolean spannable  = false;

    public static final  int DEFAULT_STYLE  = -1;

    public static final String KEY_MAIN_STYLE_SELECTED="main_style_selected";
    public static final String KEY_MAIN_STYLE_NORMAL="main_style_normal";

    public static final String KEY_SPANNABLE_STRING="sub_spannable_string";


    private TextView tv_main;

    public WearableListItem(Context context) {
        this(context, null);
    }

    public WearableListItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WearableListItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        main_style_selected = R.style.coach_list_view_text_selected;
        main_style_normal = R.style.coach_list_view_text;
    }

    public void setArgs(Bundle textStyles){
        if(textStyles != null) {
            main_style_selected = textStyles.getInt(KEY_MAIN_STYLE_SELECTED, DEFAULT_STYLE);
            main_style_normal = textStyles.getInt(KEY_MAIN_STYLE_NORMAL, DEFAULT_STYLE);
            spannable = textStyles.getBoolean(KEY_SPANNABLE_STRING,false);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        tv_main = (TextView) findViewById(R.id.tv_main);
    }

    @Override
    public void onCenterPosition(boolean animate) {
        tv_main.setTextAppearance(mContext, main_style_selected);
//        setTimeSpanString();
    }

    @Override
    public void onNonCenterPosition(boolean animate) {
        tv_main.setTextAppearance(mContext, main_style_normal);
//        setTimeSpanString();
    }

    private void setTimeSpanString(){
        if(spannable){
            //00:00:00
            String text = tv_main.getText().toString();
            SpannableString spanStr = new SpannableString(text);
            spanStr.setSpan(new RelativeSizeSpan(0.8f), text.length() - 3, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv_main.setText(spanStr);
        }
    }
}
