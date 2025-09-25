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
-dontobfuscate
#1. 确保保留所有带@Keep注解的类（优先级最高）
-keep @androidx.annotation.Keep class * { *; }
-keepclassmembers @androidx.annotation.Keep class * {
    <fields>;
    <methods>;
}
# Keep Gson classes
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keep class com.google.gson.internal.** { *; }
-keep class com.google.gson.internal.LinkedTreeMap { *; }
-keep class com.google.gson.internal.LinkedTreeMap$* { *; }
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

#保留 ini4j 的核心类和 SPI 实现
-keep class org.ini4j.** { *; }
-keepclassmembers class org.ini4j.** { *; }
# 保留 Kotlin 标准库
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.** { *; }
-keep class kotlin.ExceptionsKt { *; }
-keepclassmembers class kotlin.ExceptionsKt { *; }

# 基础保留规则 - 对序列化至关重要
-keepattributes Signature, Exceptions, InnerClasses
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes *Annotation*
-keepattributes EnclosingMethod

# 保护反射相关
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# 保护HashMap和泛型类型
-keep class java.util.HashMap { *; }
-keep class java.util.Map { *; }
-keep class java.util.Map$Entry { *; }
-keepclassmembers class java.util.HashMap {
    <fields>;
    <methods>;
}

# 保留 Byte Buddy 相关
-keep class net.bytebuddy.** { *; }

# 保护序列化相关的核心类
-keep class com.eam.rwtranslator.ui.project.TranslationConfigManager { *; }
-keepclassmembers class com.eam.rwtranslator.ui.project.TranslationConfigManager {
    public java.util.HashMap translationIniFiles;
    public java.lang.String projectName;
    public java.io.File projectRootDir;
    public java.io.File projectFile;
    public java.util.concurrent.ExecutorService executorService;
    <fields>;
    <methods>;
}

-keep class com.eam.rwtranslator.utils.serializer.WiniSerializer { *; }
-keep class com.eam.rwtranslator.utils.deserializer.WiniDeserializer { *; }
-keep class com.eam.rwtranslator.utils.serializer.HashMapWiniSerializer { *; }
-keep class com.eam.rwtranslator.utils.deserializer.HashMapWiniDeserializer { *; }
-keep class com.eam.rwtranslator.utils.CacheManager { *; }

#忽略警告
-dontwarn java.beans.**
   -dontwarn org.apache.commons.beanutils.**a

   -dontwarn org.jetbrains.kotlin.compiler.plugin.CliOption
   -dontwarn org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
   -dontwarn org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar