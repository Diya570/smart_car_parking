package com.smartparking.app.ui.fragments.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.smartparking.app.data.repository.AuthRepository;
import com.smartparking.app.data.source.Result;

public class LoginViewModel extends ViewModel {
    private final AuthRepository authRepository;
    // This LiveData will hold the result of the login/sign-in attempt.
    private final MutableLiveData<Result<FirebaseUser>> loginResult = new MutableLiveData<>();

    public LoginViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    // The Fragment will observe this LiveData.
    public LiveData<Result<FirebaseUser>> getLoginResult() {
        return loginResult;
    }

    public void login(String email, String password) {
        loginResult.setValue(Result.loading(null));
        authRepository.login(email, password)
                .addOnSuccessListener(authResult -> loginResult.setValue(Result.success(authResult.getUser())))
                .addOnFailureListener(e -> loginResult.setValue(Result.error(e.getMessage(), null)));
    }

    public void firebaseAuthWithGoogle(String idToken) {
        loginResult.setValue(Result.loading(null));
        authRepository.firebaseAuthWithGoogle(idToken)
                .addOnSuccessListener(authResult -> {
                    // Check if it's a new user to create their Firestore document
                    boolean isNewUser = authResult.getAdditionalUserInfo() != null && authResult.getAdditionalUserInfo().isNewUser();
                    if (isNewUser) {
                        authRepository.createNewUserInFirestore(authResult.getUser(), null, null, null)
                                .addOnSuccessListener(aVoid -> loginResult.setValue(Result.success(authResult.getUser())))
                                .addOnFailureListener(e -> loginResult.setValue(Result.error(e.getMessage(), null)));
                    } else {
                        loginResult.setValue(Result.success(authResult.getUser()));
                    }
                })
                .addOnFailureListener(e -> loginResult.setValue(Result.error(e.getMessage(), null)));
    }
}