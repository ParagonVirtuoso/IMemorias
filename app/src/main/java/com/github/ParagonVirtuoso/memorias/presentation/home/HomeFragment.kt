package com.github.ParagonVirtuoso.memorias.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.github.ParagonVirtuoso.memorias.R
import com.github.ParagonVirtuoso.memorias.databinding.FragmentHomeBinding
import com.github.ParagonVirtuoso.memorias.domain.model.User
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment(), MenuProvider {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupMenu()
        observeViewModel()
    }

    private fun setupViews() {
        binding.btnSignOut.setOnClickListener {
            viewModel.signOut()
        }
    }

    private fun setupMenu() {
        binding.toolbar.apply {
            requireActivity().addMenuProvider(this@HomeFragment, viewLifecycleOwner, Lifecycle.State.RESUMED)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                handleUiState(state)
            }
        }
    }

    private fun handleUiState(state: HomeUiState) {
        binding.loadingProgressBar.isVisible = state is HomeUiState.Loading

        when (state) {
            is HomeUiState.Success -> updateUi(state.user)
            is HomeUiState.Error -> showError(state.message)
            is HomeUiState.SignedOut -> navigateToAuth()
            else -> Unit
        }
    }

    private fun updateUi(user: User) {
        binding.tvWelcome.text = getString(R.string.auth_success, user.name)
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToAuth() {
        findNavController().navigate(R.id.action_home_to_auth)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.home_menu, menu)
        updateThemeIcon(menu.findItem(R.id.action_theme))
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.action_theme -> {
                toggleTheme()
                true
            }
            else -> false
        }
    }

    private fun toggleTheme() {
        val newMode = if (isNightMode()) {
            AppCompatDelegate.MODE_NIGHT_NO
        } else {
            AppCompatDelegate.MODE_NIGHT_YES
        }
        AppCompatDelegate.setDefaultNightMode(newMode)
    }

    private fun updateThemeIcon(menuItem: MenuItem) {
        menuItem.setIcon(
            if (isNightMode()) R.drawable.ic_light_mode
            else R.drawable.ic_dark_mode
        )
    }

    private fun isNightMode(): Boolean {
        return AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 