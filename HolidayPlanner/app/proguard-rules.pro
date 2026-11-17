# ProGuard / R8 rules for HolidayPlanner

# Keep Room generated classes
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Keep Retrofit / OkHttp
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

# Keep DTOs / Entities for Gson reflection
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.ashok.holidayplanner.data.remote.dto.** { *; }
-keep class com.ashok.holidayplanner.data.local.entities.** { *; }
-keep class com.ashok.holidayplanner.domain.model.** { *; }

# Keep Credential Manager & Google ID
-keep class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }
