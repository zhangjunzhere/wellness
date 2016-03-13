package com.asus.wellness;

import android.test.ApplicationTestCase;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;

import com.asus.wellness.service.CollectStepCountService;
import com.asus.wellness.utils.EBCommand;
import com.asus.wellness.sleep.SleepDataModel;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<WApplication> {
    public ApplicationTest() {
        super(WApplication.class);

    }

    @Override
    protected void setUp() throws Exception{
        super.setUp();
        createApplication();
        WApplication.getInstance().startService(CollectStepCountService.class);
    }

    @SmallTest
    public void testGetSleepQualityByDate(){
        long toady = 1443578700086L;
        long span = 15*60*1000;
      //  List<Integer> result =  SleepDataModel.getSleepQualityByDate(toady, span);

        Log.d("ApplicationTest", "testSimpleCreate");
    }

    @SmallTest
    public void testSleepEnabled(){
        EBCommand ebCommand = new EBCommand(this.getClass().getName(), CollectStepCountService.class.getName(),EBCommand.COMMAND_START_SLEEP, true);
        EventBus.getDefault().post(ebCommand);

        ebCommand.param = false;
        EventBus.getDefault().post(ebCommand);

        Log.d("ApplicationTest", "testSleepEnabled");
    }

    @SmallTest
    public void testStartSleepActivity(){
        String command = "adb shell am start -n com.asus.wellness/.sleep.NotificationSleepActivity";
        Log.d("ApplicationTest", "testStartSleepActivity");
    }
}