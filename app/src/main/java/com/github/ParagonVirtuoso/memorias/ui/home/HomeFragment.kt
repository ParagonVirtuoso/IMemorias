package com.github.ParagonVirtuoso.memorias.ui.home

import android.app.Activity
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.ParagonVirtuoso.memorias.auth.AuthManager
import com.github.ParagonVirtuoso.memorias.databinding.FragmentHomeBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var authManager: AuthManager
    private lateinit var viewModel: HomeViewModel

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        handleSignInResult(result.resultCode, result.data)
    }

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
        setupViewModel()
        setupThemeSwitch()
        initializeManagers()
        setupViews()
        observeViewModel()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
    }

    private fun setupThemeSwitch() {
        binding.themeSwitch.isChecked = isNightModeActive()
        binding.themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            updateTheme(isChecked)
        }
    }

    private fun updateTheme(isDarkMode: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    private fun isNightModeActive(): Boolean {
        return resources.configuration.uiMode and 
               Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }

    private fun initializeManagers() {
        authManager = AuthManager(requireContext())
        updateAuthState()
    }

    private fun updateAuthState() {
        viewModel.updateAuthState(authManager.getCurrentUser())
    }

    private fun setupViews() {
        with(binding) {
            googleSignInButton.setOnClickListener { signIn() }
            signOutButton.setOnClickListener { signOut() }
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            handleUiState(state)
        }

        viewModel.event.observe(viewLifecycleOwner) { event ->
            handleEvent(event)
        }
    }

    private fun handleUiState(state: HomeUiState) {
        when (state) {
            is HomeUiState.Authenticated -> showAuthenticatedUI(state.user)
            is HomeUiState.Unauthenticated -> showUnauthenticatedUI()
            is HomeUiState.Error -> showErrorMessage(state.message)
            HomeUiState.Loading -> showLoadingState()
        }
    }

    private fun handleEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.ShowMessage -> showMessage(event.message)
            HomeEvent.SignInSuccess -> updateAuthState()
            HomeEvent.SignOutSuccess -> updateAuthState()
        }
    }

    private fun showAuthenticatedUI(user: com.google.firebase.auth.FirebaseUser) {
        binding.apply {
            textHome.text = getString(R.string.auth_success, user.displayName)
            googleSignInButton.visibility = View.GONE
            signOutButton.visibility = View.VISIBLE
        }
    }

    private fun showUnauthenticatedUI() {
        binding.apply {
            textHome.text = getString(R.string.welcome_message)
            googleSignInButton.visibility = View.VISIBLE
            signOutButton.visibility = View.GONE
        }
    }

    private fun showLoadingState() {
        binding.apply {
            progressBar.visibility = View.VISIBLE
            googleSignInButton.visibility = View.GONE
            signOutButton.visibility = View.GONE
            textHome.visibility = View.GONE
        }
    }

    private fun signIn() {
        val account = GoogleSignIn.getLastSignedInAccount(requireContext())
        if (account?.idToken != null) {
            handleExistingAccount(account.idToken!!)
        } else {
            startNewSignIn()
        }
    }

    private fun handleExistingAccount(idToken: String) {
        authManager.firebaseAuthWithGoogle(
            idToken = idToken,
            onSuccess = { user ->
                viewModel.onSignInSuccess()
            },
            onError = { exception ->
                viewModel.showError(exception.message ?: "Erro desconhecido")
            }
        )
    }

    private fun startNewSignIn() {
        try {
            signInLauncher.launch(authManager.getSignInIntent())
        } catch (e: Exception) {
            viewModel.showError(e.message ?: "Erro ao iniciar login")
        }
    }

    private fun handleSignInResult(resultCode: Int, data: android.content.Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            try {
                val account = GoogleSignIn.getSignedInAccountFromIntent(data)
                    .getResult(ApiException::class.java)
                
                account.idToken?.let { token ->
                    handleExistingAccount(token)
                } ?: viewModel.showError("Token do Google não encontrado")
            } catch (e: ApiException) {
                viewModel.showError("Erro no login do Google: ${e.statusCode}")
            }
        } else {
            viewModel.showError("Login cancelado")
        }
    }

    private fun signOut() {
        authManager.signOut {
            viewModel.onSignOutSuccess()
        }
    }

    private fun showErrorMessage(message: String) {
        showMessage("Erro: $message")
    }

    private fun showMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}