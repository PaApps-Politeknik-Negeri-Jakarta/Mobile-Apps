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

# Add the keep rules from missing_rules.txt below
# Copy the contents from C:\Users\Asus\AndroidStudioProjects\TugasAkhirApp\app\build\outputs\mapping\release\missing_rules.txt

# General keep rules for com.syhdzn.tugasakhirapp package
-keep class com.syhdzn.tugasakhirapp.** { *; }

# Specific rules if any from missing_rules.txt
# Example:
# -keep class com.syhdzn.tugasakhirapp.MyClass { *; }
# -keep interface com.syhdzn.tugasakhirapp.MyInterface { *; }

# If your app uses reflection or libraries, add their rules as well
-keep class com.thirdparty.** { *; }
# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.OpenSSLProvider
