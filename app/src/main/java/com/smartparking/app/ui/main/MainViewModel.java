package com.smartparking.app.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.smartparking.app.data.model.User;
import com.smartparking.app.data.repository.AuthRepository;
import com.smartparking.app.data.repository.UserRepository;
import com.smartparking.app.data.source.Result;
import com.smartparking.app.utils.SingleLiveEvent;

public class MainViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final AuthRepository authRepository;
    private LiveData<Result<User>> userDetails;

    // A special LiveData that only fires once, perfect for navigation or single-action events.
    private final SingleLiveEvent<ValidationStatus> sessionValidationStatus = new SingleLiveEvent<>();

    public enum ValidationStatus {
        VALID,
        INVALID_DELETED_USER
    }

    public MainViewModel(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.authRepository = new AuthRepository();
    }

    public LiveData<ValidationStatus> getSessionValidationStatus() {
        return sessionValidationStatus;
    }

    /**
     * Validates the current user session by checking if their corresponding document exists in Firestore.
     */
    public void validateUserSession() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // If there's no user in Auth, the session is definitely invalid.
            sessionValidationStatus.setValue(ValidationStatus.INVALID_DELETED_USER);
            return;
        }

        // Check if the user document exists in Firestore.
        userRepository.getUserDocument(currentUser.getUid()).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                // Document exists, so the session is valid.
                sessionValidationStatus.setValue(ValidationStatus.VALID);
            } else {
                // Document does not exist. This is a "ghost" session and is invalid.
                sessionValidationStatus.setValue(ValidationStatus.INVALID_DELETED_USER);
            }
        });
    }

    /**
     * Forces a sign-out by clearing the local auth state.
     */
    public void forceSignOut() {
        authRepository.signOut();
    }

    public LiveData<Result<User>> getUserDetails() {
        if (userDetails == null) {
            userDetails = userRepository.getUserDetails();
        }
        return userDetails;
    }

    public void fetchUserDetails() {
        userRepository.fetchUserDetails();
    }

    public void updateFcmTokenAndSubscribe() {
        userRepository.updateFcmToken();
        userRepository.subscribeToAnnouncementsTopic();
    }
}