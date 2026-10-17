# DRAPE ProGuard / R8 Rules
-keepattributes *Annotation*,InnerClasses,Signature

# Kotlinx Serialization
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.drape.app.**$$serializer { *; }
-keepclassmembers class com.drape.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.drape.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
