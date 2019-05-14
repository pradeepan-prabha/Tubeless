package com.techmind.tubeless.fcm;

/**
 * Created by admin on 6/9/17.
 */

public class Config {

    // global topic to receive app wide push notifications
    public static final String TOPIC_GLOBAL = "global";

    // broadcast receiver intent filters
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    public static final String PUSH_NOTIFICATION = "pushNotification";

    // id to handle the notification in the notification tray
    public static final int NOTIFICATION_ID = 100;
    public static final int NOTIFICATION_ID_BIG_IMAGE = 101;

    public static final String SHARED_PREF = "ah_firebase";

    public static final String FCM_TYPE_HAIL_RIDE_REQUEST = "1";
    public static final String FCM_TYPE_HOP_IN_RIDE_REQUEST = "7";
    public static final String FCM_TYPE_RIDE_CANCELLED= "6";

    public static final String RIDE_TYPE_HAIL= "1";
    public static final String RIDE_TYPE_FIXED= "2";
}