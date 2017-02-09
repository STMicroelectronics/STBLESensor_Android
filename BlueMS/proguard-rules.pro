# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:/Android/sdk/tools/proguard/proguard-android.txt
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

-keep class com.androidplot.xy.** { *; }
-keep class com.androidplot.ui.** { *; }
-keep public class * extends com.st.BlueSTSDK.Feature
-keepclassmembers class * extends com.st.BlueSTSDK.Feature{
    public <init>(com.st.BlueSTSDK.Node);
}
#workaround for android support 23.1.0
-keep class android.support.v7.widget.LinearLayoutManager { *; }
#workaround after switch to jack
-keep public class android.support.v7.widget.** { *; }