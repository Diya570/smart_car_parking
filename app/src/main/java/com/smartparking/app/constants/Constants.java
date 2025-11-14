package com.smartparking.app.constants;

public class Constants {
    // Firestore Collection Names
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_PARKING_LOTS = "parking_lots";
    public static final String COLLECTION_SLOTS = "slots";
    public static final String COLLECTION_BOOKINGS = "bookings";
    public static final String COLLECTION_SLOT_TIME_INDEX = "slot_time_index";
    public static final String COLLECTION_FEEDBACK = "feedback";
    public static final String COLLECTION_ANNOUNCEMENTS = "announcements";

    // FCM Topics
    public static final String FCM_TOPIC_ANNOUNCEMENTS = "announcements";

    // Booking Logic Configuration
    public static final int TIME_BUCKET_MINUTES = 5; // Granularity for collision detection
    public static final int BOOKING_REMINDER_MINUTES_BEFORE = 15; // Schedule reminder notification

    // WorkManager Unique Work Names
    public static final String WORK_NAME_BOOKING_REMINDER = "bookingReminderWork_";
    public static final String WORK_NAME_BOOKING_EXPIRATION = "bookingExpirationWork_";
    public static final String WORK_DATA_BOOKING_ID = "booking_id";
    public static final String WORK_DATA_LOT_NAME = "lot_name";
    public static final String WORK_DATA_START_TIME = "start_time";

    // Geo-queries
    public static final double GEO_QUERY_RADIUS_METERS = 5000; // 5km
}