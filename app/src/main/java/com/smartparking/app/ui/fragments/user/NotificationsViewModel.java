package com.smartparking.app.ui.fragments.user;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.smartparking.app.data.model.Announcement;
import com.smartparking.app.data.repository.AdminRepository;
import com.smartparking.app.data.source.Result;
import java.util.Date; // You might need this import
import java.util.List;

public class NotificationsViewModel extends ViewModel {

    private final AdminRepository adminRepository;
    private LiveData<Result<List<Announcement>>> announcementsResult;

    // The constructor is now empty again
    public NotificationsViewModel() {
        this.adminRepository = new AdminRepository();
        fetchAnnouncements();
    }

    public LiveData<Result<List<Announcement>>> getAnnouncementsResult() {
        return announcementsResult;
    }

    private void fetchAnnouncements() {
        // This calls the version of getAllAnnouncements that takes NO arguments.
        // We will create this in the next step.
        announcementsResult = adminRepository.getAllAnnouncements();
    }
}