<vector android:height="24dp" android:tint="#FFFFFF"
    android:viewportHeight="24.0" android:viewportWidth="24.0"
    android:width="24dp" xmlns:android="http://schemas.android.com/apk/res/android">
    <path android:fillColor="#ffffff" android:pathData="M15.5,14h-0.79l-0.28,-0.27C15.41,12.59 16,11.11 16,9.5 16,5.91 13.09,3 9.5,3S3,5.91 3,9.5 5.91,16 9.5,16c1.61,0 3.09,-0.59 4.23,-1.57l0.27,0.28v0.79l5,4.99L20.49,19l-4.99,-5zM9.5,14C7.01,14 5,11.99 5,9.5S7.01,5 9.5,5 14,7.01 14,9.5 11.99,14 9.5,14z"/>
</vector>
                                                                                                                                                                                                                                                             # keep youtube / google
 -keep class com.google.api.services.** { *; }
-keep class com.google.android.youtube.player.** { *; }
# Needed by google-api-client to keep generic types and @Key annotations accessed via reflection
-keepclassmembers class * {
@com.google.api.client.util.Key <fields>;
}