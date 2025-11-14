package com.smartparking.app.ui.fragments.admin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.smartparking.app.data.model.User;
import com.smartparking.app.data.repository.UserRepository;
import com.smartparking.app.data.source.Result;

public class AdminBookingDetailsViewModel extends ViewModel {
    private final UserRepository userRepository;
    private LiveData<Result<User>> userDetails;

    public AdminBookingDetailsViewModel() {
        this.userRepository = new UserRepository();
    }

    public LiveData<Result<User>> getUserDetails() {
        return userDetails;
    }

    public void fetchUserDetails(String userId) {
        // This re-uses the existing method from UserRepository
        //userRepository.fetchUserDetails(userId);
        userDetails = userRepository.getUserDetailsById(userId);
    }
}