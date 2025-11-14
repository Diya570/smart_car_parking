package com.smartparking.app.services;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.smartparking.app.data.repository.UserRepository;
import com.smartparking.app.utils.NotificationHelper;

import java.util.Map;

import timber.log.Timber;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    /**
     * Called if the FCM registration token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the
     * FCM registration token is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Timber.d("Refreshed FCM token: %s", token);
        // If a user is logged in, update their token in Firestore.
        // This is a fire-and-forget operation.
        new UserRepository().updateFcmToken();
    }

    /**
     * Called when a message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Timber.d("FCM Message Received From: %s", remoteMessage.getFrom());

        // Check if message contains a notification payload (sent from Firebase Console).
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            Timber.d("Notification Title: %s, Body: %s", title, body);

            // Here we use the custom NotificationHelper to display the notification,
            // ensuring it uses the correct channel we created. This is crucial for Android 8.0+.
            NotificationHelper.showNotification(
                    getApplicationContext(),
                    title,
                    body,
                    NotificationHelper.CHANNEL_ID_ANNOUNCEMENTS
            );
        }

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Map<String, String> data = remoteMessage.getData();
            Timber.d("Data Payload: %s", data);
            // You can process the data payload here to perform background tasks or create
            // more customized notifications. For this project, we rely on the notification payload.
        }
    }
}