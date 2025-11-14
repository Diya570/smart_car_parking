package com.smartparking.app.ui.fragments.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.smartparking.app.data.repository.AuthRepository;
import com.smartparking.app.data.source.Result;

public class ForgotPasswordViewModel extends ViewModel {
    private final AuthRepository authRepository;
    // We change this to a MutableLiveData so we can post updates to it.
    private final MutableLiveData<Result<Void>> resetEmailResult = new MutableLiveData<>();

    public ForgotPasswordViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public LiveData<Result<Void>> getResetEmailResult() {
        return resetEmailResult;
    }

    public void sendPasswordResetEmail(String email) {
        // Post the LOADING state immediately.
        resetEmailResult.setValue(Result.loading(null));

        // Call the repository method, which now returns a Task.
        authRepository.sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid -> {
                    // If the task is successful, post the SUCCESS state.
                    resetEmailResult.setValue(Result.success(null));
                })
                .addOnFailureListener(e -> {
                    // If the task fails, post the ERROR state with the message.
                    resetEmailResult.setValue(Result.error(e.getMessage(), null));
                });
    }
}