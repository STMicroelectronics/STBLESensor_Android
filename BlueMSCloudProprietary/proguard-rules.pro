
### Retrofit - ST Azure web dashboard ####

#retrofit proguard rules http://square.github.io/retrofit/
# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keepattributes Signature, InnerClasses, EnclosingMethod

# Retrofit does reflection on method and parameter annotations.
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Ignore annotation used for build tooling.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit

# Top-level functions that can only be used by Kotlin.
-dontwarn retrofit2.-KotlinExtensions

# With R8 full mode, it sees no subtypes of Retrofit interfaces since they are created with a Proxy
# and replaces all potential values with null. Explicitly keeping the interfaces prevents this.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

### Azure iot central ###

#Keep ADAL classes
-keep class com.microsoft.aad.adal.** { *; }
-keep class com.microsoft.identity.common.** { *; }
-keep class com.microsoft.azure.management.** { *; }
-keep class com.microsoft.aad.adal4j.** { *; }
-keep class com.microsoft.azure.management.Azure{*;}

#Keep Gson for ADAL https://github.com/google/gson/blob/master/examples/android-proguard-example/proguard.cfg
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.Signal
-dontwarn sun.misc.SignalHandler

-keep class com.google.gson.examples.android.model.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

-dontwarn org.bouncycastle.**
-dontwarn com.microsoft.identity.common.internal.providers.oauth2.AuthorizationActivity

-dontwarn com.microsoft.azure.management.**
-dontwarn com.microsoft.aad.adal4j.**
-dontwarn com.microsoft.azure.credentials.AzureTokenCredentials
-dontwarn com.microsoft.azure.AzureEnvironment
-dontwarn java.awt.Desktop
-dontwarn edu.umd.cs.findbugs.annotations.SuppressFBWarnings

-keepclassmembers class com.github.lucadruda.iotcentral.service.Application{ *;}
-keepclassmembers class com.github.lucadruda.iotcentral.service.Device{ *;}
-keepclassmembers class com.github.lucadruda.iotcentral.service.DeviceTemplate{ *;}
-keepclassmembers class com.github.lucadruda.iotcentral.service.Subscription{ *;}
-keepclassmembers class com.github.lucadruda.iotcentral.service.types.** { *;}