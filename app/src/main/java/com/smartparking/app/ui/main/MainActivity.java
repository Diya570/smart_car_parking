package com.smartparking.app.ui.main;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.smartparking.app.R;
import com.smartparking.app.data.model.User;
import com.smartparking.app.databinding.ActivityMainBinding;
import com.smartparking.app.ui.viewmodel.ViewModelFactory;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private MainViewModel viewModel;
    private NavController navController;

    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseAuth firebaseAuth;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, R.string.notification_permission_required, Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        viewModel = new ViewModelProvider(this, new ViewModelFactory()).get(MainViewModel.class);

        setupNavigation();
        setupAuthStateListener();
        observeViewModel();
        askNotificationPermission();
    }

    private void setupAuthStateListener() {
        authStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            NavDestination currentDestination = navController.getCurrentDestination();

            // Check if the user is currently on one of the authentication screens
            boolean onAuthScreen = currentDestination != null &&
                    (currentDestination.getId() == R.id.loginFragment ||
                            currentDestination.getId() == R.id.registerFragment ||
                            currentDestination.getId() == R.id.forgotPasswordFragment);

            if (user != null) {
                // --- User is logged in ---

                // THE FIX IS HERE:
                // Only validate the session if the user is already INSIDE the main app.
                // If they are on an auth screen, we let the Login/Register fragments
                // handle the navigation to prevent the race condition.
                if (!onAuthScreen) {
                    viewModel.validateUserSession();
                }
            } else {
                // --- User is logged out ---

                // If the user is logged out AND they are not on an auth screen,
                // send them back to the login flow.
                if (!onAuthScreen) {
                    navController.navigate(R.id.auth_flow);
                }
            }
        };
    }

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(binding.bottomNav, navController);

        binding.bottomNav.setOnNavigationItemReselectedListener(item -> {
            NavDestination currentDestination = navController.getCurrentDestination();
            if (currentDestination != null && item.getItemId() == currentDestination.getId()) {
                return;
            }
            navController.popBackStack(item.getItemId(), false);
        });

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            boolean isAuthDestination = destination.getId() == R.id.loginFragment ||
                    destination.getId() == R.id.registerFragment ||
                    destination.getId() == R.id.forgotPasswordFragment;

            binding.bottomNav.setVisibility(isAuthDestination ? View.GONE : View.VISIBLE);
        });
    }

    private void observeViewModel() {
        viewModel.getSessionValidationStatus().observe(this, status -> {
            if (status == MainViewModel.ValidationStatus.VALID) {
                viewModel.fetchUserDetails();
                viewModel.updateFcmTokenAndSubscribe();
            } else if (status == MainViewModel.ValidationStatus.INVALID_DELETED_USER) {
                Toast.makeText(this, "Your account was not found. Please log in again.", Toast.LENGTH_LONG).show();
                viewModel.forceSignOut();
            }
        });

        viewModel.getUserDetails().observe(this, result -> {
            if (result.status == com.smartparking.app.data.source.Result.Status.SUCCESS) {
                User user = result.data;
                if (user != null && "admin".equals(user.getRole())) {
                    binding.bottomNav.getMenu().findItem(R.id.adminDashboardFragment).setVisible(true);
                } else {
                    binding.bottomNav.getMenu().findItem(R.id.adminDashboardFragment).setVisible(false);
                }
            }
        });
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}