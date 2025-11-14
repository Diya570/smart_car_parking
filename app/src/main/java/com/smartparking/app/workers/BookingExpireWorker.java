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

public class BookingExpireWorker extends Worker {

    public BookingExpireWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Data inputData = getInputData();
        String bookingId = inputData.getString(Constants.WORK_DATA_BOOKING_ID);
        String lotName = inputData.getString(Constants.WORK_DATA_LOT_NAME);

        if (lotName == null || bookingId == null) {
            Timber.e("Missing data for booking expiration worker.");
            return Result.failure();
        }

        Timber.d("Executing expiration worker for booking: %s", bookingId);

        String title = getApplicationContext().getString(R.string.booking_expired_title);
        String body = getApplicationContext().getString(R.string.booking_expired_body, lotName);

        NotificationHelper.showNotification(
                getApplicationContext(),
                title,
                body,
                NotificationHelper.CHANNEL_ID_BOOKINGS
        );

        // In a more complex scenario, you might trigger a Firestore update here to mark
        // the booking as "completed". However, for the Spark plan, it's safer to handle
        // this status change purely on the client-side UI based on the `endTime`.

        return Result.success();
    }
}