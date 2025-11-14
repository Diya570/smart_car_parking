package com.smartparking.app.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.smartparking.app.constants.Constants;
import com.smartparking.app.data.model.User;
import com.smartparking.app.data.model.Vehicle;
import com.smartparking.app.data.source.Result;
import java.util.Collections;

public class AuthRepository {
    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;

    public AuthRepository() {
        this.auth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
    }

    // We now return the Task directly, and the ViewModel will wrap it in LiveData.
    public Task<AuthResult> login(String email, String password) {
        return auth.signInWithEmailAndPassword(email, password);
    }

    public Task<AuthResult> firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        return auth.signInWithCredential(credential);
    }

    public Task<AuthResult> register(String name, String email, String password) {
        return auth.createUserWithEmailAndPassword(email, password);
    }

    // This method now returns a Task<Void> to signal completion.
    public Task<Void> createNewUserInFirestore(FirebaseUser firebaseUser, String vehiclePlate, String vehicleModel, String displayName) {
        if (firebaseUser == null) {
            return null;
        }

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build();

        return firebaseUser.updateProfile(profileUpdates).continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }

            User newUser = new User();
            newUser.setUid(firebaseUser.getUid());
            newUser.setDisplayName(displayName);
            newUser.setEmail(firebaseUser.getEmail());
            newUser.setRole("user");
            newUser.setFcmTokens(Collections.emptyList());

            if (vehiclePlate != null && !vehiclePlate.isEmpty()) {
                // Now we save both the plate and the model
                Vehicle vehicle = new Vehicle(vehiclePlate, vehicleModel);
                newUser.setVehicle(vehicle);
            }

            return firestore.collection(Constants.COLLECTION_USERS).document(firebaseUser.getUid()).set(newUser);
        });
    }

    public Task<Void> sendPasswordResetEmail(String email) {
        return auth.sendPasswordResetEmail(email);
    }

    public void logout() {
        auth.signOut();
    }
    public void signOut() {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(Constants.FCM_TOPIC_ANNOUNCEMENTS);
        auth.signOut();
    }
}