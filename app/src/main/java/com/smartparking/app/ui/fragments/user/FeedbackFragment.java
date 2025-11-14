package com.smartparking.app.ui.fragments.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;
import com.smartparking.app.databinding.FragmentFeedbackBinding;
import com.smartparking.app.ui.base.BaseFragment;

public class FeedbackFragment extends BaseFragment<FragmentFeedbackBinding, FeedbackViewModel> {

    // Assume lotId is passed as an argument
    private String lotId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // lotId = FeedbackFragmentArgs.fromBundle(getArguments()).getLotId();
        }
    }

    @NonNull
    @Override
    protected FragmentFeedbackBinding createBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentFeedbackBinding.inflate(inflater, container, false);
    }

    @NonNull
    @Override
    protected Class<FeedbackViewModel> getViewModelClass() {
        return FeedbackViewModel.class;
    }

    @Override
    protected void setupViews() {
        binding.submitButton.setOnClickListener(v -> {
            float rating = binding.ratingBar.getRating();
            String comment = binding.commentEditText.getText().toString().trim();
            if (rating == 0) {
                Toast.makeText(getContext(), "Please provide a rating.", Toast.LENGTH_SHORT).show();
                return;
            }
            // A dummy lotId for now, as it's not passed yet
            lotId = "dummyLotId123";
            viewModel.submitFeedback(lotId, rating, comment);
        });
    }

    @Override
    protected void observeData() {
        viewModel.getFeedbackResult().observe(getViewLifecycleOwner(), result -> {
            switch (result.status) {
                case LOADING:
                    binding.submitButton.setEnabled(false);
                    // Show progress
                    break;
                case SUCCESS:
                    binding.submitButton.setEnabled(true);
                    Toast.makeText(getContext(), "Thank you for your feedback!", Toast.LENGTH_LONG).show();
                    Navigation.findNavController(requireView()).popBackStack();
                    break;
                case ERROR:
                    binding.submitButton.setEnabled(true);
                    Toast.makeText(getContext(), "Error: " + result.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }
}