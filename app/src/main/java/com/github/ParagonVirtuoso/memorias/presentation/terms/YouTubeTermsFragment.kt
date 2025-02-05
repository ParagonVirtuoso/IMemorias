package com.github.ParagonVirtuoso.memorias.presentation.terms

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.github.ParagonVirtuoso.memorias.databinding.FragmentYoutubeTermsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class YouTubeTermsFragment : Fragment() {
    private var _binding: FragmentYoutubeTermsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentYoutubeTermsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        loadYouTubeTerms()
    }

    private fun setupViews() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.swipeRefresh.setOnRefreshListener {
            loadYouTubeTerms()
        }

        binding.webView.apply {
            settings.apply {
                javaScriptEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                domStorageEnabled = true
            }
            
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    binding.progressIndicator.isVisible = false
                    binding.swipeRefresh.isRefreshing = false
                }

                override fun onReceivedError(
                    view: WebView?,
                    errorCode: Int,
                    description: String?,
                    failingUrl: String?
                ) {
                    super.onReceivedError(view, errorCode, description, failingUrl)
                    binding.progressIndicator.isVisible = false
                    binding.swipeRefresh.isRefreshing = false
                }
            }
            webChromeClient = WebChromeClient()
        }
    }

    private fun loadYouTubeTerms() {
        binding.progressIndicator.isVisible = true
        binding.webView.loadUrl("https://www.youtube.com/t/terms?hl=pt-BR")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 