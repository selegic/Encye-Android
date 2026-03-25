# R8-specific rules for aggressive shrinking/obfuscation.

# Keep generated serializers and companions for @Serializable classes.
-if @kotlinx.serialization.Serializable class com.selegic.encye.**
-keep,allowshrinking,allowoptimization class <1>

-if @kotlinx.serialization.Serializable class com.selegic.encye.**
-keepclassmembers,allowoptimization class <1> {
    static ** Companion;
}

-if @kotlinx.serialization.Serializable class com.selegic.encye.**
-keepclassmembers,allowoptimization class <1>$Companion {
    kotlinx.serialization.KSerializer serializer(...);
}
