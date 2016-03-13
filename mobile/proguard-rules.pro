# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\Software\Develop\Android\adt-bundle-windows-x86_64-20140702\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-keep public class * extends android.app.Activity  
-keep public class * extends android.app.Application  
-keep public class * extends android.app.Service  
-keep public class * extends android.content.BroadcastReceiver  
-keep public class * extends android.content.ContentProvider  
-keep public class * extends android.support.v4.app.FragmentActivity  
-keep public class * extends android.support.v4.app.Fragment  
  
-keep public class * extends android.view.View {  
    public <init>(android.content.Context);  
    public <init>(android.content.Context, android.util.AttributeSet);  
    public <init>(android.content.Context, android.util.AttributeSet, int);  
    public void set*(...);  
}  

-dontwarn oauth.signpost.signature.OAuthMessageSigner
##---------------Begin: proguard configuration for GreenDao  ----------
-keepclassmembers class * extends de.greenrobot.dao.AbstractDao {
    public static java.lang.String TABLENAME;
}
##---------------Begin: proguard configuration for Eventbus  ----------
-keep class **$Properties

-keepclassmembers class ** {
    public void onEvent*(**);
}
##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature
-keepattributes *Annotation*
# Gson specific classes
-keep class com.google.gson.stream.** { *; }
-keep class com.google.gson.examples.android.model.** { *; }
-keep class com.google.gson.** { *;}
-keep class com.google.gson.annotations.Expose { *;}
# Application classes that will be serialized/deserialized over Gson
-keep class com.asus.wellness.dbhelper.Device { *; }