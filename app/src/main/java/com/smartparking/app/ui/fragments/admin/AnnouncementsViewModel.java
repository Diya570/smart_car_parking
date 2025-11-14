package com.smartparking.app.ui.fragments.admin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.smartparking.app.data.repository.AdminRepository;
import com.smartparking.app.data.source.Result;

public class AnnouncementsViewModel extends ViewModel {
    private final AdminRepository adminRepository;
    // THE FIX IS HERE: Change to MutableLiveData and initialize it.
    private final MutableLiveData<Result<Void>> sendResult = new MutableLiveData<>();

    public AnnouncementsViewModel(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public LiveData<Result<Void>> getSendResult() {
        return sendResult;
    }

    public void sendAnnouncement(String title, String message) {
        // Now, we observe the LiveData returned by the repository and post its value
        // to our ViewModel's LiveData. This is a robust pattern.
        sendResult.setValue(Result.loading(null));
        adminRepository.sendAnnouncement(title, message).observeForever(result -> {
            sendResult.setValue(result);
        });
    }
}