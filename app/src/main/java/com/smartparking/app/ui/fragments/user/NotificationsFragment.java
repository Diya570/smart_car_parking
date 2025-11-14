package com.smartparking.app.ui.fragments.user;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.smartparking.app.databinding.FragmentNotificationsBinding;
import com.smartparking.app.ui.adapters.NotificationsAdapter;
import com.smartparking.app.ui.base.BaseFragment;

public class NotificationsFragment extends BaseFragment<FragmentNotificationsBinding, NotificationsViewModel> {

    private NotificationsAdapter adapter;

    @NonNull
    @Override
    protected FragmentNotificationsBinding createBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentNotificationsBinding.inflate(inflater, container, false);
    }

    @NonNull
    @Override
    protected Class<NotificationsViewModel> getViewModelClass() {
        return NotificationsViewModel.class;
    }

    @Override
    protected void setupViews() {
        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).popBackStack());
        adapter = new NotificationsAdapter();
        binding.recyclerViewNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewNotifications.setAdapter(adapter);
    }

    @Override
    protected void observeData() {
        viewModel.getAnnouncementsResult().observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;
            switch (result.status) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    if (result.data != null) {
                        adapter.setAnnouncements(result.data);
                    }
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error: " + result.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }
}