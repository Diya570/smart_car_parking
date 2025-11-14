package com.smartparking.app.ui.fragments.auth;

import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.navigation.Navigation;
import com.smartparking.app.R;
import com.smartparking.app.databinding.FragmentForgotPasswordBinding;
import com.smartparking.app.ui.base.BaseFragment;

public class ForgotPasswordFragment extends BaseFragment<FragmentForgotPasswordBinding, ForgotPasswordViewModel> {

    @NonNull
    @Override
    protected FragmentForgotPasswordBinding createBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentForgotPasswordBinding.inflate(inflater, container, false);
    }

    @NonNull
    @Override
    protected Class<ForgotPasswordViewModel> getViewModelClass() {
        return ForgotPasswordViewModel.class;
    }

    @Override
    protected void setupViews() {
        binding.sendResetEmailButton.setOnClickListener(v -> {
            String email = binding.emailEditText.getText().toString().trim();
            if (email.isEmpty()) {
                binding.emailLayout.setError("Email cannot be empty");
                return;
            }
            binding.emailLayout.setError(null);
            viewModel.sendPasswordResetEmail(email);
        });

        // ✅ Handle back to login navigation
        String fullText = "Remember your password? Log in";
        SpannableString spannable = new SpannableString(fullText);
        int start = fullText.indexOf("Log in");

        // Make "Log in" green and bold
        spannable.setSpan(
                new ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.brand_green)),
                start, fullText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new StyleSpan(Typeface.BOLD),
                start, fullText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        binding.backToLoginText.setText(spannable);

        // On click, navigate back to login fragment
        binding.backToLoginText.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_forgotPasswordFragment_to_loginFragment)
        );
    }



    @Override
    protected void observeData() {
        viewModel.getResetEmailResult().observe(getViewLifecycleOwner(), result -> {
            switch (result.status) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.sendResetEmailButton.setEnabled(false);
                    break;
                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.sendResetEmailButton.setEnabled(true);
                    Toast.makeText(getContext(), "Password reset email sent. Please check your inbox.", Toast.LENGTH_LONG).show();
                    Navigation.findNavController(requireView()).popBackStack();
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.sendResetEmailButton.setEnabled(true);
                    Toast.makeText(getContext(), "Error: " + result.message, Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }
}