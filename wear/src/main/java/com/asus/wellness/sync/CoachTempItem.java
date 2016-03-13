package com.asus.wellness.sync;

import com.asus.sharedata.CoachSyncItem;
import com.asus.wellness.dbhelper.Coach;

import java.util.List;

/**
 * Created by smile_gao on 2015/10/21.
 */
public class CoachTempItem {
    public List<CoachSyncItem> coachSyncItemList;
    public List<Coach> coachList;
    public CoachTempItem(List<CoachSyncItem> csilist, List<Coach> coachlist)
    {
        coachSyncItemList = csilist;
        coachList = coachlist;
    }
}
