package com.smartparking.app.ui.fragments.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;
import com.smartparking.app.R;
import com.smartparking.app.databinding.FragmentRegisterBinding;
import com.smartparking.app.ui.base.BaseFragment;

public class RegisterFragment extends BaseFragment<FragmentRegisterBinding, RegisterViewModel> {

    @NonNull
    @Override
    protected FragmentRegisterBinding createBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentRegisterBinding.inflate(inflater, container, false);
    }

    @NonNull
    @Override
    protected Class<RegisterViewModel> getViewModelClass() {
        return RegisterViewModel.class;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Navigation.findNavController(requireView()).popBackStack();
            }
        });
    }

    @Override
    protected void setupViews() {
        binding.registerButton.setOnClickListener(v -> {
            String passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$";
            String name = binding.nameEditText.getText().toString().trim();
            String email = binding.emailEditText.getText().toString().trim();
            String password = binding.passwordEditText.getText().toString().trim();
            String vehiclePlate = binding.vehiclePlateEditText.getText().toString().trim();
            String vehicleModel = binding.vehicleModelEditText.getText().toString().trim();


            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || vehiclePlate.isEmpty()) {
                Toast.makeText(getContext(), "All fields are required.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!password.matches(passwordPattern)) {
                Toast.makeText(getContext(), "Password must be at least 8 characters long and include one uppercase letter, one lowercase letter, one number, and one special character.", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.register(name, email, password, vehiclePlate, vehicleModel);
        });

        // THE FIX IS HERE: Add listeners for the new buttons.
        // Both will navigate back to the Login screen.
        View.OnClickListener backToLoginListener = v -> Navigation.findNavController(v).popBackStack();
        binding.loginText.setOnClickListener(backToLoginListener);
        binding.googleSignInButton.setOnClickListener(backToLoginListener);
    }

    @Override
    protected void observeData() {
        viewModel.getRegisterResult().observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;
            switch (result.status) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.registerButton.setEnabled(false);
                    break;
                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.registerButton.setEnabled(true);
                    if (result.data != null) {
                        Toast.makeText(getContext(), "Registration Successful", Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(requireView()).navigate(R.id.action_register_successful);
                    }
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.registerButton.setEnabled(true);
                    Toast.makeText(getContext(), "Registration Failed: " + result.message, Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }
}