package flo.org.campusmein.app.utils.NotificationUtils;

/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import flo.org.campusmein.R;
import flo.org.campusmein.app.Home.MainHomeActivity;
import flo.org.campusmein.app.Home.ProductView.productView;
import flo.org.campusmein.app.Home.orderPlacement.orderDetailsView;
import flo.org.campusmein.app.Home.orderPlacement.ordersView;


public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = MyGcmListenerService.class.getSimpleName();


    private static final String ORD_ID_KEY = "ord_id";
    private static final String PRODUCT_OBJECT_ID = "objectId";



    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);
        Log.d(TAG, "Whole Data: " + data.toString());


        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext());

        if(data.getString("size").equals("small")){
            if(data.getString("type").equals("order")){

                Intent order = new Intent(getApplicationContext(), orderDetailsView.class);
                order.putExtra(ORD_ID_KEY,data.getString("ord_id"));

                showNotificationMessage(mBuilder,getApplicationContext(),data.getString("message"),data.getString("subMessage"),data.getString("timestamp"),order);
            }else if(data.getString("type").equals("product")){

                Intent product = new Intent(getApplicationContext(), productView.class);
                product.putExtra(PRODUCT_OBJECT_ID,data.getString("productId"));

                showNotificationMessage(mBuilder,getApplicationContext(),data.getString("message"),data.getString("subMessage"),data.getString("timestamp"),product);
            }else if(data.getString("type").equals("home")){

                Intent home = new Intent(getApplicationContext(), MainHomeActivity.class);

                showNotificationMessage(mBuilder,getApplicationContext(),data.getString("message"),data.getString("subMessage"),data.getString("timestamp"),home);
            }


        }else if(data.getString("size").equals("big")){

            if(data.getString("type").equals("order")){

                Intent order = new Intent(getApplicationContext(), orderDetailsView.class);
                order.putExtra(ORD_ID_KEY,data.getString("ord_id"));

                showNotificationMessageWithBigImage(mBuilder,getApplicationContext(),data.getString("message"),data.getString("subMessage"),data.getString("timestamp"),order,data.getString("imgUrl"));
            }else if(data.getString("type").equals("product")){

                Intent product = new Intent(getApplicationContext(), productView.class);
                product.putExtra(PRODUCT_OBJECT_ID,data.getString("productId"));

                showNotificationMessageWithBigImage(mBuilder,getApplicationContext(),data.getString("message"),data.getString("subMessage"),data.getString("timestamp"),product,data.getString("imgUrl"));
            }else if(data.getString("type").equals("home")){
                Intent home = new Intent(getApplicationContext(), MainHomeActivity.class);
                showNotificationMessageWithBigImage(mBuilder,getApplicationContext(),data.getString("message"),data.getString("subMessage"),data.getString("timestamp"),home,data.getString("imgUrl"));
            }else {
                sendNotification(message);
            }

        }
        // [END_EXCLUDE]
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */

    /**
     * Showing notification with text only
     */
    private void showNotificationMessage(NotificationCompat.Builder mBuilder,Context context, String title, String message, String timeStamp, Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        NotificationCompat.BigTextStyle bigtext = new NotificationCompat.BigTextStyle();

        inboxStyle.addLine(message);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Notification notification;
        notification = mBuilder
                .setTicker(title)
                .setAutoCancel(true)
                .setContentText(message)
                .setContentTitle(title)
                .setContentIntent(pendingIntent)
                .setSound(defaultSoundUri)
//                .setStyle(bigtext)
                .setWhen(getTimeMilliSec(timeStamp))
                .setSmallIcon(R.drawable.ic_notification_campusme)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(Config.NOTIFICATION_ID, notification);
    }

    /**
     * Showing notification with text and image
     */
    private void showNotificationMessageWithBigImage(NotificationCompat.Builder mBuilder,Context context, String title, String message, String timeStamp, Intent intent, String imageUrl) {
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Bitmap bitmap = getBitmapFromURL(imageUrl);

        NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle();
        bigPictureStyle.setBigContentTitle(title);
        bigPictureStyle.setSummaryText(message);
        bigPictureStyle.bigPicture(bitmap);

        Notification notification;
        notification = mBuilder
                .setTicker(title)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentIntent(pendingIntent)
                .setSound(defaultSoundUri)
                .setStyle(bigPictureStyle)
                .setWhen(getTimeMilliSec(timeStamp))
                .setSmallIcon(R.drawable.ic_notification_campusme)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(Config.NOTIFICATION_ID_BIG_IMAGE, notification);
    }

    /**
     * Downloading push notification image before displaying it in
     * the notification tray
     */
    public Bitmap getBitmapFromURL(String strURL) {
        try {
            URL url = new URL(strURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    private void sendNotification(String message) {
        Intent intent = new Intent(this, ordersView.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification_campusme)
                .setContentTitle(message)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }


    public static long getTimeMilliSec(String timeStamp) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = format.parse(timeStamp);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
