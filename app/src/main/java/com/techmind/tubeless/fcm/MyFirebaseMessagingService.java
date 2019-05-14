package com.techmind.tubeless.fcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.techmind.tubeless.MainActivity;
import com.techmind.tubeless.R;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.Random;


public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();
    public static String REQUEST_ACCEPT = "0";
    LocalBroadcastManager broadcaster;

    public MyFirebaseMessagingService() {
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        show_Notification(remoteMessage);

    }

    private void show_Notification(RemoteMessage remoteMessage) {
        System.out.println("remoteMessage = " + remoteMessage);
        System.out.println("remoteMessage = " + remoteMessage.getData().get("title") + "-" + remoteMessage.getData().get("message"));
        try {
            Uri uriDefaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Map<String, String> data = remoteMessage.getData();
            for (Map.Entry<String, String> entry : data.entrySet()) {
                System.out.println("Key = " + entry.getKey() +
                        ", Value =" + entry.getValue());
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon((((BitmapDrawable) getResources().getDrawable(R.mipmap.ic_launcher)).getBitmap()))
                    .setContentTitle(getResources().getString(R.string.app_name))
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(data.get("message")))
                    .setContentText(data.get("title") + "-" + data.get("message"))
                    .setContentInfo("0")
                    .setAutoCancel(false)
                    .setSound(uriDefaultSound);
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
            String timestamp = df.format(c.getTime());


            Intent home = new Intent(this, MainActivity.class);
            home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent intent = PendingIntent.getActivity(this, 0, home, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(intent);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            int notId = new Random().nextInt(4);
            broadcaster = LocalBroadcastManager.getInstance(getBaseContext());

            Intent intentbro = new Intent(REQUEST_ACCEPT);
            broadcaster.sendBroadcast(intentbro);
            notificationManager.notify(notId, builder.build());

            Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
//            if( data.get("fcmtype").equalsIgnoreCase("12")){
//                JSONObject obj = new JSONObject( data.get("responsedata"));
////                AppController.getInstance().PassangersDetHM.remove(obj.getString("ongngrecid"));
//            }
//            pushNotification.putExtra("responsedata", data.get("responsedata"));
//            pushNotification.putExtra("fcmtype", data.get("fcmtype"));
            LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}