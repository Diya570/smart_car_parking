package com.smartparking.app.ui.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewbinding.ViewBinding;
import com.smartparking.app.ui.viewmodel.ViewModelFactory;

/**
 * An abstract base class for Fragments to reduce boilerplate code for ViewBinding and ViewModel initialization.
 * It uses a ViewModelFactory to allow for dependency injection into ViewModels.
 *
 * @param <VB> The type of the ViewBinding class generated for the fragment's layout.
 * @param <VM> The type of the ViewModel class associated with the fragment.
 */
public abstract class BaseFragment<VB extends ViewBinding, VM extends ViewModel> extends Fragment {

    protected VB binding;
    protected VM viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = createBinding(inflater, container);
        // Use the custom ViewModelFactory to create the ViewModel instance
        viewModel = new ViewModelProvider(this, new ViewModelFactory()).get(getViewModelClass());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews();
        observeData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Set binding to null to avoid memory leaks when the fragment's view is destroyed.
        binding = null;
    }

    /**
     * This method is responsible for creating the ViewBinding instance.
     * Implemented by subclasses.
     * e.g., return FragmentLoginBinding.inflate(inflater, container, false);
     */
    @NonNull
    protected abstract VB createBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container);

    /**
     * This method returns the class of the ViewModel.
     * Implemented by subclasses.
     * e.g., return LoginViewModel.class;
     */
    @NonNull
    protected abstract Class<VM> getViewModelClass();

    /**
     * Set up your views, listeners, and adapters here.
     * This is called in onViewCreated.
     */
    protected abstract void setupViews();

    /**
     * Observe LiveData from your ViewModel here.
     * This is called in onViewCreated.
     */
    protected abstract void observeData();
}