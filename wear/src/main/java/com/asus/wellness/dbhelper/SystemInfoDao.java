package com.asus.wellness.dbhelper;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

import com.asus.wellness.dbhelper.SystemInfo;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table SYSTEM_INFO.
*/
public class SystemInfoDao extends AbstractDao<SystemInfo, Long> {

    public static final String TABLENAME = "SYSTEM_INFO";

    /**
     * Properties of entity SystemInfo.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property SystemId = new Property(0, Long.class, "systemId", true, "SYSTEM_ID");
        public final static Property Firtuse = new Property(1, Boolean.class, "firtuse", false, "FIRTUSE");
    };


    public SystemInfoDao(DaoConfig config) {
        super(config);
    }
    
    public SystemInfoDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'SYSTEM_INFO' (" + //
                "'SYSTEM_ID' INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: systemId
                "'FIRTUSE' INTEGER);"); // 1: firtuse
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'SYSTEM_INFO'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, SystemInfo entity) {
        stmt.clearBindings();
 
        Long systemId = entity.getSystemId();
        if (systemId != null) {
            stmt.bindLong(1, systemId);
        }
 
        Boolean firtuse = entity.getFirtuse();
        if (firtuse != null) {
            stmt.bindLong(2, firtuse ? 1l: 0l);
        }
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public SystemInfo readEntity(Cursor cursor, int offset) {
        SystemInfo entity = new SystemInfo( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // systemId
            cursor.isNull(offset + 1) ? null : cursor.getShort(offset + 1) != 0 // firtuse
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, SystemInfo entity, int offset) {
        entity.setSystemId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setFirtuse(cursor.isNull(offset + 1) ? null : cursor.getShort(offset + 1) != 0);
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(SystemInfo entity, long rowId) {
        entity.setSystemId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(SystemInfo entity) {
        if(entity != null) {
            return entity.getSystemId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
}
