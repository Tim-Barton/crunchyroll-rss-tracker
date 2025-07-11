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

# Keep data classes
-keep class com.example.crunchyrollwatcher.Anime { *; }

# Keep ViewModel classes
-keep class * extends androidx.lifecycle.ViewModel { *; }

# Keep RecyclerView adapter classes
-keep class * extends androidx.recyclerview.widget.RecyclerView$Adapter { *; }
-keep class * extends androidx.recyclerview.widget.RecyclerView$ViewHolder { *; }

# Keep classes that use reflection
-keepclassmembers class * {
    @androidx.lifecycle.* <methods>;
}

# Keep Activity classes
-keep class * extends androidx.appcompat.app.AppCompatActivity { *; }

# Material Design Components
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

# Kotlin Coroutines
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Keep LiveData
-keep class androidx.lifecycle.** { *; }
-dontwarn androidx.lifecycle.**
