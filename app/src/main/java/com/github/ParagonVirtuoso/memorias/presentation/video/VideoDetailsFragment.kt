package com.github.ParagonVirtuoso.memorias.presentation.video

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.github.ParagonVirtuoso.memorias.databinding.FragmentVideoDetailsBinding
import com.google.android.material.snackbar.Snackbar
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VideoDetailsFragment : Fragment() {

    private var _binding: FragmentVideoDetailsBinding? = null
    private val binding get() = _binding!!
    private val args: VideoDetailsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVideoDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        setupVideoDetails()
        setupVideoPlayer()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupVideoDetails() {
        val video = args.video
        
        binding.apply {
            toolbar.title = video.title
            titleTextView.text = video.title
            descriptionTextView.text = video.description
            channelTextView.text = video.channelTitle
        }
    }

    private fun setupVideoPlayer() {
        lifecycle.addObserver(binding.youtubePlayerView)
        
        binding.youtubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                youTubePlayer.loadVideo(args.video.id, 0f)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 