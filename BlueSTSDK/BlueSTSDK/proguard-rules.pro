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
# Preserve all annotations.

-keepattributes *Annotation*

# Preserve all public classes, and their public and protected fields and
# methods.

-keep public class * {
    public protected *;
}

# Preserve all .class method names.

-keepclassmembernames class * {
    java.lang.Class class$(java.lang.String);
    java.lang.Class class$(java.lang.String, boolean);
}

# Preserve all native method names and the names of their classes.

-keepclasseswithmembernames class * {
    native <methods>;
}

# Preserve the special static methods that are required in all enumeration
# classes.

-keepclassmembers class * extends java.lang.Enum {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Explicitly preserve all serialization members. The Serializable interface
# is only a marker interface, so it wouldn't save them.
# You can comment this out if your library doesn't use serialization.
# If your code contains serializable classes that have to be backward
# compatible, please refer to the manual.
-keep public class * extends com.st.BlueSTSDK.Feature
-keepclassmembers class * extends com.st.BlueSTSDK.Feature{
    public <init>(com.st.BlueSTSDK.Node);
}

# Your library may contain more items that need to be preserved;
# typically classes that are dynamically created using Class.forName:

# -keep public class mypackage.MyClass
# -keep public interface mypackage.MyInterface
# -keep public class * implements mypackage.MyInterface

-keepclasseswithmembernames class com.st.BlueSTSDK.fwDataBase.db.BoardCatalog
-keepclasseswithmembernames class com.st.BlueSTSDK.fwDataBase.db.BoardFirmware
-keepclasseswithmembernames class com.st.BlueSTSDK.fwDataBase.db.BleCharacteristic
-keepclasseswithmembernames class com.st.BlueSTSDK.fwDataBase.db.BleCharacteristicProperty
-keepclasseswithmembernames class com.st.BlueSTSDK.fwDataBase.db.BleCharacteristicFormat
-keepclasseswithmembernames class com.st.BlueSTSDK.fwDataBase.db.OptionByte
-keepclasseswithmembernames class com.st.BlueSTSDK.fwDataBase.db.OptionByteEnumType
