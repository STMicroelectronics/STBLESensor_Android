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


#needed for the chinese voice to text
#see: MSC Develop Manual for Android.pdf
-keep class com.iflytek.**{*;}
-keepattributes Signature

#retrofit proguard rules http://square.github.io/retrofit/
# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote retrofit2.Platform
# Platform used when running on Java 8 VMs. Will not be used at runtime
-dontwarn retrofit2.Platform$Java8
# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions
#retrofit 2.3 & co
-dontwarn javax.annotation.**
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

#ibm watson
#keep the class member name to avoid problems with json to java objects
-keepclassmembers class com.ibm.watson.developer_cloud.speech_to_text.v1.** { <fields>; }
#ingore warnings about class that do not exist in android
-dontwarn javax.naming.InitialContext
-dontwarn javax.naming.Context

#need for amazon mobile analytics
#https://github.com/aws/aws-sdk-android/blob/master/Proguard.md
# Class names are needed in reflection
# Class names are needed in reflection
-keepnames class com.amazonaws.**
-keepnames class com.amazon.**
# Request handlers defined in request.handlers
-keep class com.amazonaws.services.**.*Handler
# The following are referenced but aren't required to run
-dontwarn com.fasterxml.jackson.**
-dontwarn org.apache.commons.logging.**
# Android 6.0 release removes support for the Apache HTTP client
-dontwarn org.apache.http.**
# The SDK has several references of Apache HTTP client
-dontwarn com.amazonaws.http.**
-dontwarn com.amazonaws.metrics.**

-dontwarn com.amazonaws.mobile.auth.facebook.FacebookSignInProvider
-dontwarn com.amazonaws.mobile.auth.google.GoogleSignInProvider
-dontwarn com.amazonaws.mobile.auth.userpools.CognitoUserPoolsSignInProvider

#need for Crashlytics
-keepattributes *Annotation*                      # Keep Crashlytics annotations
-keepattributes SourceFile,LineNumberTable        # Keep file names/line numbers
-keep public class * extends java.lang.Exception

#need for okhttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**