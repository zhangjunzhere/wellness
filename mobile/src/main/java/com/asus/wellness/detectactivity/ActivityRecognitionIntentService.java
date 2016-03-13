/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.asus.wellness.detectactivity;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * Service that receives ActivityRecognition updates. It receives updates
 * in the background, even if the main Activity is not visible.
 */
public class ActivityRecognitionIntentService extends IntentService {

	public static final int DETECT_CONFIDENCE_THRESHOLD=50;
	
    public ActivityRecognitionIntentService() {
        // Set the label for the service's background thread
        super("ActivityRecognitionIntentService");
    }

    /**
     * Called when a new activity detection update is available.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
    	Log.d("circle","onhandleintent");
        if (ActivityRecognitionResult.hasResult(intent)) {
            // Get the update
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

            // Get the most probable activity from the list of activities in the update
            DetectedActivity mostProbableActivity = result.getMostProbableActivity();

            // Get the confidence percentage for the most probable activity
            int confidence = mostProbableActivity.getConfidence();
            
            if(confidence>DETECT_CONFIDENCE_THRESHOLD){
            	sendBroadcastToManager(mostProbableActivity.getType());
            }
        }
    }
    
    private void sendBroadcastToManager(int activityType){
        Intent intent=new Intent(DetectActivityManager.ACTION_SEND_ACTIVITY_TYPE);
        intent.putExtra(DetectActivityManager.KEY_ACTIVITY_TYPE, activityType);
        sendBroadcast(intent);
    }
}
