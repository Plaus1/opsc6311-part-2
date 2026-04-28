# Add project specific ProGuard rules here.
-keep class com.spendsmart.data.** { *; }
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
