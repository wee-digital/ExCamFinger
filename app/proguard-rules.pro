# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-keep class com.intel.** { *; }
-keep interface com.intel.** { *; }
-dontwarn com.intel.**

-keep class crypto.** { *; }
-keep interface crypto.** { *; }
-dontwarn crypto.**

-keep class go.Seq.** { *; }
-keep interface go.Seq.** { *; }
-dontwarn go.Seq.**

-keep class wee.digital.camera.core.** { *; }
-keep interface wee.digital.camera.core.** { *; }
-dontwarn wee.digital.camera.core.**

-keep class wee.vietinbank.kiosk.data.model.** { *; }
-dontwarn wee.vietinbank.kiosk.data.model.**

-keep class com.HEROFUN.** { *; }
-dontwarn com.HEROFUN.**

-keep class wee.vietinbank.kiosk.util.Shell
-dontwarn wee.vietinbank.kiosk.util.Shell
