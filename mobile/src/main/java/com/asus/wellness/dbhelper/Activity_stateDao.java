package com.asus.wellness.dbhelper;

import java.util.List;
import java.util.ArrayList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.SqlUtils;
import de.greenrobot.dao.internal.DaoConfig;

import com.asus.wellness.dbhelper.Activity_state;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table activity_state.
*/
public class Activity_stateDao extends AbstractDao<Activity_state, Long> {

    public static final String TABLENAME = "activity_state";

    /**
     * Properties of entity Activity_state.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Start = new Property(1, Long.class, "start", false, "start");
        public final static Property End = new Property(2, Long.class, "end", false, "end");
        public final static Property Step_count = new Property(3, Long.class, "step_count", false, "step_count");
        public final static Property Distance = new Property(4, Long.class, "distance", false, "distance");
        public final static Property Type = new Property(5, Long.class, "type", false, "type");
        public final static Property DeviceId = new Property(6, Long.class, "deviceId", false, "deviceId");
    };

    private DaoSession daoSession;


    public Activity_stateDao(DaoConfig config) {
        super(config);
    }
    
    public Activity_stateDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'activity_state' (" + //
                "'_id' INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "'start' INTEGER," + // 1: start
                "'end' INTEGER," + // 2: end
                "'step_count' INTEGER," + // 3: step_count
                "'distance' INTEGER," + // 4: distance
                "'type' INTEGER," + // 5: type
                "'deviceId' INTEGER);"); // 6: deviceId
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'activity_state'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, Activity_state entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        Long start = entity.getStart();
        if (start != null) {
            stmt.bindLong(2, start);
        }
 
        Long end = entity.getEnd();
        if (end != null) {
            stmt.bindLong(3, end);
        }
 
        Long step_count = entity.getStep_count();
        if (step_count != null) {
            stmt.bindLong(4, step_count);
        }
 
        Long distance = entity.getDistance();
        if (distance != null) {
            stmt.bindLong(5, distance);
        }
 
        Long type = entity.getType();
        if (type != null) {
            stmt.bindLong(6, type);
        }
 
        Long deviceId = entity.getDeviceId();
        if (deviceId != null) {
            stmt.bindLong(7, deviceId);
        }
    }

    @Override
    protected void attachEntity(Activity_state entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public Activity_state readEntity(Cursor cursor, int offset) {
        Activity_state entity = new Activity_state( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getLong(offset + 1), // start
            cursor.isNull(offset + 2) ? null : cursor.getLong(offset + 2), // end
            cursor.isNull(offset + 3) ? null : cursor.getLong(offset + 3), // step_count
            cursor.isNull(offset + 4) ? null : cursor.getLong(offset + 4), // distance
            cursor.isNull(offset + 5) ? null : cursor.getLong(offset + 5), // type
            cursor.isNull(offset + 6) ? null : cursor.getLong(offset + 6) // deviceId
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, Activity_state entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setStart(cursor.isNull(offset + 1) ? null : cursor.getLong(offset + 1));
        entity.setEnd(cursor.isNull(offset + 2) ? null : cursor.getLong(offset + 2));
        entity.setStep_count(cursor.isNull(offset + 3) ? null : cursor.getLong(offset + 3));
        entity.setDistance(cursor.isNull(offset + 4) ? null : cursor.getLong(offset + 4));
        entity.setType(cursor.isNull(offset + 5) ? null : cursor.getLong(offset + 5));
        entity.setDeviceId(cursor.isNull(offset + 6) ? null : cursor.getLong(offset + 6));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(Activity_state entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(Activity_state entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
    private String selectDeep;

    protected String getSelectDeep() {
        if (selectDeep == null) {
            StringBuilder builder = new StringBuilder("SELECT ");
            SqlUtils.appendColumns(builder, "T", getAllColumns());
            builder.append(',');
            SqlUtils.appendColumns(builder, "T0", daoSession.getDeviceDao().getAllColumns());
            builder.append(" FROM activity_state T");
            builder.append(" LEFT JOIN device T0 ON T.'deviceId'=T0.'_id'");
            builder.append(' ');
            selectDeep = builder.toString();
        }
        return selectDeep;
    }
    
    protected Activity_state loadCurrentDeep(Cursor cursor, boolean lock) {
        Activity_state entity = loadCurrent(cursor, 0, lock);
        int offset = getAllColumns().length;

        Device device = loadCurrentOther(daoSession.getDeviceDao(), cursor, offset);
        entity.setDevice(device);

        return entity;    
    }

    public Activity_state loadDeep(Long key) {
        assertSinglePk();
        if (key == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder(getSelectDeep());
        builder.append("WHERE ");
        SqlUtils.appendColumnsEqValue(builder, "T", getPkColumns());
        String sql = builder.toString();
        
        String[] keyArray = new String[] { key.toString() };
        Cursor cursor = db.rawQuery(sql, keyArray);
        
        try {
            boolean available = cursor.moveToFirst();
            if (!available) {
                return null;
            } else if (!cursor.isLast()) {
                throw new IllegalStateException("Expected unique result, but count was " + cursor.getCount());
            }
            return loadCurrentDeep(cursor, true);
        } finally {
            cursor.close();
        }
    }
    
    /** Reads all available rows from the given cursor and returns a list of new ImageTO objects. */
    public List<Activity_state> loadAllDeepFromCursor(Cursor cursor) {
        int count = cursor.getCount();
        List<Activity_state> list = new ArrayList<Activity_state>(count);
        
        if (cursor.moveToFirst()) {
            if (identityScope != null) {
                identityScope.lock();
                identityScope.reserveRoom(count);
            }
            try {
                do {
                    list.add(loadCurrentDeep(cursor, false));
                } while (cursor.moveToNext());
            } finally {
                if (identityScope != null) {
                    identityScope.unlock();
                }
            }
        }
        return list;
    }
    
    protected List<Activity_state> loadDeepAllAndCloseCursor(Cursor cursor) {
        try {
            return loadAllDeepFromCursor(cursor);
        } finally {
            cursor.close();
        }
    }
    

    /** A raw-style query where you can pass any WHERE clause and arguments. */
    public List<Activity_state> queryDeep(String where, String... selectionArg) {
        Cursor cursor = db.rawQuery(getSelectDeep() + where, selectionArg);
        return loadDeepAllAndCloseCursor(cursor);
    }
 
}
