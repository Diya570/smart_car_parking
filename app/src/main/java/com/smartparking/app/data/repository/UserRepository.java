package com.smartparking.app.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.smartparking.app.constants.Constants;
import com.smartparking.app.data.model.User;
import com.smartparking.app.data.source.Result;
import com.smartparking.app.data.model.Vehicle;

import com.google.firebase.auth.UserProfileChangeRequest;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

public class UserRepository {
    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;
    private final MutableLiveData<Result<User>> userDetailsResult = new MutableLiveData<>();

    public UserRepository() {
        this.auth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
    }

    /**
     * Returns a Task to get the user's document from Firestore.
     * This is used for quick existence checks during session validation.
     * @param uid The user's unique ID.
     * @return A Task that resolves with the DocumentSnapshot.
     */
    public Task<DocumentSnapshot> getUserDocument(String uid) {
        return firestore.collection(Constants.COLLECTION_USERS).document(uid).get();
    }

    /**
     * Returns a LiveData object that the UI can observe for the full user profile details.
     */
    public LiveData<Result<User>> getUserDetails() {
        return userDetailsResult;
    }

    /**
     * Fetches the full user profile from Firestore and updates the userDetailsResult LiveData.
     */
    public void fetchUserDetails() {
        userDetailsResult.setValue(Result.loading(null));

        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser == null) {
            userDetailsResult.setValue(Result.error("User not logged in", null));
            return;
        }

        getUserDocument(firebaseUser.getUid()).addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                User user = documentSnapshot.toObject(User.class);
                userDetailsResult.setValue(Result.success(user));
            } else {
                // This case can happen in a ghost session, where Auth is valid but Firestore doc is gone.
                userDetailsResult.setValue(Result.error("User profile not found in Firestore.", null));
            }
        }).addOnFailureListener(e -> userDetailsResult.setValue(Result.error(e.getMessage(), null)));
    }

    /**
     * Retrieves the current FCM token and adds it to the user's document in Firestore.
     */
    public void updateFcmToken() {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser == null) return;

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Timber.w(task.getException(), "Fetching FCM registration token failed");
                return;
            }
            String token = task.getResult();
            Timber.d("FCM Token retrieved: %s", token);
            firestore.collection(Constants.COLLECTION_USERS).document(firebaseUser.getUid())
                    .update("fcmTokens", FieldValue.arrayUnion(token))
                    .addOnSuccessListener(aVoid -> Timber.d("FCM token updated in Firestore."))
                    .addOnFailureListener(e -> Timber.e(e, "Failed to update FCM token in Firestore."));
        });
    }

    /**
     * Subscribes the user to the general announcements topic for push notifications.
     */
    public void subscribeToAnnouncementsTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic(Constants.FCM_TOPIC_ANNOUNCEMENTS)
                .addOnCompleteListener(task -> {
                    String msg = task.isSuccessful() ? "Subscribed to announcements topic" : "Failed to subscribe to announcements topic";
                    Timber.d(msg);
                });
    }
    public LiveData<Result<Void>> updateUserProfile(String newName, Vehicle newVehicle) {
        MutableLiveData<Result<Void>> result = new MutableLiveData<>();
        result.setValue(Result.loading(null));

        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser == null) {
            result.setValue(Result.error("User not logged in", null));
            return result;
        }

        // Update display name in Firebase Auth
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build();

        firebaseUser.updateProfile(profileUpdates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // If Auth update is successful, update data in Firestore
                Map<String, Object> updates = new HashMap<>();
                updates.put("displayName", newName);
                updates.put("vehicle", newVehicle);

                firestore.collection(Constants.COLLECTION_USERS).document(firebaseUser.getUid())
                        .update(updates)
                        .addOnSuccessListener(aVoid -> result.setValue(Result.success(null)))
                        .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage(), null)));
            } else {
                result.setValue(Result.error(task.getException().getMessage(), null));
            }
        });

        return result;
    }

    public LiveData<Result<User>> getUserDetailsById(String userId) {
        MutableLiveData<Result<User>> userResult = new MutableLiveData<>();
        userResult.setValue(Result.loading(null));
        getUserDocument(userId).addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                User user = documentSnapshot.toObject(User.class);
                userResult.setValue(Result.success(user));
            } else {
                userResult.setValue(Result.error("User profile not found.", null));
            }
        }).addOnFailureListener(e -> userResult.setValue(Result.error(e.getMessage(), null)));
        return userResult;
    }

}