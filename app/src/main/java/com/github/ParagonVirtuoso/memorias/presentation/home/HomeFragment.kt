package com.github.ParagonVirtuoso.memorias.presentation.home

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.github.ParagonVirtuoso.memorias.R
import com.github.ParagonVirtuoso.memorias.databinding.FragmentHomeBinding
import com.github.ParagonVirtuoso.memorias.util.showErrorSnackbar
import com.github.ParagonVirtuoso.memorias.util.showSuccessSnackbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private var isMenuExpanded = false

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
        observeUiState()
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.home_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_theme -> {
                        viewModel.toggleTheme()
                        true
                    }
                    R.id.action_search -> {
                        navigateToSearch()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setupViews() {
        with(binding) {
            btnSearch.setOnClickListener { navigateToSearch() }
            btnPlaylists.setOnClickListener { navigateToPlaylists() }
            btnFavorites.setOnClickListener { navigateToFavorites() }
            fabMenu.setOnClickListener { toggleMenu() }
            fabAddMemory.setOnClickListener { navigateToAddMemory() }
            fabSignOut.setOnClickListener { showSignOutConfirmation() }
            
            (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        }
    }

    private fun toggleMenu() {
        isMenuExpanded = !isMenuExpanded
        if (isMenuExpanded) {
            showMenuOptions()
        } else {
            hideMenuOptions()
        }
    }

    private fun showMenuOptions() {
        with(binding) {
            fabMenu.animate()
                .rotation(90f)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .setDuration(300)
                .withStartAction {
                    fabMenu.setImageResource(R.drawable.ic_menu)
                }
                .withEndAction {
                    fabMenu.setImageResource(R.drawable.ic_close)
                    fabMenu.animate()
                        .rotation(180f)
                        .setInterpolator(AccelerateDecelerateInterpolator())
                        .setDuration(300)
                        .start()
                }
                .start()

            fabAddMemory.visibility = View.VISIBLE
            fabSignOut.visibility = View.VISIBLE
            
            fabAddMemory.alpha = 0f
            fabAddMemory.scaleX = 0f
            fabAddMemory.scaleY = 0f
            
            fabSignOut.alpha = 0f
            fabSignOut.scaleX = 0f
            fabSignOut.scaleY = 0f

            val animatorSet = AnimatorSet().apply {
                playTogether(
                    ObjectAnimator.ofFloat(fabAddMemory, "alpha", 0f, 1f),
                    ObjectAnimator.ofFloat(fabAddMemory, "scaleX", 0f, 1f),
                    ObjectAnimator.ofFloat(fabAddMemory, "scaleY", 0f, 1f),
                    ObjectAnimator.ofFloat(fabSignOut, "alpha", 0f, 1f),
                    ObjectAnimator.ofFloat(fabSignOut, "scaleX", 0f, 1f),
                    ObjectAnimator.ofFloat(fabSignOut, "scaleY", 0f, 1f)
                )
                duration = 300
                interpolator = OvershootInterpolator()
            }
            animatorSet.start()
        }
    }

    private fun hideMenuOptions() {
        with(binding) {
            fabMenu.animate()
                .rotation(90f)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .setDuration(300)
                .withStartAction {
                    fabMenu.setImageResource(R.drawable.ic_close)
                }
                .withEndAction {
                    fabMenu.setImageResource(R.drawable.ic_menu)
                    fabMenu.animate()
                        .rotation(0f)
                        .setInterpolator(AccelerateDecelerateInterpolator())
                        .setDuration(300)
                        .start()
                }
                .start()

            fabAddMemory.animate()
                .alpha(0f)
                .scaleX(0f)
                .scaleY(0f)
                .setDuration(300)
                .withEndAction {
                    fabAddMemory.visibility = View.GONE
                }
                .start()

            fabSignOut.animate()
                .alpha(0f)
                .scaleX(0f)
                .scaleY(0f)
                .setDuration(300)
                .withEndAction {
                    fabSignOut.visibility = View.GONE
                }
                .start()
        }
    }

    private fun showSignOutConfirmation() {
        hideMenuOptions()
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.sign_out)
            .setMessage(R.string.sign_out_confirmation)
            .setPositiveButton(R.string.confirm) { _, _ ->
                signOut()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is HomeUiState.Welcome -> {
                        binding.tvWelcome.text = getString(R.string.welcome_message, state.userName)
                    }
                    is HomeUiState.Error -> {
                        binding.root.showErrorSnackbar(state.message)
                    }
                    is HomeUiState.SignedOut -> {
                        binding.root.showSuccessSnackbar(state.message)
                        findNavController().navigate(R.id.action_home_to_auth)
                    }
                }
            }
        }
    }

    private fun signOut() {
        viewModel.signOut()
    }

    private fun navigateToAuth() {
        findNavController().navigate(R.id.action_home_to_auth)
    }

    private fun navigateToSearch() {
        try {
            findNavController().navigate(R.id.action_home_to_search)
        } catch (e: Exception) {
            if (e.message?.contains("403") == true) {
                showError(getString(R.string.error_403))
            } else {
                showError(getString(R.string.error_unknown))
            }
        }
    }

    private fun navigateToAddMemory() {
        // TODO: Implement navigation to add memory screen
        Snackbar.make(
            binding.root,
            getString(R.string.coming_soon),
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun navigateToPlaylists() {
        try {
            findNavController().navigate(R.id.action_home_to_playlist)
        } catch (e: Exception) {
            showError(getString(R.string.error_unknown))
        }
    }

    private fun navigateToFavorites() {
        try {
            findNavController().navigate(R.id.action_home_to_favorites)
        } catch (e: Exception) {
            showError(getString(R.string.error_unknown))
        }
    }

    private fun showError(message: String) {
        binding.root.showErrorSnackbar(message)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 