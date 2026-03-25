# Project-specific ProGuard rules.

# Keep WebView JavaScript bridge methods callable by name from JS.
-keepclassmembers class com.selegic.encye.article.EditorBridge {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep route and DTO serializers used by Navigation 3 and kotlinx.serialization.
-keep class com.selegic.encye.AppDestinations { *; }
-keep class com.selegic.encye.AppDestinations$* { *; }
-keep class com.selegic.encye.data.remote.dto.** { *; }
-keepclassmembers class com.selegic.encye.data.remote.dto.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Preserve serializer metadata and runtime-visible annotations used by Retrofit
# and kotlinx.serialization generated adapters.
-keepattributes RuntimeVisibleAnnotations,RuntimeInvisibleAnnotations,AnnotationDefault,Signature,InnerClasses,EnclosingMethod

# Keep source and line information for useful release stack traces.
-keepattributes SourceFile,LineNumberTable
