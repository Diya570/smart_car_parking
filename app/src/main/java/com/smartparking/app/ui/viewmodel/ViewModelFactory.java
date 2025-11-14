package com.smartparking.app.ui.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.smartparking.app.data.repository.AdminRepository;
import com.smartparking.app.data.repository.AuthRepository;
import com.smartparking.app.data.repository.BookingRepository;
import com.smartparking.app.data.repository.FeedbackRepository;
import com.smartparking.app.data.repository.ParkingRepository;
import com.smartparking.app.data.repository.UserRepository;
import com.smartparking.app.ui.fragments.admin.*;
import com.smartparking.app.ui.fragments.auth.*;
import com.smartparking.app.ui.fragments.user.*;
import com.smartparking.app.ui.main.MainViewModel;

import java.lang.reflect.InvocationTargetException;

public class ViewModelFactory implements ViewModelProvider.Factory {

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {

        // Main & Auth
        if (modelClass.isAssignableFrom(MainViewModel.class)) {
            return (T) new MainViewModel(new UserRepository());
        } else if (modelClass.isAssignableFrom(LoginViewModel.class)) {
            return (T) new LoginViewModel(new AuthRepository());
        } else if (modelClass.isAssignableFrom(RegisterViewModel.class)) {
            return (T) new RegisterViewModel(new AuthRepository());
        } else if (modelClass.isAssignableFrom(ForgotPasswordViewModel.class)) {
            return (T) new ForgotPasswordViewModel(new AuthRepository());
        }

        // User Flow
        else if (modelClass.isAssignableFrom(LotsListViewModel.class)) {
            return (T) new LotsListViewModel(new ParkingRepository());
        } else if (modelClass.isAssignableFrom(LotDetailsViewModel.class)) {
            return (T) new LotDetailsViewModel(new BookingRepository());
        } else if (modelClass.isAssignableFrom(SlotSelectionViewModel.class)) {
            return (T) new SlotSelectionViewModel(new ParkingRepository());
        } else if (modelClass.isAssignableFrom(BookingReviewViewModel.class)) {
            return (T) new BookingReviewViewModel(new BookingRepository());
        } else if (modelClass.isAssignableFrom(MyBookingsViewModel.class)) {
            return (T) new MyBookingsViewModel(new BookingRepository());
        } else if (modelClass.isAssignableFrom(ProfileViewModel.class)) {
            return (T) new ProfileViewModel(new UserRepository(), new AuthRepository());
        } else if (modelClass.isAssignableFrom(FeedbackViewModel.class)) {
            return (T) new FeedbackViewModel(new FeedbackRepository());
        }

        // Admin Flow
        else if (modelClass.isAssignableFrom(ManageLotViewModel.class)) {
            return (T) new ManageLotViewModel(new AdminRepository());
        }
        // THE FIX IS HERE: ManageSlotsViewModel now gets a ParkingRepository
        else if (modelClass.isAssignableFrom(ManageSlotsViewModel.class)) {
            return (T) new ManageSlotsViewModel(new ParkingRepository());
        }
        else if (modelClass.isAssignableFrom(AdminBookingsViewModel.class)) {
            return (T) new AdminBookingsViewModel(new AdminRepository());
        } else if (modelClass.isAssignableFrom(UsersViewModel.class)) {
            return (T) new UsersViewModel(new AdminRepository());
        } else if (modelClass.isAssignableFrom(AnnouncementsViewModel.class)) {
            return (T) new AnnouncementsViewModel(new AdminRepository());
        } else if (modelClass.isAssignableFrom(ScanQrViewModel.class)) {
            return (T) new ScanQrViewModel(new AdminRepository());
        } else if (modelClass.isAssignableFrom(ReportsViewModel.class)) {
            return (T) new ReportsViewModel(new AdminRepository());
        }else if (modelClass.isAssignableFrom(BookingReviewViewModel.class)) {
            return (T) new BookingReviewViewModel(new BookingRepository());
        } else if (modelClass.isAssignableFrom(BookingDetailsViewModel.class)) {
            return (T) new BookingDetailsViewModel();

        } else if (modelClass.isAssignableFrom(MyBookingsViewModel.class)) {
            return (T) new MyBookingsViewModel(new BookingRepository());
        }

        // Fallback for ViewModels with no constructor arguments
        try {
            return modelClass.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException("Cannot create an instance of " + modelClass, e);
        }
    }
}