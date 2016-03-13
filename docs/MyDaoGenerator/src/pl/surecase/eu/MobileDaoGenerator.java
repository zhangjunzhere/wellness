package pl.surecase.eu;



import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.PropertyType;
import de.greenrobot.daogenerator.Schema;

public class MobileDaoGenerator {

    public static void main(String args[]) throws Exception {

        final int SCHEMA_VERSION = 1102;
        final String outDir = "./mobile/src/main/java/";
        final String javaPackage = "com.asus.wellness.dbhelper";

        Schema schema = new Schema(SCHEMA_VERSION, javaPackage);

        Entity device = schema.addEntity("Device");
        device.setTableName("device");
        device.addIdProperty().primaryKey().autoincrement();
        device.addProperty(PropertyType.String, "name").columnName("name");
        device.addProperty(PropertyType.String ,"blueaddr").columnName("blueaddr");
        device.addProperty(PropertyType.Boolean ,"isRobin").columnName("isrobin");
        device.addProperty(PropertyType.Long ,"lastconnecttime").columnName("lastconnecttime");
        device.addProperty(PropertyType.Long ,"stepsynctime").columnName("stepsynctime");
        device.addProperty(PropertyType.Long ,"ecgsynctime").columnName("ecgsynctime");
        device.setHasKeepSections(true);

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
        profile.addProperty(PropertyType.Int, "distance_unit").columnName("distance_unit");
        profile.addProperty(PropertyType.Long, "birthday").columnName("birthday");
        profile.setHasKeepSections(true);
//        ContentProvider cp = profile.addContentProvider();
//        cp.setClassName("ProfileContentProvider");
//        cp.setAuthority(javaPackage + ".profile");
//        cp.setBasePath("profile");


        Entity activity_status = schema.addEntity("Activity_state");
        activity_status.setTableName("activity_state");
        activity_status.addIdProperty().primaryKey().autoincrement();
        activity_status.addProperty(PropertyType.Long, "start").columnName("start");
        activity_status.addProperty(PropertyType.Long, "end").columnName("end");
        activity_status.addProperty(PropertyType.Long, "step_count").columnName("step_count");
        activity_status.addProperty(PropertyType.Long, "distance").columnName("distance");
        activity_status.addProperty(PropertyType.Long, "type").columnName("type");
        Property deivceId = activity_status.addProperty(PropertyType.Long, "deviceId").columnName("deviceId").getProperty();
      //  Property catalogOfProduct = product.addProperty(PropertyType.Long, "category_id").getProperty();
        activity_status.addToOne(device, deivceId, "device");
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
        ecg.addProperty(PropertyType.String, "measure_comment").columnName("measure_comment");
     //   ecg.addProperty(PropertyType.String, "measure_comment123").columnName("measure_comment123");
        deivceId = ecg.addProperty(PropertyType.Long, "deviceId").columnName("deviceId").getProperty();
        //  Property catalogOfProduct = product.addProperty(PropertyType.Long, "category_id").getProperty();
        ecg.addToOne(device, deivceId, "device");
//        cp = ecg.addContentProvider();
//        cp.setClassName("EcgContentProvider");
//        cp.setAuthority(javaPackage + ".ecg");
//        cp.setBasePath("ecg");

        Entity step_count = schema.addEntity("Step_goal");
        step_count.setTableName("step_goal");
        step_count.addIdProperty().primaryKey().autoincrement();
        step_count.addStringProperty("date_milli").columnName("date_milli");
        step_count.addProperty(PropertyType.Long, "step_goal").columnName("step_goal");
//        cp = step_count.addContentProvider();
//        cp.setClassName("Step_countContentProvider");
//        cp.setAuthority(javaPackage+".step_count");
//        cp.setBasePath("step_count");


        Entity location_change = schema.addEntity("Location_change");
        location_change.setTableName("location_change");
        location_change.addIdProperty().primaryKey().autoincrement();
        location_change.addProperty(PropertyType.Long,"get_location_time").columnName("get_location_time");
        location_change.addProperty(PropertyType.Double, "latitude").columnName("latitude");
        location_change.addProperty(PropertyType.Double, "longitude").columnName("longitude");
        location_change.addProperty(PropertyType.String, "district").columnName("district");

//        Entity syncdata = schema.addEntity("Syncdata");
//        syncdata.setTableName("syncdata");
//        syncdata.addIdProperty().primaryKey().autoincrement();
//        syncdata.addProperty(PropertyType.Long, "ecgsyncid").columnName("ecgsyncid");
//        syncdata.addProperty(PropertyType.Long,"stepsyncid").columnName("stepsyncid");

        Entity coach = schema.addEntity("Coach");
        coach.setTableName("coach");
        coach.addIdProperty().primaryKey().autoincrement();
        coach.addProperty(PropertyType.Long, "start").columnName("start");
        coach.addProperty(PropertyType.Long, "end").columnName("end");
        coach.addProperty(PropertyType.Long, "duration").columnName("duration").notNull();
        coach.addProperty(PropertyType.Long, "value").columnName("value");
        coach.addProperty(PropertyType.Int, "percent").columnName("percent");
        coach.addProperty(PropertyType.Long, "type").columnName("type");
        deivceId = coach.addProperty(PropertyType.Long, "deviceId").columnName("deviceId").getProperty();
        //  Property catalogOfProduct = product.addProperty(PropertyType.Long, "category_id").getProperty();
        coach.addToOne(device, deivceId, "device");


        Entity sleep = schema.addEntity("Sleep");
        sleep.setTableName("sleep");
        sleep.addIdProperty().primaryKey().autoincrement();
        sleep.addProperty(PropertyType.Long, "start").columnName("start").notNull();
        sleep.addProperty(PropertyType.Long, "end").columnName("end");
        sleep.addProperty(PropertyType.String, "date").columnName("date");
        sleep.addProperty(PropertyType.String, "data").columnName("data");
//        sleep.addProperty(PropertyType.Long, "deep_percent").columnName("deep_percent");
        deivceId = sleep.addProperty(PropertyType.Long, "deviceId").columnName("deviceId").getProperty();
        sleep.addToOne(device, deivceId, "device");

        new DaoGenerator().generateAll(schema, outDir);
    }
}
