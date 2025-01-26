package com.github.ParagonVirtuoso.memorias.ui.home

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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
    private lateinit var homeViewModel: HomeViewModel

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
        setupViewBinding(inflater, container)
        initializeManagers()
        setupViews()
        observeViewModel()
        return binding.root
    }

    private fun setupViewBinding(inflater: LayoutInflater, container: ViewGroup?) {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
    }

    private fun initializeManagers() {
        authManager = AuthManager(requireContext())
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
    }

    private fun setupViews() {
        binding.googleSignInButton.setOnClickListener { signIn() }
        updateUIForUser()
    }

    private fun observeViewModel() {
        homeViewModel.text.observe(viewLifecycleOwner) {
            binding.textHome.text = it
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
                showSuccessMessage(user.email)
                updateUIForUser()
            },
            onError = { exception ->
                showErrorMessage(exception.message)
            }
        )
    }

    private fun startNewSignIn() {
        try {
            signInLauncher.launch(authManager.getSignInIntent())
        } catch (e: Exception) {
            showErrorMessage(e.message)
        }
    }

    private fun handleSignInResult(resultCode: Int, data: android.content.Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            try {
                val account = GoogleSignIn.getSignedInAccountFromIntent(data)
                    .getResult(ApiException::class.java)
                
                account.idToken?.let { token ->
                    handleExistingAccount(token)
                } ?: showErrorMessage("Token do Google não encontrado")
            } catch (e: ApiException) {
                showErrorMessage("Erro no login do Google: ${e.statusCode}")
            }
        } else {
            showMessage("Login cancelado")
        }
    }

    private fun updateUIForUser() {
        authManager.getCurrentUser()?.let { user ->
            binding.textHome.text = "Bem-vindo, ${user.displayName}"
        }
    }

    private fun showSuccessMessage(email: String?) {
        showMessage("Login realizado com sucesso: $email")
    }

    private fun showErrorMessage(message: String?) {
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