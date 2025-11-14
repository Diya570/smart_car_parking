package com.smartparking.app.ui.fragments.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;
import com.smartparking.app.databinding.FragmentAnnouncementsBinding;
import com.smartparking.app.ui.base.BaseFragment;

public class AnnouncementsFragment extends BaseFragment<FragmentAnnouncementsBinding, AnnouncementsViewModel> {

    @NonNull
    @Override
    protected FragmentAnnouncementsBinding createBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentAnnouncementsBinding.inflate(inflater, container, false);
    }

    @NonNull
    @Override
    protected Class<AnnouncementsViewModel> getViewModelClass() {
        return AnnouncementsViewModel.class;
    }

    @Override
    protected void setupViews() {
        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        binding.sendButton.setOnClickListener(v -> {
            String title = binding.titleEditText.getText().toString().trim();
            String message = binding.messageEditText.getText().toString().trim();
            if (title.isEmpty() || message.isEmpty()) {
                Toast.makeText(getContext(), "Title and message cannot be empty.", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.sendAnnouncement(title, message);
        });
    }

    @Override
    protected void observeData() {
        viewModel.getSendResult().observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;
            switch (result.status) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.sendButton.setEnabled(false);
                    break;
                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.sendButton.setEnabled(true);
                    Toast.makeText(getContext(), "Announcement sent successfully!", Toast.LENGTH_LONG).show();
                    Navigation.findNavController(requireView()).popBackStack();
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.sendButton.setEnabled(true);
                    Toast.makeText(getContext(), "Error: " + result.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }
}