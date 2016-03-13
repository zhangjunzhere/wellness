package pl.surecase.eu;



import de.greenrobot.daogenerator.ContentProvider;
import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.PropertyType;
import de.greenrobot.daogenerator.Schema;

public class WearDaoGenerator {

    public static void main(String args[]) throws Exception {

        final int SCHEMA_VERSION = 1113;
        final String outDir = "./wear/src/main/java/";
        final String javaPackage = "com.asus.wellness.dbhelper";

        Schema schema = new Schema(SCHEMA_VERSION, javaPackage);



        Entity profile = schema.addEntity("Profile");
        profile.setTableName("profile");
        profile.addIdProperty().primaryKey().autoincrement();
        profile.addProperty(PropertyType.String, "name").columnName("name");
        profile.addProperty(PropertyType.String, "photo_path").columnName("photo_path");
        profile.addProperty(PropertyType.Int, "age").columnName("age");
        profile.addProperty(PropertyType.Int, "gender").columnName("gender");
        profile.addProperty(PropertyType.Int, "height").columnName("height");
        profile.addProperty(PropertyType.Int, "height_unit").columnName("height_unit");
        profile.addProperty(PropertyType.Int, "weight").columnName("weight");
        profile.addProperty(PropertyType.Int, "weight_unit").columnName("weight_unit");
        profile.addProperty(PropertyType.Long, "start_time").columnName("start_time");
        profile.addProperty(PropertyType.Int, "step_goal").columnName("step_goal");
        profile.addProperty(PropertyType.Int, "next_step_goal").columnName("next_step_goal");
        profile.addProperty(PropertyType.Int, "distance_unit").columnName("distance_unit");
        profile.addProperty(PropertyType.Boolean, "isprofileset").columnName("isprofileset");
        profile.addProperty(PropertyType.Long ,"stepsynctime").columnName("stepsynctime");
        profile.addProperty(PropertyType.Long ,"ecgsynctime").columnName("ecgsynctime");
        profile.addProperty(PropertyType.ByteArray, "photodata").columnName("photodata");

        profile.setHasKeepSections(true);
//        ContentProvider cp = profile.addContentProvider();
//        cp.setClassName("ProfileContentProvider");
//        cp.setAuthority(javaPackage + ".profile");
//        cp.setBasePath("profile");

        Entity system = schema.addEntity("SystemInfo");
        system.addProperty(PropertyType.Long, "systemId").primaryKey().autoincrement();
        system.addProperty(PropertyType.Boolean, "firtuse");

        Entity activity_status = schema.addEntity("Activity_status");
        activity_status.setTableName("activity_status");
        activity_status.addIdProperty().primaryKey().autoincrement();
        activity_status.addProperty(PropertyType.Long, "step").columnName("step");
        activity_status.addProperty(PropertyType.Long, "distance").columnName("distance");
        activity_status.addProperty(PropertyType.Long, "activity_type").columnName("activity_type");
//        cp = activity_status.addContentProvider();
//        cp.setClassName("Activity_statusContentProvider");
//        cp.setAuthority(javaPackage + ".activity_status");
//        cp.setBasePath("activity_status");
//
        Entity ecg = schema.addEntity("Ecg");
        ecg.setTableName("ecg");
        ecg.addIdProperty().primaryKey().autoincrement();
        ecg.addProperty(PropertyType.Long, "measure_time").columnName("measure_time");
        ecg.addProperty(PropertyType.Long, "measure_value").columnName("measure_value");
        ecg.addProperty(PropertyType.Long, "measure_type").columnName("measure_type");
//        cp = ecg.addContentProvider();
//        cp.setClassName("EcgContentProvider");
//        cp.setAuthority(javaPackage + ".ecg");
//        cp.setBasePath("ecg");

        Entity step_count = schema.addEntity("Step_count");
        step_count.setTableName("step_count");
        step_count.addIdProperty().primaryKey().autoincrement();
        step_count.addProperty(PropertyType.Long, "start").columnName("start");
        step_count.addProperty(PropertyType.Long, "end").columnName("end");
        step_count.addProperty(PropertyType.Long, "step_count").columnName("step_count");
        step_count.addProperty(PropertyType.Long, "sensor_value").columnName("sensor_value");


        Entity coach = schema.addEntity("Coach");
        coach.setTableName("coach");
        coach.addIdProperty().primaryKey().autoincrement();
        coach.addProperty(PropertyType.Long, "start").columnName("start");
        coach.addProperty(PropertyType.Long, "end").columnName("end");
        coach.addProperty(PropertyType.Long, "duration").columnName("duration").notNull();
        coach.addProperty(PropertyType.Long, "value").columnName("value");
        coach.addProperty(PropertyType.Int, "percent").columnName("percent");
        coach.addProperty(PropertyType.Long, "type").columnName("type");


        Entity coachItem = schema.addEntity("CoachItem");
        coachItem.setTableName("coach_item");
        Property coachId = coachItem.addProperty(PropertyType.Long, "coach_id").columnName("coach_id").notNull().getProperty();
        coachItem.addProperty(PropertyType.Long, "start").primaryKey().columnName("start").notNull();
        coachItem.addProperty(PropertyType.Long, "end").columnName("end");
        coachItem.addProperty(PropertyType.Long, "value").columnName("value");

        //one coach has many coachItems
        coach.addToMany(coachItem, coachId, "items");


        Entity sleep = schema.addEntity("Sleep");
        sleep.setTableName("sleep");
        sleep.addIdProperty().primaryKey().autoincrement();
        sleep.addProperty(PropertyType.Long, "start").columnName("start").notNull();
        sleep.addProperty(PropertyType.Long, "end").columnName("end");
        sleep.addProperty(PropertyType.String, "date").columnName("date");
        sleep.addProperty(PropertyType.String, "data").columnName("data");

        new DaoGenerator().generateAll(schema, outDir);
    }
}
