package com.asus.wellness.dbhelper;

/**
 * Created by smile_gao on 2015/7/7.
 */


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.internal.DaoConfig;

/**
 * Created by pokawa on 18/05/15.
 */
public class MigrationHelper {
    private final static  String TAG = "MigrationHelper";
    private static final String CONVERSION_CLASS_NOT_FOUND_EXCEPTION = "MIGRATION HELPER - CLASS DOESN'T MATCH WITH THE CURRENT PARAMETERS";
    private static MigrationHelper instance;

    public static MigrationHelper getInstance() {
        if(instance == null) {
            instance = new MigrationHelper();
        }
        return instance;
    }

    public void migrate(SQLiteDatabase db, Class<? extends AbstractDao<?, ?>>... daoClasses) {
        generateTempTables(db, daoClasses);
        DaoMaster.dropAllTables(db, true);
        DaoMaster.createAllTables(db, false);
        restoreData(db, daoClasses);
    }

    private void generateTempTables(SQLiteDatabase db, Class<? extends AbstractDao<?, ?>>... daoClasses) {
        for(int i = 0; i < daoClasses.length; i++) {
            DaoConfig daoConfig = new DaoConfig(db, daoClasses[i]);

            String divider = "";
            String tableName = daoConfig.tablename;
            String tempTableName = daoConfig.tablename.concat("_TEMP");
            ArrayList<String> properties = new ArrayList<String>();

            List<String> columns = getColumns(db, tableName);
            StringBuilder createTableStringBuilder = new StringBuilder();
            createTableStringBuilder.append("CREATE TABLE ").append(tempTableName).append(" (");

            for(int j = 0; j < daoConfig.properties.length; j++) {
                String columnName = daoConfig.properties[j].columnName;
                if(columns.contains(columnName)) {
                    properties.add(columnName);
                    String type = null;

                    try {
                        type = getTypeByClass(daoConfig.properties[j].type);
                    } catch (Exception exception) {
                      //  Crashlytics.logException(exception);
                    }

                    createTableStringBuilder.append(divider).append(columnName).append(" ").append(type);

                    if(daoConfig.properties[j].primaryKey) {
                        createTableStringBuilder.append(" PRIMARY KEY");
                    }

                    divider = ",";
                }
            }
            //no such table
            if(properties.size() == 0){
                continue;
            }

            createTableStringBuilder.append(");");
            db.execSQL(createTableStringBuilder.toString());

            StringBuilder insertTableStringBuilder = new StringBuilder();

            insertTableStringBuilder.append("INSERT INTO ").append(tempTableName).append(" (");
            insertTableStringBuilder.append(TextUtils.join(",", properties));
            insertTableStringBuilder.append(") SELECT ");
            insertTableStringBuilder.append(TextUtils.join(",", properties));
            insertTableStringBuilder.append(" FROM ").append(tableName).append(";");

            db.execSQL(insertTableStringBuilder.toString());
        }
    }

    private void restoreData(SQLiteDatabase db, Class<? extends AbstractDao<?, ?>>... daoClasses) {
        for(int i = 0; i < daoClasses.length; i++) {
            DaoConfig daoConfig = new DaoConfig(db, daoClasses[i]);

            String tableName = daoConfig.tablename;
            String tempTableName = daoConfig.tablename.concat("_TEMP");
            List<String> columns = getColumns(db, tempTableName);


            ArrayList<String> properties = new ArrayList();

            for (int j = 0; j < daoConfig.properties.length; j++) {
                String columnName = daoConfig.properties[j].columnName;
                if(columns.contains(columnName)) {
                    properties.add(columnName);
                }
            }
            //no such table
            if(properties.size() == 0){
                continue;
            }

            StringBuilder insertTableStringBuilder = new StringBuilder();

            insertTableStringBuilder.append("INSERT INTO ").append(tableName).append(" (");
            insertTableStringBuilder.append(TextUtils.join(",", properties));
            insertTableStringBuilder.append(") SELECT ");
            insertTableStringBuilder.append(TextUtils.join(",", properties));
            insertTableStringBuilder.append(" FROM ").append(tempTableName).append(";");

            StringBuilder dropTableStringBuilder = new StringBuilder();

            dropTableStringBuilder.append("DROP TABLE ").append(tempTableName);

            db.execSQL(insertTableStringBuilder.toString());
            db.execSQL(dropTableStringBuilder.toString());
        }
    }

    private String getTypeByClass(Class<?> type) throws Exception {
        if(type.equals(String.class)) {
            return "TEXT";
        }
        if(type.equals(Long.class) || type.equals(Integer.class) || type.equals(long.class)) {
            return "INTEGER";
        }
        if(type.equals(Boolean.class)) {
            return "BOOLEAN";
        }

        Exception exception = new Exception(CONVERSION_CLASS_NOT_FOUND_EXCEPTION.concat(" - Class: ").concat(type.toString()));
      //  Crashlytics.logException(exception);
        throw exception;
    }

    private static List<String> getColumns(SQLiteDatabase db, String tableName) {
        List<String> columns = new ArrayList<String>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + tableName + " limit 1", null);
            if (cursor != null) {
                columns = new ArrayList<String>(Arrays.asList(cursor.getColumnNames()));
            }
        } catch (Exception e) {
            Log.v(tableName, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return columns;
    }

    private static List<String> getTables(SQLiteDatabase db) {
        List<String> tables = new ArrayList<String>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM sqlite_master WHERE type='table'", null);
            if (cursor != null && cursor.moveToFirst()) {
                do{
                    String table = cursor.getString(cursor.getColumnIndex("name"));
                    if(!table.equals("sqlite_sequence") && !table.equals("android_metadata")){
                        tables.add(table);
                    }
                }while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.v(TAG, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return tables;
    }

    public static void   migrate(SQLiteDatabase newDb, SQLiteDatabase oldDb) {
        String sql_attachDb = "ATTACH '" + oldDb.getPath() + "' AS old";
        newDb.execSQL(sql_attachDb);
        //get tables
        List<String> newTables = getTables(newDb);
//        newDb.beginTransaction();

        for(String table : newTables){
            StringBuilder  sb = new StringBuilder();
            List<String> newColumns = getColumns(newDb, table);
            List<String> oldColumns = getColumns(oldDb,table);

            List<String> properties = new ArrayList<String>();
//            HashSet<String> oldColumnSet = new HashSet<String>();
//            oldColumnSet.addAll(oldColumns);

            for(String column : newColumns){
                if(oldColumns.contains(column)){
                    properties.add(column);
                }
            }
            //table not match
            if(properties.size() == 0){
                continue;
            }

            String columnsToUpdate  = TextUtils.join(",", properties);
            String sql_insert = "insert or replace into "+ table+"(" +columnsToUpdate +")" +
                    " select " + columnsToUpdate + " from old."+table ;

            Log.d(TAG, "migration db :" + sql_insert);

            newDb.execSQL(sql_insert);
        }

//        newDb.endTransaction();
        //cannot detach database within transaction
        String sql_detach = "DETACH DATABASE old";
        newDb.execSQL(sql_detach);

        //oldDb.close();
        //newDb.close();
    }
}