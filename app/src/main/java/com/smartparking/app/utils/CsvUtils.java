package com.smartparking.app.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.core.content.FileProvider;
import com.smartparking.app.data.model.Booking;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import timber.log.Timber;

public class CsvUtils {

    /**
     * Generates a CSV file from a list of bookings and triggers a share intent.
     * @param context The context to use for file operations and starting the intent.
     * @param bookings The list of bookings to export.
     * @param fileName The desired name for the CSV file (e.g., "report-2025-08-28.csv").
     */
    public static void exportBookingsToCsv(Context context, List<Booking> bookings, String fileName) {
        StringBuilder data = new StringBuilder();
        // Add header
        data.append("Booking ID,User ID,Lot ID,Slot ID,Start Time,End Time,Status,Cost,OTP\n");

        // Add rows
        for (Booking booking : bookings) {
            data.append(booking.getBookingId()).append(",");
            data.append(booking.getUserId()).append(",");
            data.append(booking.getLotId()).append(",");
            data.append(booking.getSlotId()).append(",");
            data.append(TimeUtils.formatDateTime(booking.getStartTime())).append(",");
            data.append(TimeUtils.formatDateTime(booking.getEndTime())).append(",");
            data.append(booking.getStatus()).append(",");
            data.append(booking.getCost()).append(",");
            data.append(booking.getOtp()).append("\n");
        }

        try {
            // Save the file in the cache directory
            File cachePath = new File(context.getCacheDir(), "reports");
            cachePath.mkdirs(); // Make sure the directory exists
            File file = new File(cachePath, fileName);

            FileOutputStream out = new FileOutputStream(file);
            out.write(data.toString().getBytes());
            out.close();

            // Get a content URI using FileProvider
            Uri contentUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);

            if (contentUri != null) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.setDataAndType(contentUri, context.getContentResolver().getType(contentUri));
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Parking Booking Report");
                context.startActivity(Intent.createChooser(shareIntent, "Share Report via"));
            }

        } catch (IOException e) {
            Timber.e(e, "Error writing CSV file");
        }
    }
}