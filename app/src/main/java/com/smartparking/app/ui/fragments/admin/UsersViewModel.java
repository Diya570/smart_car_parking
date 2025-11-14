package com.smartparking.app.ui.fragments.admin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.smartparking.app.data.model.User;
import com.smartparking.app.data.repository.AdminRepository;
import com.smartparking.app.data.source.Result;
import java.util.List;

public class UsersViewModel extends ViewModel {
    private final AdminRepository adminRepository;
    private LiveData<Result<List<User>>> allUsersResult;

    public UsersViewModel(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public LiveData<Result<List<User>>> getAllUsersResult() {
        if (allUsersResult == null) {
            allUsersResult = adminRepository.getAllUsers();
        }
        return allUsersResult;
    }

    public void fetchAllUsers() {
        allUsersResult = adminRepository.getAllUsers();
    }
}