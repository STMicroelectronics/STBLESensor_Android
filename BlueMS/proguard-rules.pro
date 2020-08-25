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


#ibm watson
#keep the class member name to avoid problems with json to java objects
-keepclassmembers class com.ibm.watson.speech_to_text.v1.** { <fields>; }
#ingore warnings about class that do not exist in android
-dontwarn javax.naming.InitialContext
-dontwarn javax.naming.Context
-dontwarn javax.mail.internet.MimeBodyPart
-dontwarn java.lang.instrument.ClassFileTransformer
-dontwarn javax.mail.internet.SharedInputStream
-dontwarn java.lang.ClassValue
-dontwarn javax.activation.DataContentHandler
-dontwarn okhttp3.internal.http.UnrepeatableRequestBody


#need for Crashlytics
-keepattributes *Annotation*                      # Keep Crashlytics annotations
-keepattributes SourceFile,LineNumberTable        # Keep file names/line numbers
-keep public class * extends java.lang.Exception

#need for okhttp (audio websocket)
# https://github.com/square/okhttp/blob/master/okhttp/src/main/resources/META-INF/proguard/okhttp3.pro
# JSR 305 annotations are for embedding nullability information.
#-dontwarn javax.annotation.**
# A resource is loaded with a relative path so the package of this class must be preserved.
#-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
#-dontwarn org.codehaus.mojo.animal_sniffer.*
# OkHttp platform used only on JVM and when Conscrypt dependency is available.
#-dontwarn okhttp3.internal.platform.ConscryptPlatform