package com.smartparking.app.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.AggregateQuery;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;
import com.smartparking.app.constants.Constants;
import com.smartparking.app.data.model.Announcement;
import com.smartparking.app.data.model.Booking;
import com.smartparking.app.data.model.ParkingLot;
import com.smartparking.app.data.model.Slot;
import com.smartparking.app.data.model.User;
import com.smartparking.app.data.source.Result;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import timber.log.Timber;

public class AdminRepository {
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;
    private static final String TAG = "AdminRepo";

    public AdminRepository() {
        this.firestore = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    // --- Dashboard Statistics Methods ---

    public LiveData<Result<Long>> getLotCount() {
        Timber.tag(TAG).d("Fetching lot count...");
        MutableLiveData<Result<Long>> result = new MutableLiveData<>();
        result.setValue(Result.loading(null));
        firestore.collection(Constants.COLLECTION_PARKING_LOTS)
                .count()
                .get(AggregateSource.SERVER)
                .addOnSuccessListener(snapshot -> result.setValue(Result.success(snapshot.getCount())))
                .addOnFailureListener(e -> {
                    Timber.tag(TAG).e(e, "Failed to get lot count.");
                    result.setValue(Result.error(e.getMessage(), null));
                });
        return result;
    }

    public LiveData<Result<Long>> getUserCount() {
        Timber.tag(TAG).d("Fetching user count...");
        MutableLiveData<Result<Long>> result = new MutableLiveData<>();
        result.setValue(Result.loading(null));
        firestore.collection(Constants.COLLECTION_USERS)
                .count()
                .get(AggregateSource.SERVER)
                .addOnSuccessListener(snapshot -> result.setValue(Result.success(snapshot.getCount())))
                .addOnFailureListener(e -> {
                    Timber.tag(TAG).e(e, "Failed to get user count.");
                    result.setValue(Result.error(e.getMessage(), null));
                });
        return result;
    }

    public LiveData<Result<Long>> getActiveBookingCount() {
        Timber.tag(TAG).d("Fetching active booking count...");
        MutableLiveData<Result<Long>> result = new MutableLiveData<>();
        result.setValue(Result.loading(null));

        Query query = firestore.collection(Constants.COLLECTION_BOOKINGS)
                .whereIn("status", List.of("confirmed", "checkedIn"))
                .whereGreaterThan("endTime", System.currentTimeMillis());

        query.count().get(AggregateSource.SERVER)
                .addOnSuccessListener(snapshot -> result.setValue(Result.success(snapshot.getCount())))
                .addOnFailureListener(e -> {
                    Timber.tag(TAG).e(e, "Failed to get active booking count.");
                    result.setValue(Result.error(e.getMessage(), null));
                });
        return result;
    }

    public LiveData<Result<Double>> getTotalRevenue() {
        Timber.tag(TAG).d("Fetching total revenue...");
        MutableLiveData<Result<Double>> result = new MutableLiveData<>();
        result.setValue(Result.loading(null));

        Query query = firestore.collection(Constants.COLLECTION_BOOKINGS)
                .whereEqualTo("status", "confirmed");

        AggregateQuery sumQuery = query.aggregate(com.google.firebase.firestore.AggregateField.sum("cost"));
        sumQuery.get(AggregateSource.SERVER)
                .addOnSuccessListener(snapshot -> {
                    Number sumResult = (Number) snapshot.get(com.google.firebase.firestore.AggregateField.sum("cost"));
                    double totalRevenue = (sumResult != null) ? sumResult.doubleValue() : 0.0;
                    result.setValue(Result.success(totalRevenue));
                })
                .addOnFailureListener(e -> {
                    Timber.tag(TAG).e(e, "Failed to get total revenue.");
                    result.setValue(Result.error(e.getMessage(), null));
                });
        return result;
    }

    // --- Data Management Methods ---

    public LiveData<Result<List<Booking>>> getAllBookings() {
        MutableLiveData<Result<List<Booking>>> result = new MutableLiveData<>();
        result.setValue(Result.loading(null));
        firestore.collection(Constants.COLLECTION_BOOKINGS)
                .orderBy("startTime", Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Booking> bookings = queryDocumentSnapshots.toObjects(Booking.class);
                    result.setValue(Result.success(bookings));
                })
                .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage(), null)));
        return result;
    }
    /*
    public LiveData<Result<List<Announcement>>> getAllAnnouncements(Date userCreationDate) {
        MutableLiveData<Result<List<Announcement>>> result = new MutableLiveData<>();
        result.setValue(Result.loading(null));

        // Add a 'whereGreaterThan' clause to filter by the user's creation date.
        firestore.collection(Constants.COLLECTION_ANNOUNCEMENTS)
                .whereGreaterThan("createdAt", userCreationDate)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Announcement> announcements = queryDocumentSnapshots.toObjects(Announcement.class);
                    result.setValue(Result.success(announcements));
                })
                .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage(), null)));
        return result;
    }
     */
    public LiveData<Result<List<Announcement>>> getAllAnnouncements(Date userCreationDate) {
        MutableLiveData<Result<List<Announcement>>> result = new MutableLiveData<>();
        result.setValue(Result.loading(null));

        firestore.collection(Constants.COLLECTION_ANNOUNCEMENTS)
                .whereGreaterThan("createdAt", userCreationDate)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Announcement> announcements = queryDocumentSnapshots.toObjects(Announcement.class);
                    result.setValue(Result.success(announcements));
                })
                .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage(), null)));
        return result;
    }
    public LiveData<Result<List<Announcement>>> getAllAnnouncements() {
        MutableLiveData<Result<List<Announcement>>> result = new MutableLiveData<>();
        result.setValue(Result.loading(null));

        firestore.collection(Constants.COLLECTION_ANNOUNCEMENTS)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Announcement> announcements = queryDocumentSnapshots.toObjects(Announcement.class);
                    result.setValue(Result.success(announcements));
                })
                .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage(), null)));
        return result;
    }
    public LiveData<Result<List<User>>> getAllUsers() {
        MutableLiveData<Result<List<User>>> result = new MutableLiveData<>();
        result.setValue(Result.loading(null));
        firestore.collection(Constants.COLLECTION_USERS)
                .orderBy("displayName")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> users = queryDocumentSnapshots.toObjects(User.class);
                    result.setValue(Result.success(users));
                })
                .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage(), null)));
        return result;
    }

    public LiveData<Result<Void>> sendAnnouncement(String title, String message) {
        MutableLiveData<Result<Void>> result = new MutableLiveData<>();
        result.setValue(Result.loading(null));
        String adminUid = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        Announcement announcement = new Announcement();
        announcement.setTitle(title);
        announcement.setMessage(message);
        announcement.setType("info");
        announcement.setCreatedBy(adminUid);
        announcement.setCreatedAt(new Date());
        firestore.collection(Constants.COLLECTION_ANNOUNCEMENTS)
                .add(announcement)
                .addOnSuccessListener(documentReference -> {
                    documentReference.update("id", documentReference.getId());
                    result.setValue(Result.success(null));
                })
                .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage(), null)));
        return result;
    }

    public LiveData<Result<Void>> deleteUserFirestoreDocument(String uid) {
        MutableLiveData<Result<Void>> result = new MutableLiveData<>();
        result.setValue(Result.loading(null));
        firestore.collection(Constants.COLLECTION_USERS).document(uid).delete()
                .addOnSuccessListener(aVoid -> result.setValue(Result.success(null)))
                .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage(), null)));
        return result;
    }

    public LiveData<Result<List<ParkingLot>>> getAllLots() {
        MutableLiveData<Result<List<ParkingLot>>> result = new MutableLiveData<>();
        result.setValue(Result.loading(null));
        firestore.collection(Constants.COLLECTION_PARKING_LOTS).orderBy("name").get()
                .addOnSuccessListener(snaps -> result.setValue(Result.success(snaps.toObjects(ParkingLot.class))))
                .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage(), null)));
        return result;
    }

    public LiveData<Result<List<Booking>>> getBookingsByDateRange(long startTime, long endTime) {
        MutableLiveData<Result<List<Booking>>> result = new MutableLiveData<>();
        result.setValue(Result.loading(null));
        firestore.collection(Constants.COLLECTION_BOOKINGS)
                .whereGreaterThanOrEqualTo("startTime", startTime)
                .whereLessThanOrEqualTo("startTime", endTime)
                .orderBy("startTime", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snaps -> result.setValue(Result.success(snaps.toObjects(Booking.class))))
                .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage(), null)));
        return result;
    }

    // --- Check-in Methods ---

    /**
     * Finds a booking, verifies its QR token, start time, and status, then updates it to "checkedIn".
     */
    public LiveData<Result<Booking>> verifyAndCheckInWithQr(String bookingId, String entryToken) {
        MutableLiveData<Result<Booking>> result = new MutableLiveData<>();
        result.setValue(Result.loading(null));
        firestore.collection(Constants.COLLECTION_BOOKINGS).document(bookingId).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        result.setValue(Result.error("Booking ID not found.", null)); return;
                    }
                    Booking booking = doc.toObject(Booking.class);
                    if (booking == null || !booking.getEntryToken().equals(entryToken)) {
                        result.setValue(Result.error("Invalid QR Code.", null)); return;
                    }

                    if (!isCheckInTimeValid(booking)) {
                        result.setValue(Result.error("User is too early for this booking.", null)); return;
                    }

                    String status = booking.getStatus();
                    if ("confirmed".equals(status)) {
                        doc.getReference().update("status", "checkedIn", "checkedInAt", new Date())
                                .addOnSuccessListener(aVoid -> result.setValue(Result.success(booking)))
                                .addOnFailureListener(e -> result.setValue(Result.error("Failed to update status: " + e.getMessage(), null)));
                    } else {
                        result.setValue(Result.error("Booking is not 'confirmed'. Current status: " + status, null));
                    }
                })
                .addOnFailureListener(e -> result.setValue(Result.error("Failed to fetch booking: " + e.getMessage(), null)));
        return result;
    }

    /**
     * Finds a booking, verifies its OTP, start time, and status, then updates it to "checkedIn".
     */
    public LiveData<Result<Booking>> verifyAndCheckInWithOtp(String otp) {
        MutableLiveData<Result<Booking>> result = new MutableLiveData<>();
        result.setValue(Result.loading(null));

        firestore.collection(Constants.COLLECTION_BOOKINGS)
                .whereEqualTo("otp", otp)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        result.setValue(Result.error("Invalid OTP.", null));
                        return;
                    }
                    DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                    Booking booking = doc.toObject(Booking.class);

                    if (booking == null) {
                        result.setValue(Result.error("Invalid booking data.", null));
                        return;
                    }
                    if (!isCheckInTimeValid(booking)) {
                        result.setValue(Result.error("User is too early for this booking.", null));
                        return;
                    }

                    String status = booking.getStatus();
                    if ("confirmed".equals(status)) {
                        doc.getReference().update("status", "checkedIn", "checkedInAt", new Date())
                                .addOnSuccessListener(aVoid -> result.setValue(Result.success(booking)))
                                .addOnFailureListener(e -> result.setValue(Result.error("Failed to update status: " + e.getMessage(), null)));
                    } else {
                        result.setValue(Result.error("Booking is not 'confirmed'. Current status: " + status, null));
                    }
                })
                .addOnFailureListener(e -> result.setValue(Result.error("Failed to fetch booking: " + e.getMessage(), null)));
        return result;
    }

    /**
     * Helper method to check if a booking is within its valid check-in window.
     * Allows check-in 15 minutes before start time and up until the end time.
     */
    private boolean isCheckInTimeValid(Booking booking) {
        long now = System.currentTimeMillis();
        long startTime = booking.getStartTime();
        long endTime = booking.getEndTime();
        long gracePeriod = TimeUnit.MINUTES.toMillis(15);

        // Check-in is valid if:
        // 1. The current time is after the "grace period start time" (e.g., 15 mins before)
        // 2. The current time is before the booking's end time
        return (now >= (startTime - gracePeriod)) && (now < endTime);
    }

    // --- Lot & Slot CRUD Methods (Subcollection Model) ---

    public LiveData<Result<Void>> addParkingLot(ParkingLot lot) {
        MutableLiveData<Result<Void>> result = new MutableLiveData<>();
        result.setValue(Result.loading(null));

        firestore.collection(Constants.COLLECTION_PARKING_LOTS).add(lot)
                .addOnSuccessListener(lotDocRef -> {
                    String newLotId = lotDocRef.getId();
                    WriteBatch batch = firestore.batch();
                    for (int i = 1; i <= lot.getTotalSlots(); i++) {
                        DocumentReference slotDocRef = lotDocRef.collection(Constants.COLLECTION_SLOTS).document();
                        Slot newSlot = new Slot();
                        newSlot.setId(slotDocRef.getId());
                        newSlot.setLabel(String.format(Locale.US, "S-%03d", i));
                        newSlot.setLevel(0);
                        newSlot.setType("regular");
                        newSlot.setActive(true);
                        batch.set(slotDocRef, newSlot);
                    }
                    batch.update(lotDocRef, "id", newLotId);
                    batch.commit()
                            .addOnSuccessListener(aVoid -> result.setValue(Result.success(null)))
                            .addOnFailureListener(e -> result.setValue(Result.error("Failed to create slots: " + e.getMessage(), null)));
                })
                .addOnFailureListener(e -> result.setValue(Result.error("Failed to create lot: " + e.getMessage(), null)));
        return result;
    }

    public LiveData<Result<Void>> updateParkingLot(ParkingLot lot) {
        MutableLiveData<Result<Void>> result = new MutableLiveData<>();
        result.setValue(Result.loading(null));
        firestore.collection(Constants.COLLECTION_PARKING_LOTS).document(lot.getId())
                .set(lot)
                .addOnSuccessListener(aVoid -> result.setValue(Result.success(null)))
                .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage(), null)));
        return result;
    }

    public LiveData<Result<Void>> deleteParkingLot(ParkingLot lot) {
        MutableLiveData<Result<Void>> result = new MutableLiveData<>();
        result.setValue(Result.loading(null));
        firestore.collection(Constants.COLLECTION_PARKING_LOTS).document(lot.getId())
                .collection(Constants.COLLECTION_SLOTS).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    WriteBatch batch = firestore.batch();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        batch.delete(doc.getReference());
                    }
                    DocumentReference lotRef = firestore.collection(Constants.COLLECTION_PARKING_LOTS).document(lot.getId());
                    batch.delete(lotRef);
                    batch.commit()
                            .addOnSuccessListener(aVoid -> result.setValue(Result.success(null)))
                            .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage(), null)));
                })
                .addOnFailureListener(e -> result.setValue(Result.error("Failed to read slots for deletion: " + e.getMessage(), null)));
        return result;
    }

    public LiveData<Result<Void>> addSlot(String lotId, Slot slot) {
        MutableLiveData<Result<Void>> result = new MutableLiveData<>();
        result.setValue(Result.loading(null));
        firestore.collection(Constants.COLLECTION_PARKING_LOTS).document(lotId)
                .collection(Constants.COLLECTION_SLOTS)
                .add(slot)
                .addOnSuccessListener(docRef -> {
                    docRef.update("id", docRef.getId());
                    result.setValue(Result.success(null));
                })
                .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage(), null)));
        return result;
    }

    public LiveData<Result<Void>> updateSlot(String lotId, Slot slot) {
        MutableLiveData<Result<Void>> result = new MutableLiveData<>();
        result.setValue(Result.loading(null));
        firestore.collection(Constants.COLLECTION_PARKING_LOTS).document(lotId)
                .collection(Constants.COLLECTION_SLOTS).document(slot.getId())
                .set(slot)
                .addOnSuccessListener(aVoid -> result.setValue(Result.success(null)))
                .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage(), null)));
        return result;
    }

    public LiveData<Result<Void>> deleteSlot(String lotId, String slotId) {
        MutableLiveData<Result<Void>> result = new MutableLiveData<>();
        result.setValue(Result.loading(null));
        firestore.collection(Constants.COLLECTION_PARKING_LOTS).document(lotId)
                .collection(Constants.COLLECTION_SLOTS).document(slotId)
                .delete()
                .addOnSuccessListener(aVoid -> result.setValue(Result.success(null)))
                .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage(), null)));
        return result;
    }

    public LiveData<Result<List<Booking>>> getBookingsForUser(String userId) {
        MutableLiveData<Result<List<Booking>>> result = new MutableLiveData<>();
        result.setValue(Result.loading(null));
        firestore.collection(Constants.COLLECTION_BOOKINGS)
                .whereEqualTo("userId", userId)
                .orderBy("startTime", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Booking> bookings = queryDocumentSnapshots.toObjects(Booking.class);
                    result.setValue(Result.success(bookings));
                })
                .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage(), null)));
        return result;
    }
}