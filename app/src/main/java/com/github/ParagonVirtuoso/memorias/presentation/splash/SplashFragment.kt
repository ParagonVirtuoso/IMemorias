package com.github.ParagonVirtuoso.memorias.presentation.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.github.ParagonVirtuoso.memorias.R
import com.github.ParagonVirtuoso.memorias.databinding.FragmentSplashBinding
import com.github.ParagonVirtuoso.memorias.util.showErrorSnackbar
import com.github.ParagonVirtuoso.memorias.worker.MemoryNotificationWorker
import com.github.ParagonVirtuoso.memorias.NavGraphDirections
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashFragment : Fragment() {
    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SplashViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.splashState.collect { state ->
                when (state) {
                    is SplashState.Loading -> Unit
                    is SplashState.Authenticated -> checkDeepLinkAndNavigate()
                    is SplashState.Unauthenticated -> navigateToAuth()
                    is SplashState.Error -> showError(state.message)
                }
            }
        }
    }

    private suspend fun checkDeepLinkAndNavigate() {
        delay(1500)
        activity?.intent?.let { intent ->
            val videoId = intent.getStringExtra(MemoryNotificationWorker.KEY_VIDEO_ID)
            val videoTitle = intent.getStringExtra(MemoryNotificationWorker.KEY_VIDEO_TITLE)
            val videoThumbnail = intent.getStringExtra(MemoryNotificationWorker.KEY_VIDEO_THUMBNAIL)

            if (videoId != null && videoTitle != null && videoThumbnail != null) {
                val action = NavGraphDirections.actionGlobalVideoDetails(
                    videoId = videoId,
                    videoTitle = videoTitle,
                    videoThumbnail = videoThumbnail
                )
                findNavController().navigate(action)
            } else {
                findNavController().navigate(R.id.action_splash_to_home)
            }
        } ?: findNavController().navigate(R.id.action_splash_to_home)
    }

    private suspend fun navigateToAuth() {
        delay(1500)
        findNavController().navigate(R.id.action_splash_to_auth)
    }

    private fun showError(message: String) {
        binding.root.showErrorSnackbar(message)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 