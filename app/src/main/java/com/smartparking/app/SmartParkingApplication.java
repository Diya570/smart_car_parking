package com.smartparking.app;

import android.app.Application;



import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.smartparking.app.utils.NotificationHelper;

import timber.log.Timber;

public class SmartParkingApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // This line requires the BuildConfig class to be imported
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }


        // Initialize Firebase
        FirebaseApp.initializeApp(this);

        // Enable Firestore offline persistence for a seamless offline experience
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build();
        firestore.setFirestoreSettings(settings);

        // Create notification channels on app startup
        NotificationHelper.createNotificationChannels(this);
    }
}