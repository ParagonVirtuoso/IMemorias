package com.github.ParagonVirtuoso.memorias.presentation.auth

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.github.ParagonVirtuoso.memorias.R
import com.github.ParagonVirtuoso.memorias.databinding.FragmentAuthBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.view.animation.AlphaAnimation

@AndroidEntryPoint
class AuthFragment : Fragment() {
    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()
    
    @Inject
    lateinit var googleSignInClient: GoogleSignInClient

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
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        binding.btnGoogleSignIn.setOnClickListener { signIn() }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.authState.collect { state ->
                handleAuthState(state)
            }
        }
    }

    private fun handleAuthState(state: AuthState) {
        toggleLoading(state is AuthState.Loading)

        when (state) {
            is AuthState.Success -> {
                hideLoading()
                findNavController().navigate(R.id.action_authFragment_to_homeFragment)
            }
            is AuthState.Error -> {
                showMessage(getString(R.string.auth_error, state.message))
            }
            else -> Unit
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    private fun handleSignInResult(resultCode: Int, data: android.content.Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)
                account.idToken?.let { token ->
                    viewModel.signInWithGoogle(token)
                } ?: showMessage(getString(R.string.error_unknown))
            } catch (e: ApiException) {
                showMessage(getString(R.string.auth_error, e.statusCode.toString()))
            }
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun toggleLoading(isLoading: Boolean) {
        val fadeOut = AlphaAnimation(1.0f, 0.0f).apply {
            duration = 300
            fillAfter = true
        }
        val fadeIn = AlphaAnimation(0.0f, 1.0f).apply {
            duration = 300
            fillAfter = true
        }

        if (isLoading) {
            binding.btnGoogleSignIn.startAnimation(fadeOut)
            binding.progressBar.startAnimation(fadeIn)
            binding.btnGoogleSignIn.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.startAnimation(fadeOut)
            binding.btnGoogleSignIn.startAnimation(fadeIn)
            binding.btnGoogleSignIn.isEnabled = true
            binding.progressBar.visibility = View.GONE
        }
        binding.btnGoogleSignIn.text = if (isLoading) "" else getString(R.string.sign_in_with_google)
    }

    private fun hideLoading() {
        toggleLoading(false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 