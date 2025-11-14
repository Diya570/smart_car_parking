package com.smartparking.app.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.WriteBatch;
import com.smartparking.app.constants.Constants;
import com.smartparking.app.data.model.Booking;
import com.smartparking.app.data.source.Result;
import com.smartparking.app.workers.BookingReminderWorker;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import timber.log.Timber;

public class BookingRepository {
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;

    public BookingRepository() {
        this.firestore = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    public LiveData<Result<Integer>> getOccupiedSlotsCount(String lotId, Date timeToCheck) {
        MutableLiveData<Result<Integer>> result = new MutableLiveData<>();
        result.setValue(Result.loading(null));

        SimpleDateFormat hourIdFormat = new SimpleDateFormat("yyyyMMdd_HH", Locale.US);
        String hourId = hourIdFormat.format(timeToCheck);

        Query query = firestore.collection(Constants.COLLECTION_BOOKINGS)
                .whereEqualTo("lotId", lotId)
                .whereIn("status", List.of("confirmed", "checkedIn"))
                .whereArrayContains("occupiedHours", hourId);

        query.count().get(AggregateSource.SERVER).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                result.setValue(Result.success((int) task.getResult().getCount()));
            } else {
                result.setValue(Result.error(task.getException() != null ? task.getException().getMessage() : "Failed to count", null));
            }
        });
        return result;
    }

    public LiveData<Result<Booking>> createBooking(Booking bookingToCreate, String lotName, Context context) {
        MutableLiveData<Result<Booking>> result = new MutableLiveData<>();
        result.setValue(Result.loading(null));

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            result.setValue(Result.error("Transaction failed: User not authenticated.", null));
            return result;
        }

        String userId = currentUser.getUid();
        bookingToCreate.setOccupiedHours(generateOccupiedHours(bookingToCreate.getStartTime(), bookingToCreate.getEndTime()));
        DocumentReference bookingRef = firestore.collection(Constants.COLLECTION_BOOKINGS).document();

        bookingToCreate.setBookingId(bookingRef.getId());
        bookingToCreate.setUserId(userId);
        bookingToCreate.setStatus("confirmed");
        bookingToCreate.setEntryToken(UUID.randomUUID().toString());

        long startMinute = TimeUnit.MILLISECONDS.toMinutes(bookingToCreate.getStartTime());
        long endMinute = TimeUnit.MILLISECONDS.toMinutes(bookingToCreate.getEndTime());

        // Step 1: Run the query outside the transaction to check for conflicts
        Query conflictCheckQuery = firestore.collection(Constants.COLLECTION_SLOT_TIME_INDEX)
                .whereEqualTo("slotId", bookingToCreate.getSlotId())
                .whereGreaterThanOrEqualTo("epochMinute", startMinute)
                .whereLessThan("epochMinute", endMinute);

        conflictCheckQuery.get().addOnSuccessListener(conflictSnapshots -> {
            if (!conflictSnapshots.isEmpty()) {
                result.setValue(Result.error("Slot is unavailable for the selected time. Please try another time.", null));
                return;
            }

            // Step 2: Proceed with transaction to safely write booking and slot indexes
            firestore.runTransaction((Transaction.Function<Void>) transaction -> {
                transaction.set(bookingRef, bookingToCreate);

                for (long minute = startMinute; minute < endMinute; minute += Constants.TIME_BUCKET_MINUTES) {
                    Map<String, Object> indexData = new HashMap<>();
                    indexData.put("bookingId", bookingToCreate.getBookingId());
                    indexData.put("userId", userId);
                    indexData.put("slotId", bookingToCreate.getSlotId());
                    indexData.put("epochMinute", minute);
                    indexData.put("lotId", bookingToCreate.getLotId());
                    DocumentReference indexRef = firestore.collection(Constants.COLLECTION_SLOT_TIME_INDEX).document();
                    transaction.set(indexRef, indexData);
                }

                return null;
            }).addOnSuccessListener(aVoid -> {
                Timber.d("Transaction success! Booking created: %s", bookingToCreate.getBookingId());
                scheduleBookingReminder(context, bookingToCreate, lotName);
                result.setValue(Result.success(bookingToCreate));
            }).addOnFailureListener(e -> {
                Timber.e(e, "Transaction failure.");
                result.setValue(Result.error(e.getMessage(), null));
            });

        }).addOnFailureListener(e -> {
            result.setValue(Result.error("Failed to check for slot conflicts: " + e.getMessage(), null));
        });

        return result;
    }

    public LiveData<Result<Void>> cancelBooking(Booking booking, Context context) {
        MutableLiveData<Result<Void>> result = new MutableLiveData<>();
        result.setValue(Result.loading(null));

        firestore.collection(Constants.COLLECTION_SLOT_TIME_INDEX)
                .whereEqualTo("bookingId", booking.getBookingId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    WriteBatch batch = firestore.batch();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        batch.delete(doc.getReference());
                    }
                    DocumentReference bookingRef = firestore.collection(Constants.COLLECTION_BOOKINGS).document(booking.getBookingId());
                    batch.update(bookingRef, "status", "canceled", "canceledAt", new Date());

                    batch.commit().addOnSuccessListener(aVoid -> {
                        Timber.d("Booking canceled successfully: %s", booking.getBookingId());
                        cancelBookingReminder(context, booking.getBookingId());
                        result.setValue(Result.success(null));
                    }).addOnFailureListener(e -> result.setValue(Result.error(e.getMessage(), null)));
                })
                .addOnFailureListener(e -> result.setValue(Result.error("Failed to find booking indexes to cancel: " + e.getMessage(), null)));

        return result;
    }

    public LiveData<Result<List<Booking>>> getUserBookings() {
        MutableLiveData<Result<List<Booking>>> result = new MutableLiveData<>();
        result.setValue(Result.loading(null));

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            result.setValue(Result.error("No user is currently logged in.", new ArrayList<>()));
            return result;
        }

        String userId = currentUser.getUid();

        firestore.collection(Constants.COLLECTION_BOOKINGS)
                .whereEqualTo("userId", userId)
                .orderBy("startTime", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Booking> bookings = queryDocumentSnapshots.toObjects(Booking.class);
                    result.setValue(Result.success(bookings));
                })
                .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage(), null)));

        return result;
    }

    public LiveData<Result<Set<String>>> getConflictingSlotIds(String lotId, long startTime, long endTime) {
        MutableLiveData<Result<Set<String>>> result = new MutableLiveData<>();
        result.setValue(Result.loading(null));

        long startMinute = TimeUnit.MILLISECONDS.toMinutes(startTime);
        long endMinute = TimeUnit.MILLISECONDS.toMinutes(endTime);

        firestore.collection(Constants.COLLECTION_SLOT_TIME_INDEX)
                .whereEqualTo("lotId", lotId)
                .whereGreaterThanOrEqualTo("epochMinute", startMinute)
                .whereLessThan("epochMinute", endMinute)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Set<String> conflictingSlotIds = new HashSet<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String docSlotId = (String) doc.get("slotId");
                        if (docSlotId != null) {
                            conflictingSlotIds.add(docSlotId);
                        }
                    }
                    result.setValue(Result.success(conflictingSlotIds));
                })
                .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage(), null)));

        return result;
    }

    private List<String> generateOccupiedHours(long startTimeMillis, long endTimeMillis) {
        Set<String> hours = new HashSet<>();
        SimpleDateFormat hourIdFormat = new SimpleDateFormat("yyyyMMdd_HH", Locale.US);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(startTimeMillis);

        while (cal.getTimeInMillis() < endTimeMillis) {
            hours.add(hourIdFormat.format(cal.getTime()));
            cal.add(Calendar.HOUR_OF_DAY, 1);
        }

        cal.setTimeInMillis(endTimeMillis);
        hours.add(hourIdFormat.format(cal.getTime()));
        return new ArrayList<>(hours);
    }

    private void scheduleBookingReminder(Context context, Booking booking, String lotName) {
        long now = System.currentTimeMillis();
        long reminderTime = booking.getStartTime() - TimeUnit.MINUTES.toMillis(Constants.BOOKING_REMINDER_MINUTES_BEFORE);
        if (reminderTime > now) {
            Data data = new Data.Builder()
                    .putString(Constants.WORK_DATA_BOOKING_ID, booking.getBookingId())
                    .putString(Constants.WORK_DATA_LOT_NAME, lotName)
                    .build();

            OneTimeWorkRequest reminderWork = new OneTimeWorkRequest.Builder(BookingReminderWorker.class)
                    .setInitialDelay(Duration.ofMillis(reminderTime - now))
                    .setInputData(data)
                    .build();

            String workTag = Constants.WORK_NAME_BOOKING_REMINDER + booking.getBookingId();
            WorkManager.getInstance(context).enqueueUniqueWork(workTag, androidx.work.ExistingWorkPolicy.REPLACE, reminderWork);
            Timber.d("Scheduled reminder for booking %s", booking.getBookingId());
        }
    }

    private void cancelBookingReminder(Context context, String bookingId) {
        String workTag = Constants.WORK_NAME_BOOKING_REMINDER + bookingId;
        WorkManager.getInstance(context).cancelUniqueWork(workTag);
        Timber.d("Cancelled reminder for booking %s", bookingId);
    }
}
