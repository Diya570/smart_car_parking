package com.smartparking.app.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeUtils {

    /**
     * Formats an epoch millisecond timestamp into a human-readable date and time string.
     * e.g., "Aug 30, 2025, 01:40 AM"
     * @param epochMillis The timestamp in milliseconds.
     * @return A formatted string in the user's local timezone.
     */
    public static String formatDateTime(long epochMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault()); // Display in user's local timezone
        return sdf.format(new Date(epochMillis));
    }

    /**
     * Formats an epoch millisecond timestamp into a human-readable time string.
     * e.g., "01:40 AM"
     * @param epochMillis The timestamp in milliseconds.
     * @return A formatted string in the user's local timezone.
     */
    public static String formatTime(long epochMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(new Date(epochMillis));
    }

    /**
     * Formats an epoch millisecond timestamp into a human-readable date string.
     * e.g., "August 30, 2025"
     * @param epochMillis The timestamp in milliseconds.
     * @return A formatted string in the user's local timezone.
     */
    public static String formatDate(long epochMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(new Date(epochMillis));
    }

    //================================================================================
    // THIS IS THE NEWLY ADDED METHOD THAT WAS MISSING
    //================================================================================
    /**
     * Formats an epoch millisecond timestamp into a string suitable for a filename.
     * Replaces colons and spaces with hyphens or underscores.
     * e.g., "2025-08-30_01-40-50"
     * @param epochMillis The timestamp in milliseconds.
     * @return A formatted, file-safe string.
     */
    public static String formatDateForFilename(long epochMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US);
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(new Date(epochMillis));
    }
    //================================================================================
}