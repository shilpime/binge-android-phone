# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
-keepclassmembers class fqcn.of.javascript.interface.for.webview {
   public *;
}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

#........

#-keep public class MyClass
-keepattributes *Annotation*

# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keepattributes Signature, InnerClasses, EnclosingMethod

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
#-dontwarn retrofit2.-KotlinExtensions

-keep class retrofit2.** { *; }



#code commented - Refer - https://square.github.io/okhttp/features/r8_proguard/
#ok http
#-dontwarn com.squareup.okhttp3.**
#-keep class com.squareup.okhttp3.** { *; }
#-keep interface com.squareup.okhttp3.* { *; }
#-dontwarn javax.annotation.Nullable
#-dontwarn javax.annotation.ParametersAreNonnullByDefault


# okio -- required for curl loggin interceptor but in release build we
# dont need to intercept curl so commenting this
#-dontwarn okio.**



#joda time
#-dontwarn org.joda.convert.**
#-dontwarn org.joda.time.**
#-keep class org.joda.time.** { *; }
#-keep interface org.joda.time.** { *;}


#ExoPlayer
-keep class com.google.android.exoplayer.** { *; }

#Glide removed after enabling R8 https://github.com/bumptech/glide#r8--proguard
#-keep public class * implements com.bumptech.glide.module.GlideModule

-dontwarn java.nio.file.Files
-dontwarn java.nio.file.Path
-dontwarn java.nio.file.OpenOption
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement #TODO check

#-keep class your.package.name.model.** {*;}

# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# Gson specific classes
#-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; }



##---------------Begin: proguard configuration for Gson  ----------
##source - https://github.com/google/gson/blob/master/examples/android-proguard-example/proguard.cfg
##         https://stackoverflow.com/questions/23826171/proguard-for-android-and-gson
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-dontwarn sun.misc.**
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
#-keep class com.google.gson.examples.android.model.** { <fields>; }
-keep class com.tatasky.binge.data.networking.model.** { <fields>; }

# Prevent proguard from stripping interface information from TypeAdapter, TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

##---------------End: proguard configuration for Gson  ----------


# Application classes that will be serialized/deserialized over Gson
#-keep class com.google.gson.examples.android.model.** { *; }

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

-keepclassmembers enum * { *; }
#-keepclassmembers enum com.your.package.** { *; }
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class com.google.** { *; }

#-keep public class * extends android.app.Activity
#-keep public class * extends android.app.Application
#-keep public class * extends android.app.Service
#-keep public class * extends android.content.BroadcastReceiver
#-keep public class * extends android.content.ContentProvider
#-keep public class * extends android.app.backup.BackupAgentHelper
#-keep public class * extends android.preference.Preference


-keepattributes ElementList, Root
#-keepclassmembers class com.package.app.ClassItem{ *; }
#
#-keep public class org.apache.commons.io.**
#-keep public class com.google.gson.**
#-keep public class com.google.gson.** {public private protected *;}
#
#
###---------------Begin: proguard configuration for Gson ----------
#-keep class com.tataskymore.ActivityMonitor.ClassMultiPoints.** { *; }
#-keep public class com.tataskymore.ActivityMonitor$ClassMultiPoints     { public protected *; }
#-keep public class com.tataskymore.ActivityMonitor$ClassMultiPoints$ClassPoints { public protected *; }
#-keep public class com.tataskymore.ActivityMonitor$ClassMultiPoints$ClassPoints$ClassPoint { public protected *; }
#
###---------------End: proguard configuration for Gson ----------
#
#-optimizationpasses 5
#-dump class_files.txt
#-printseeds seeds.txt
#-printusage unused.txt
#-printmapping mapping.txt
#-optimizations !code/simplification/arithmetic,!field/*,!class/merging*/
#-allowaccessmodification
-repackageclasses ''
-optimizations !method/removal/parameter
#-dontobfuscate
-keepattributes InnerClasses

-keepclassmembers class * {
    static final %                *;
    static final java.lang.String *;
}

#-keep class com.tataskymore.A { *; }
#-keep class com.tataskymore.A$B { *; }
#-keep class com.tataskymore.A$C { *; }

#-keep class com.tataskymore.** {*;}

# PubNub
-dontwarn com.pubnub.**
-keep class com.pubnub.** { *; }
-keep class com.tatasky.binge.pubnub.**{*;}


# keep everything in this package from being renamed only
-keepnames class com.pubnub.** { *; }


-dontwarn de.**
-keep class de.** { *; }

# keep everything in this package from being renamed only
-keepnames class de.** { *; }

# Proguard configuration for Jackson 2.x (fasterxml package instead of codehaus package)
#-keep class com.fasterxml.jackson.databind.ObjectMapper {
#    public <methods>;
#    protected <methods>;
#}
#-keep class com.fasterxml.jackson.databind.ObjectWriter {
#    public ** writeValueAsString(**);
#}
#-keepnames class com.fasterxml.jackson.** { *; }
#-dontwarn com.fasterxml.jackson.databind.**

-keep class com.irdeto.**{*;}
-keep class com.erosnow.**{*;}
-keep class com.tatasky.binge.data.networking.models.**{*;}
-keep class com.tatasky.binge.ui.features.sidemenunavdrawer.contentlanguage.model.**{*;}
-keep class com.tatasky.binge.ui.features.player.model.**{*;}
-keep class com.tatasky.binge.shemaroo.modal.**{*;}
-keep class com.tatasky.binge.data.database.model.**{*;}
-keep class com.tatasky.binge.voot.model.**{*;}

#SonySdk Rules
-keep class com.sonylivandroidtssdk.**{*;}
-keep public class com.catchmedia.cmsdkCore.managers.CMSDKCoreManager
-keep public class com.catchmedia.cmsdk.managers.CMSDKManager
-keep class com.google.gson.**{*;}
-keep class com.logituit.**{*;}

-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

#-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn okhttp3.internal.platform.*
-dontnote retrofit2.Platform
-dontwarn retrofit2.Platform$Java8

-dontwarn com.catchmedia.**
-keep class com.catchmedia.** {*;}

#End of SonySDK Rules

-ignorewarnings

# AppsFlyer with Uninstall measurement
-dontwarn com.appsflyer.**
-keep public class com.google.firebase.messaging.FirebaseMessagingService {
  public *;
}

# Juspay SDK Proguard rules
-keep class in.juspay.** {*;}

# Probe SDK Rules (QoE)
-dontwarn com.probe.**
-keep class com.probe.** {*;}