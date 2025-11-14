package com.smartparking.app.ui.fragments.auth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.smartparking.app.R;
import com.smartparking.app.databinding.FragmentLoginBinding;
import com.smartparking.app.ui.base.BaseFragment;

import timber.log.Timber;

public class LoginFragment extends BaseFragment<FragmentLoginBinding, LoginViewModel> {

    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @NonNull
    @Override
    protected FragmentLoginBinding createBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentLoginBinding.inflate(inflater, container, false);
    }

    @NonNull
    @Override
    protected Class<LoginViewModel> getViewModelClass() {
        return LoginViewModel.class;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupGoogleSignIn();
    }

    @Override
    protected void setupViews() {
        binding.loginButton.setOnClickListener(v -> {
            String passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$";
            String email = binding.emailEditText.getText().toString().trim();
            String password = binding.passwordEditText.getText().toString().trim();
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), R.string.field_cannot_be_empty, Toast.LENGTH_SHORT).show();
                return;
            }
            if (!password.matches(passwordPattern)) {
                Toast.makeText(getContext(), "Password must be at least 8 characters long and include one uppercase letter, one lowercase letter, one number, and one special character.", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.login(email, password);
        });

        binding.googleSignInButton.setOnClickListener(v -> signInWithGoogle());

        binding.registerText.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_registerFragment));

        binding.forgotPasswordText.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_forgotPasswordFragment));
    }

    @Override
    protected void observeData() {
        // THE FIX IS HERE: We call getLoginResult() instead of the old method.
        viewModel.getLoginResult().observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;
            switch (result.status) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.loginButton.setEnabled(false);
                    binding.googleSignInButton.setEnabled(false);
                    break;
                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.loginButton.setEnabled(true);
                    binding.googleSignInButton.setEnabled(true);
                    if (result.data != null) {
                        Navigation.findNavController(requireView()).navigate(R.id.action_login_successful);
                    }
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.loginButton.setEnabled(true);
                    binding.googleSignInButton.setEnabled(true);
                    Toast.makeText(getContext(), "Login Failed: " + result.message, Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        if (getContext() != null) {
            googleSignInClient = GoogleSignIn.getClient(getContext(), gso);
        }

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            Timber.d("firebaseAuthWithGoogle:" + account.getId());
                            if(account.getIdToken() != null){
                                viewModel.firebaseAuthWithGoogle(account.getIdToken());
                            } else {
                                Toast.makeText(getContext(), "Google Sign-In Failed: No ID Token", Toast.LENGTH_SHORT).show();
                            }
                        } catch (ApiException e) {
                            Timber.w(e, "Google sign in failed");
                            Toast.makeText(getContext(), "Google Sign-In Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }
}