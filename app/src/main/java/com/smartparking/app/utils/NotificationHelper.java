package com.smartparking.app.utils;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.smartparking.app.R;
import com.smartparking.app.ui.main.MainActivity;

public class NotificationHelper {

    public static final String CHANNEL_ID_BOOKINGS = "booking_reminders";
    public static final String CHANNEL_ID_ANNOUNCEMENTS = "announcements";

    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Bookings Channel (High Importance for reminders)
            NotificationChannel bookingsChannel = new NotificationChannel(
                    CHANNEL_ID_BOOKINGS,
                    context.getString(R.string.notification_channel_bookings),
                    NotificationManager.IMPORTANCE_HIGH
            );
            bookingsChannel.setDescription("Notifications for upcoming booking reminders and expirations.");

            // Announcements Channel (Default Importance)
            NotificationChannel announcementsChannel = new NotificationChannel(
                    CHANNEL_ID_ANNOUNCEMENTS,
                    context.getString(R.string.notification_channel_announcements),
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            announcementsChannel.setDescription("General announcements from parking administrators.");

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(bookingsChannel);
                manager.createNotificationChannel(announcementsChannel);
            }
        }
    }

    public static void showNotification(Context context, String title, String content, String channelId) {
        // Create a unique ID for each notification to prevent them from overwriting each other
        int notificationId = (int) (System.currentTimeMillis() & 0xfffffff);

        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // Use FLAG_IMMUTABLE for PendingIntents on newer Android versions
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_stat_notification) // A white-on-transparent icon is required
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent) // Set the intent that will fire when the user taps the notification
                .setAutoCancel(true); // Automatically removes the notification when the user taps it

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // Check for notification permission before trying to notify
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // This check is required for Android 13 (API 33) and above.
            // The app should have already requested this permission. If it's still not granted, we can't post.
            return;
        }
        notificationManager.notify(notificationId, builder.build());
    }
}