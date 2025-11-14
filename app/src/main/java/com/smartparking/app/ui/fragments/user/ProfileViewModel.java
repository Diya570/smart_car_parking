package com.smartparking.app.ui.fragments.user;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.smartparking.app.data.model.User;
import com.smartparking.app.data.model.Vehicle;
import com.smartparking.app.data.repository.AuthRepository;
import com.smartparking.app.data.repository.UserRepository;
import com.smartparking.app.data.source.Result;

public class ProfileViewModel extends ViewModel {
    private final UserRepository userRepository;
    private final AuthRepository authRepository;
    private LiveData<Result<User>> userDetails;
    private final MutableLiveData<Result<Void>> updateResult = new MutableLiveData<>();

    public ProfileViewModel(UserRepository userRepository, AuthRepository authRepository) {
        this.userRepository = userRepository;
        this.authRepository = authRepository;
    }

    /**
     * Gets the user details and triggers a fetch if the data hasn't been loaded yet.
     * @return LiveData containing the user's profile information.
     */
    public LiveData<Result<User>> getUserDetails() {
        if (userDetails == null) {
            // THE FIX IS HERE: We get the LiveData object from the repository
            // AND immediately trigger the fetch operation.
            userDetails = userRepository.getUserDetails();
            fetchUserDetails();
        }
        return userDetails;
    }

    public LiveData<Result<Void>> getUpdateResult() {
        return updateResult;
    }

    /**
     * Public method to allow the UI to refresh the user data if needed.
     */
    public void fetchUserDetails() {
        userRepository.fetchUserDetails();
    }

    /**
     * Triggers the repository to update the user's profile information.
     */
    public void updateProfile(String name, String model, String plate) {
        Vehicle newVehicle = new Vehicle(plate, model);
        updateResult.setValue(Result.loading(null));
        userRepository.updateUserProfile(name, newVehicle).observeForever(result -> {
            updateResult.setValue(result);
        });
    }

    /**
     * Handles the user logout process.
     */
    public void logout() {
        authRepository.signOut();
    }
}