package com.smartparking.app.ui.fragments.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.smartparking.app.databinding.FragmentUsersBinding;
import com.smartparking.app.ui.adapters.UsersAdapter;
import com.smartparking.app.ui.base.BaseFragment;

public class UsersFragment extends BaseFragment<FragmentUsersBinding, UsersViewModel> {

    private UsersAdapter adapter;

    @NonNull
    @Override
    protected FragmentUsersBinding createBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentUsersBinding.inflate(inflater, container, false);
    }

    @NonNull
    @Override
    protected Class<UsersViewModel> getViewModelClass() {
        return UsersViewModel.class;
    }

    @Override
    protected void setupViews() {
        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        adapter = new UsersAdapter(user -> {
            // Navigate to the user details screen
            UsersFragmentDirections.ActionUsersFragmentToAdminUserDetailsFragment action =
                    UsersFragmentDirections.actionUsersFragmentToAdminUserDetailsFragment(user);
            Navigation.findNavController(requireView()).navigate(action);
        });

        binding.recyclerViewUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewUsers.setAdapter(adapter);
        viewModel.fetchAllUsers();
    }

    @Override
    protected void observeData() {
        viewModel.getAllUsersResult().observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;
            switch (result.status) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.recyclerViewUsers.setVisibility(View.GONE);
                    break;
                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.recyclerViewUsers.setVisibility(View.VISIBLE);
                    if (result.data != null) {
                        adapter.setUsers(result.data);
                    }
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.textError.setText("Error: " + result.message);
                    binding.textError.setVisibility(View.VISIBLE);
                    break;
            }
        });
    }
}