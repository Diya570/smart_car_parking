package com.smartparking.app.workers;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.smartparking.app.R;
import com.smartparking.app.constants.Constants;
import com.smartparking.app.utils.NotificationHelper;

import timber.log.Timber;

public class BookingReminderWorker extends Worker {

    public BookingReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Retrieve data passed to the worker
        Data inputData = getInputData();
        String lotName = inputData.getString(Constants.WORK_DATA_LOT_NAME);
        String bookingId = inputData.getString(Constants.WORK_DATA_BOOKING_ID);

        if (lotName == null || bookingId == null) {
            Timber.e("Missing data for booking reminder worker. Cannot show notification.");
            return Result.failure();
        }

        Timber.d("Executing reminder worker for booking: %s", bookingId);

        // Prepare notification content
        String title = getApplicationContext().getString(R.string.booking_reminder_title);
        String body = getApplicationContext().getString(R.string.booking_reminder_body, lotName);

        // Use the helper to display the notification on the correct channel
        NotificationHelper.showNotification(
                getApplicationContext(),
                title,
                body,
                NotificationHelper.CHANNEL_ID_BOOKINGS
        );

        return Result.success();
    }
}