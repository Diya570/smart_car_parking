package com.smartparking.app.ui.fragments.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.auth.FirebaseUser;
import com.smartparking.app.data.repository.AuthRepository;
import com.smartparking.app.data.source.Result;

public class RegisterViewModel extends ViewModel {
    private final AuthRepository authRepository;
    private final MutableLiveData<Result<FirebaseUser>> registerResult = new MutableLiveData<>();

    public RegisterViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public LiveData<Result<FirebaseUser>> getRegisterResult() {
        return registerResult;
    }

    public void register(String name, String email, String password, String vehiclePlate, String vehicleModel) {
        registerResult.setValue(Result.loading(null));
        authRepository.register(name, email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser != null) {
                        authRepository.createNewUserInFirestore(firebaseUser, vehiclePlate, vehicleModel, name)
                                .addOnSuccessListener(aVoid -> registerResult.setValue(Result.success(firebaseUser)))
                                .addOnFailureListener(e -> registerResult.setValue(Result.error(e.getMessage(), null)));
                    } else {
                        registerResult.setValue(Result.error("Failed to get user after creation.", null));
                    }
                })
                .addOnFailureListener(e -> registerResult.setValue(Result.error(e.getMessage(), null)));
    }
}